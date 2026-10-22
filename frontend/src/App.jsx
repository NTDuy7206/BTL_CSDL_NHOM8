import { useCallback, useEffect, useMemo, useState } from 'react'
import AuthPage from './components/AuthPage.jsx'
import { Sidebar, Topbar } from './components/AppShell.jsx'
import Toast from './components/Toast.jsx'
import DashboardPage from './pages/DashboardPage.jsx'
import EntityPage from './pages/EntityPage.jsx'
import ProfilePage from './pages/ProfilePage.jsx'
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
  const activeModule=useMemo(()=>modules.find(module=>module.key===page),[page])
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
  async function register(username,password){await signUp(username,password);const result=await signIn(username,password);finishLogin(result)}
  function logout(){sessionStorage.removeItem('buildingToken');sessionStorage.removeItem('buildingUser');setToken('');setUser(null);setProfile(null);setPage('dashboard');setMobileNav(false)}
  function navigate(next){setPage(next);setMobileNav(false)}
  const title=page==='profile'?'Thông tin cá nhân':activeModule?.title||'Tổng quan'

  if(!token)return <><AuthPage onLogin={login} onRegister={register}/><Toast toast={toast}/></>
  return <div className="app-frame"><Sidebar page={page} user={user} profile={profile} onNavigate={navigate} onLogout={logout} open={mobileNav} onClose={()=>setMobileNav(false)}/><div className="app-main"><Topbar title={title} onMenu={()=>setMobileNav(v=>!v)} onRefresh={()=>setRefreshKey(v=>v+1)}/><main className="content-area" key={`${page}-${refreshKey}`}>{activeModule?<EntityPage module={activeModule} request={request} notify={notify}/>:page==='profile'?<ProfilePage request={request} profile={profile} user={user} onProfileUpdated={setProfile} notify={notify}/>:<DashboardPage request={request} user={user} onNavigate={navigate}/>}</main><footer className="app-footer"><span>© 2026 HomeBase · Nhóm 8</span><span>Hệ thống quản lý tòa nhà và phòng cho thuê</span></footer></div><Toast toast={toast}/></div>
}
