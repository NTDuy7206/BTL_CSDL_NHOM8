import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {initialPage,pageFromHash,visitPage} from './data/browserNavigation.js'
import AuthPage from './components/AuthPage.jsx'
import AccountsPage from './pages/AccountsPage.jsx'
import { Sidebar, Topbar } from './components/AppShell.jsx'
import Toast from './components/Toast.jsx'
import DashboardPage from './pages/DashboardPage.jsx'
import EntityPage from './pages/EntityPage.jsx'
import ProfilePage from './pages/ProfilePage.jsx'
import ContractRequestsPage from './pages/ContractRequestsPage.jsx'
import BillingNotice from './components/BillingNotice.jsx'
import ProfitsPage from './pages/ProfitsPage.jsx'
import PaymentsPage from './pages/PaymentsPage.jsx'

import IncidentReportPage from './pages/IncidentReportPage.jsx'
import {visibleModules} from './data/permissions.js'
import { modules } from './data/modules.js'
import { request as apiRequest, signIn, signUp } from './api/client.js'

function readSession() {
  try { return JSON.parse(sessionStorage.getItem('buildingUser') || 'null') } catch { return null }
}

export default function App() {
  const [token,setToken]=useState(sessionStorage.getItem('buildingToken')||'')
  const [user,setUser]=useState(readSession)
  const [page,setPage]=useState(()=>initialPage(window,sessionStorage,readSession()))
  const [mobileNav,setMobileNav]=useState(false)
  const [collapsed,setCollapsed]=useState(()=>{try{return localStorage.getItem('buildingSidebarCollapsed')==='true'}catch{return false}})
  function toggleSidebar(){if(window.matchMedia('(max-width:700px)').matches){setMobileNav(v=>!v);return}setCollapsed(value=>{const next=!value;try{localStorage.setItem('buildingSidebarCollapsed',String(next))}catch{}return next})}
  const [toast,setToast]=useState(null)
  const [refreshKey,setRefreshKey]=useState(0)
  const [profile,setProfile]=useState(null)
  const [paymentInvoiceId,setPaymentInvoiceId]=useState('')
  const tokenRef=useRef(token);tokenRef.current=token
  const [pendingRequests,setPendingRequests]=useState(0)
  const [pendingPayments,setPendingPayments]=useState(0)
  const activeModule=useMemo(()=>visibleModules(user?.role).find(module=>module.key===page),[page,user?.role])
  const notify=useCallback((message,type='success')=>{setToast({message,type});window.setTimeout(()=>setToast(null),3600)},[])
  const request=useCallback(async(path,options={})=>{
    try{const result=await apiRequest(path,{...options,token});if(tokenRef.current!==token)throw new Error('Phiên đăng nhập đã thay đổi');return result}
    catch(error){if(error.status===401&&tokenRef.current===token){sessionStorage.removeItem('buildingToken');sessionStorage.removeItem('buildingUser');setToken('');setUser(null);notify('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.','error')}throw error}
  },[token,notify])
  useEffect(()=>{
    if(!token){setProfile(null);return}
    let active=true
    request('/api/profile').then(result=>{if(active)setProfile(result)}).catch(()=>{})
    return()=>{active=false}
  },[token,request])
  function finishLogin(result){const nextToken=result.accessToken;const nextUser=result.user||{};sessionStorage.setItem('buildingToken',nextToken);sessionStorage.setItem('buildingUser',JSON.stringify(nextUser));tokenRef.current=nextToken;setPaymentInvoiceId('');setToken(nextToken);setUser(nextUser);setPage(visitPage(window,sessionStorage,nextUser,pageFromHash(window.location.hash,nextUser)||'dashboard',{replace:true}));notify('Đăng nhập thành công.');}
  async function login(username,password){const result=await signIn(username,password);finishLogin(result)}
  async function register(username,password,role,roomCount,rooms){await signUp(username,password,role,roomCount,rooms);const result=await signIn(username,password);finishLogin(result)}
  function logout(){sessionStorage.removeItem('buildingToken');sessionStorage.removeItem('buildingUser');tokenRef.current='';setPaymentInvoiceId('');setToken('');setUser(null);setProfile(null);setPage('dashboard');window.history.replaceState(null,'','/#/login');setMobileNav(false)}
  function navigate(next){setPage(visitPage(window,sessionStorage,user,next));setMobileNav(false);window.scrollTo({top:0,behavior:'instant'})}
  useEffect(()=>{
    if(!token)return
    const sync=()=>{const next=initialPage(window,sessionStorage,user);setPage(visitPage(window,sessionStorage,user,next,{replace:true}));setMobileNav(false)}
    sync();window.addEventListener('popstate',sync);window.addEventListener('hashchange',sync)
    return()=>{window.removeEventListener('popstate',sync);window.removeEventListener('hashchange',sync)}
  },[token,user])
  useEffect(()=>{if(!mobileNav)return;const close=e=>{if(e.key==='Escape')setMobileNav(false)};window.addEventListener('keydown',close);return()=>window.removeEventListener('keydown',close)},[mobileNav])
  const title=page==='accounts'?'Tài khoản':page==='profile'?'Thông tin cá nhân':activeModule?.title||'Tổng quan'
  const role=user?.role||'USER'
  const canManage=role==='ADMIN'

  useEffect(()=>{
    if(!canManage){setPendingRequests(0);setPendingPayments(0);return}
    let active=true
    const refresh=()=>Promise.allSettled([request('/api/contract-requests'),request('/api/payment-reports')]).then(([contracts,payments])=>{if(!active)return;if(contracts.status==='fulfilled'&&Array.isArray(contracts.value))setPendingRequests(contracts.value.filter(row=>row.status==='PENDING').length);if(payments.status==='fulfilled'&&Array.isArray(payments.value))setPendingPayments(payments.value.filter(row=>row.status==='PENDING').length)})
    refresh()
    const timer=window.setInterval(refresh,30000)
    return()=>{active=false;window.clearInterval(timer)}
  },[request,canManage])

  const pageTitle=page==='rental-requests'?(role==='USER'?'Đăng ký thuê phòng':'Yêu cầu thuê'):page==='profits'?'Lợi nhuận':page==='payments'?'Thanh toán':title

  if(!token)return <><AuthPage onLogin={login} onRegister={register}/><Toast toast={toast}/></>
  return <div className={`app-frame ${collapsed?'sidebar-collapsed':''}`}><Sidebar collapsed={collapsed} onToggle={toggleSidebar} page={page} user={user} profile={profile} onNavigate={navigate} onLogout={logout} open={mobileNav} onClose={()=>setMobileNav(false)} role={role} pendingRequests={pendingRequests} pendingPayments={pendingPayments}/><div className="app-main"><Topbar title={pageTitle} onMenu={toggleSidebar} collapsed={collapsed} mobileOpen={mobileNav} onRefresh={()=>setRefreshKey(v=>v+1)}/><main className="content-area" key={`${user?.id||user?.username}-${page}-${refreshKey}`}><BillingNotice request={request} role={role} onNavigate={navigate}/>{page==='profits'&&canManage?<ProfitsPage request={request} notify={notify}/>:page==='accounts'&&canManage?<AccountsPage request={request}/>:page==='rental-requests'?<ContractRequestsPage request={request} role={role} notify={notify}/>:page==='payments'?<PaymentsPage request={request} role={role} notify={notify} initialInvoiceId={paymentInvoiceId}/>:activeModule?.key==='incidents'&&!canManage?<IncidentReportPage module={activeModule} request={request} notify={notify}/>:activeModule?<EntityPage onNavigate={navigate} module={activeModule} request={request} notify={notify} readOnly={!canManage} onPay={!canManage&&activeModule.key==='invoices'?invoice=>{setPaymentInvoiceId(invoice.invoiceId);navigate('payments')}:undefined}/>:page==='profile'?<ProfilePage request={request} profile={profile} user={user} onProfileUpdated={setProfile} notify={notify}/>:<DashboardPage request={request} user={user} onNavigate={navigate}/>}</main><footer className="app-footer"><span>© 2026 HomeBase · Nhóm 8</span><span>Hệ thống quản lý tòa nhà và phòng cho thuê</span></footer></div><Toast toast={toast}/></div>
}
