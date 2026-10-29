import {pageHref,followPageLink} from '../data/browserNavigation.js'
import Icon from './Icon.jsx'
import { visibleModules } from '../data/permissions.js'

function NavLink({target,page,onNavigate,children,label}){
 return <a href={pageHref(target)} className={`nav-link ${page===target?'selected':''}`} aria-current={page===target?'page':undefined} aria-label={label} title={label} onClick={event=>followPageLink(event,onNavigate,target)}>{children}</a>
}

export function Sidebar({ page, user, profile, onNavigate, onLogout, open, onClose, role, collapsed=false,onToggle, pendingRequests = 0 }) {
  return <>
    {open && <button className="sidebar-scrim" aria-label="Đóng menu" onClick={onClose} />}
    <aside id="app-sidebar" aria-label="Điều hướng chính" className={`sidebar ${open ? 'sidebar-open' : ''}`}>
      <div className="sidebar-heading"><div className="brand-lockup"><div className="brand-icon"><Icon name="building" size={21} /></div><div><b>HomeBase</b><span>BUILDING MANAGEMENT</span></div></div>
      <button className="icon-button sidebar-toggle desktop-sidebar-toggle" aria-label={collapsed?'Mở rộng thanh bên':'Thu gọn thanh bên'} aria-expanded={!collapsed} aria-controls="app-sidebar" title={collapsed?'Mở rộng thanh bên':'Thu gọn thanh bên'} onClick={onToggle}><span className={collapsed?'':'rotate-chevron'}><Icon name="chevron"/></span></button><button className="icon-button mobile-sidebar-close" aria-label="Đóng menu" onClick={onClose}><Icon name="close"/></button></div>
      <div className="nav-section"><div className="nav-heading">MENU</div>
        <NavLink target="dashboard" page={page} onNavigate={onNavigate} label="Tổng quan"><Icon name="dashboard"/><span>Tổng quan</span></NavLink>
      </div>
      {['USER','ADMIN'].includes(role) && <div className="nav-section"><div className="nav-heading">GIAO DỊCH</div>
        <NavLink target="rental-requests" page={page} onNavigate={onNavigate} label={role==='USER'?'Đăng ký thuê':'Yêu cầu thuê'}><Icon name="contract"/><span>{role==='USER'?'Đăng ký thuê':'Yêu cầu thuê'}</span>{role!=='USER'&&pendingRequests>0&&<b className="nav-badge">{pendingRequests>99?'99+':pendingRequests}</b>}</NavLink>
        <NavLink target="payments" page={page} onNavigate={onNavigate} label="Thanh toán"><Icon name="invoice"/><span>Thanh toán</span></NavLink>
        {role==='ADMIN'&&<NavLink target="profits" page={page} onNavigate={onNavigate} label="Lợi nhuận"><Icon name="meter"/><span>Lợi nhuận</span></NavLink>}
      </div>}
      {role==='ADMIN'&&<div className="nav-section"><div className="nav-heading">QUẢN TRỊ</div><NavLink target="accounts" page={page} onNavigate={onNavigate} label="Tài khoản"><Icon name="users"/><span>Tài khoản</span></NavLink></div>}
      <div className="nav-section"><div className="nav-heading">QUẢN LÝ TÒA NHÀ</div>
        {visibleModules(role).map((module) => <NavLink key={module.key} target={module.key} page={page} onNavigate={onNavigate} label={module.title}><Icon name={module.icon}/><span>{module.title}</span></NavLink>)}
      </div>
      <div className="sidebar-bottom"><NavLink target="profile" page={page} onNavigate={onNavigate} label="Thông tin cá nhân"><Icon name="employee"/><span>Thông tin cá nhân</span></NavLink><div className="profile-card"><div className="profile-avatar">{profile?.avatarUrl?<img src={profile.avatarUrl} alt="Ảnh đại diện"/>:(profile?.displayName || user?.username || 'U').slice(0, 1).toUpperCase()}</div><div className="profile-text"><b>{profile?.displayName || user?.username || 'Người dùng'}</b><span>{role==='ADMIN'?'Chủ nhà · ADMIN':'Khách thuê · USER'}</span></div><button className="icon-button logout-button" title="Đăng xuất" onClick={onLogout}><Icon name="logout" /></button></div><p>Nhóm 8 · Quản lý tòa nhà</p></div>
    </aside>
  </>
}

export function Topbar({ title, onMenu, onRefresh,collapsed=false,mobileOpen=false }) {
  return <header className="topbar"><div className="topbar-left"><button className="icon-button menu-button desktop-menu-toggle" onClick={onMenu} aria-label={collapsed?'Mở rộng thanh bên':'Thu gọn thanh bên'} aria-expanded={!collapsed} aria-controls="app-sidebar"><Icon name="menu"/></button><button className="icon-button menu-button mobile-menu-toggle" onClick={onMenu} aria-label={mobileOpen?'Đóng menu':'Mở menu'} aria-expanded={mobileOpen} aria-controls="app-sidebar"><Icon name="menu"/></button><div><div className="breadcrumb">QUẢN LÝ TÒA NHÀ <span>/</span> <b>{title.toUpperCase()}</b></div><h1>{title}</h1></div></div><div className="topbar-right"><div className="today-chip"><Icon name="calendar" size={16} /><span>{new Intl.DateTimeFormat('vi-VN',{weekday:'short',day:'2-digit',month:'short',year:'numeric'}).format(new Date())}</span></div><button className="icon-button refresh-button" title="Làm mới" onClick={onRefresh}><Icon name="refresh" /></button></div></header>
}
