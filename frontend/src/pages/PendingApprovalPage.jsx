import Icon from '../components/Icon.jsx'

export default function PendingApprovalPage({ username, onLogout }) {
  return <main className="pending-approval-page"><section className="pending-approval-card"><span className="pending-approval-mark"><Icon name="building" size={30}/></span><span className="eyebrow muted-eyebrow">HOMEBASE · XÁC MINH TÀI KHOẢN</span><h1>Yêu cầu chủ nhà đang chờ duyệt</h1><p>Tài khoản <b>{username}</b> đã được tạo. ADMIN cần xác minh trước khi bật quyền quản lý tòa nhà.</p><div className="pending-approval-note"><Icon name="incident" size={17}/><span>Bạn vẫn có thể đăng xuất và đăng nhập lại sau khi tài khoản được duyệt.</span></div><button className="button button-secondary" onClick={onLogout}><Icon name="logout" size={16}/> Đăng xuất</button></section></main>
}
