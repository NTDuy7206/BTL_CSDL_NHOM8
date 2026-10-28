import { useCallback, useEffect, useMemo, useState } from 'react'
import AuthPage from './components/AuthPage.jsx'
import { Sidebar, Topbar } from './components/AppShell.jsx'
import Toast from './components/Toast.jsx'
import DashboardPage from './pages/DashboardPage.jsx'
import EntityPage from './pages/EntityPage.jsx'
import ProfilePage from './pages/ProfilePage.jsx'
import ContractRequestsPage from './pages/ContractRequestsPage.jsx'
import PaymentsPage from './pages/PaymentsPage.jsx'

import RentalForm from './components/RentalForm.jsx'
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
  const [page,setPage]=useState('dashboard')
  const [mobileNav,setMobileNav]=useState(false)
  const [toast,setToast]=useState(null)
  const [refreshKey,setRefreshKey]=useState(0)
  const [profile,setProfile]=useState(null)
  const [pendingRequests,setPendingRequests]=useState(0)
  const activeModule=useMemo(()=>visibleModules(user?.role).find(module=>module.key===page),[page,user?.role])
  const notify=useCallback((message,type='success')=>{setToast({message,type});window.setTimeout(()=>setToast(null),3600)},[])
  const request=useCallback(async(path,options={})=>{
    try{return await apiRequest(path,{...options,token})}
    catch(error){if(error.status===401){sessionStorage.removeItem('buildingToken');sessionStorage.removeItem('buildingUser');setToken('');setUser(null);notify('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.','error')}throw error}
  },[token,notify])
  useEffect(()=>{
    if(!token){setProfile(null);return}
    let active=true
    request('/api/profile').then(result=>{if(active)setProfile(result)}).catch(()=>{})
    return()=>{active=false}
  },[token,request])
  function finishLogin(result){const nextToken=result.accessToken;const nextUser=result.user||{};sessionStorage.setItem('buildingToken',nextToken);sessionStorage.setItem('buildingUser',JSON.stringify(nextUser));setToken(nextToken);setUser(nextUser);setPage('dashboard');notify('Đăng nhập thành công.');}
  async function login(username,password){const result=await signIn(username,password);finishLogin(result)}
  async function register(username,password,role){await signUp(username,password,role);const result=await signIn(username,password);finishLogin(result)}
  function logout(){sessionStorage.removeItem('buildingToken');sessionStorage.removeItem('buildingUser');setToken('');setUser(null);setProfile(null);setPage('dashboard');setMobileNav(false)}
  function navigate(next){setPage(next);setMobileNav(false)}
  const title=page==='profile'?'Thông tin cá nhân':activeModule?.title||'Tổng quan'
  const role=user?.role||'USER'
  const canManage=role==='ADMIN'

  useEffect(()=>{
    if(!canManage){setPendingRequests(0);return}
    let active=true
    const refresh=()=>request('/api/contract-requests').then(rows=>{if(active)setPendingRequests(Array.isArray(rows)?rows.filter(row=>row.status==='PENDING').length:0)}).catch(()=>{})
    refresh()
    const timer=window.setInterval(refresh,45000)
    return()=>{active=false;window.clearInterval(timer)}
  },[request,canManage])

  const pageTitle=page==='rental-requests'?(role==='USER'?'Đăng ký thuê phòng':'Yêu cầu thuê'):page==='payments'?'Thanh toán':page==='new-rental'?'Thêm khách & hợp đồng':title

  if(!token)return <><AuthPage onLogin={login} onRegister={register}/><Toast toast={toast}/></>
  return <div className="app-frame"><Sidebar page={page} user={user} profile={profile} onNavigate={navigate} onLogout={logout} open={mobileNav} onClose={()=>setMobileNav(false)} role={role} pendingRequests={pendingRequests}/><div className="app-main"><Topbar title={pageTitle} onMenu={()=>setMobileNav(v=>!v)} onRefresh={()=>setRefreshKey(v=>v+1)}/><main className="content-area" key={`${page}-${refreshKey}`}>{page==='rental-requests'?<ContractRequestsPage request={request} role={role} notify={notify}/>:page==='payments'?<PaymentsPage request={request} role={role} notify={notify}/>:page==='new-rental'&&canManage?<RentalForm admin request={request} notify={notify} onSaved={()=>navigate('contracts')}/>:activeModule?.key==='incidents'&&!canManage?<IncidentReportPage module={activeModule} request={request} notify={notify}/>:activeModule?<EntityPage module={activeModule} request={request} notify={notify} readOnly={!canManage} onCreateRental={canManage?()=>navigate('new-rental'):undefined}/>:page==='profile'?<ProfilePage request={request} profile={profile} user={user} onProfileUpdated={setProfile} notify={notify}/>:<DashboardPage request={request} user={user} onNavigate={navigate}/>}</main><footer className="app-footer"><span>© 2026 HomeBase · Nhóm 8</span><span>Hệ thống quản lý tòa nhà và phòng cho thuê</span></footer></div><Toast toast={toast}/></div>
}
