import Icon from './Icon.jsx'
import { visibleModules } from '../data/permissions.js'

export function Sidebar({ page, user, profile, onNavigate, onLogout, open, onClose, role, pendingRequests = 0 }) {
  return <>
    {open && <button className="sidebar-scrim" aria-label="Đóng menu" onClick={onClose} />}
    <aside className={`sidebar ${open ? 'sidebar-open' : ''}`}>
      <div className="brand-lockup"><div className="brand-icon"><Icon name="building" size={21} /></div><div><b>HomeBase</b><span>BUILDING MANAGEMENT</span></div></div>
      <div className="nav-section"><div className="nav-heading">MENU</div>
        <button className={`nav-link ${page === 'dashboard' ? 'selected' : ''}`} onClick={() => onNavigate('dashboard')}><Icon name="dashboard" /><span>Tổng quan</span></button>
      </div>
      {['USER','ADMIN'].includes(role) && <div className="nav-section"><div className="nav-heading">GIAO DỊCH</div>
        <button className={`nav-link ${page === 'rental-requests' ? 'selected' : ''}`} onClick={() => onNavigate('rental-requests')}><Icon name="contract"/><span>{role==='USER'?'Đăng ký thuê':'Yêu cầu thuê'}</span>{role!=='USER'&&pendingRequests>0&&<b className="nav-badge">{pendingRequests>99?'99+':pendingRequests}</b>}</button>
        <button className={`nav-link ${page === 'payments' ? 'selected' : ''}`} onClick={() => onNavigate('payments')}><Icon name="invoice"/><span>Thanh toán</span></button>
      </div>}
      {role==='ADMIN'&&<div className="nav-section"><div className="nav-heading">QUẢN TRỊ</div><button className="nav-link" onClick={() => onNavigate("accounts")}><Icon name="users"/><span>Tài khoản</span></button></div>}
      <div className="nav-section"><div className="nav-heading">QUẢN LÝ TÒA NHÀ</div>
        {visibleModules(role).map((module) => <button key={module.key} className={`nav-link ${page === module.key ? 'selected' : ''}`} onClick={() => onNavigate(module.key)}><Icon name={module.icon} /><span>{module.title}</span></button>)}
      </div>
      <div className="sidebar-bottom"><button className={`nav-link profile-nav-link ${page === 'profile' ? 'selected' : ''}`} onClick={() => onNavigate('profile')}><Icon name="employee"/><span>Thông tin cá nhân</span></button><div className="profile-card"><div className="profile-avatar">{profile?.avatarUrl?<img src={profile.avatarUrl} alt="Ảnh đại diện"/>:(profile?.displayName || user?.username || 'U').slice(0, 1).toUpperCase()}</div><div className="profile-text"><b>{profile?.displayName || user?.username || 'Người dùng'}</b><span>{role==='ADMIN'?'Chủ nhà · ADMIN':'Khách thuê · USER'}</span></div><button className="icon-button logout-button" title="Đăng xuất" onClick={onLogout}><Icon name="logout" /></button></div><p>Nhóm 8 · Quản lý tòa nhà</p></div>
    </aside>
  </>
}

export function Topbar({ title, onMenu, onRefresh }) {
  return <header className="topbar"><div className="topbar-left"><button className="icon-button menu-button" onClick={onMenu} aria-label="Mở menu"><Icon name="menu" /></button><div><div className="breadcrumb">QUẢN LÝ TÒA NHÀ <span>/</span> <b>{title.toUpperCase()}</b></div><h1>{title}</h1></div></div><div className="topbar-right"><div className="today-chip"><Icon name="calendar" size={16} /><span>{new Intl.DateTimeFormat('vi-VN',{weekday:'short',day:'2-digit',month:'short',year:'numeric'}).format(new Date())}</span></div><button className="icon-button refresh-button" title="Làm mới" onClick={onRefresh}><Icon name="refresh" /></button></div></header>
}
