import Icon from '../components/Icon.jsx'

export default function PendingApprovalPage({ username, onLogout }) {
  return <main className="pending-approval-page"><section className="pending-approval-card"><span className="pending-approval-mark"><Icon name="building" size={30}/></span><span className="eyebrow muted-eyebrow">HOMEBASE · XÁC MINH TÀI KHOẢN</span><h1>Yêu cầu chủ nhà đang chờ duyệt</h1><p>Tài khoản <b>{username}</b></p><button className="button button-secondary" onClick={onLogout}><Icon name="logout" size={16}/> Đăng xuất</button></section></main>
}
