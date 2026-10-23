import { useCallback, useEffect, useState } from 'react'
import Icon from '../components/Icon.jsx'

export default function AccountApprovalsPage({ request, notify }) {
  const [accounts, setAccounts] = useState([])
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const load = useCallback(async () => {
    setError('')
    try { const rows = await request('/api/admin/accounts/pending-owners'); setAccounts(Array.isArray(rows) ? rows : []) }
    catch (err) { setError(err.message || 'Không tải được danh sách tài khoản.') }
  }, [request])
  useEffect(() => { load() }, [load])

  async function approve(account) {
    setBusy(true); setError('')
    try {
      await request(`/api/admin/accounts/${account.id}/approve-owner`, { method: 'PATCH' })
      notify(`Đã kích hoạt vai trò chủ nhà cho ${account.username}.`)
      await load()
    } catch (err) { setError(err.message || 'Không duyệt được tài khoản.') }
    finally { setBusy(false) }
  }

  return <div className="page-stack">
    <div className="page-intro"><div className="page-intro-icon"><Icon name="users" size={21}/></div><div><h2>Duyệt tài khoản chủ nhà</h2><p>Chủ nhà mới chỉ được quản lý dữ liệu sau khi ADMIN xác minh và duyệt tài khoản.</p></div><span className="intro-spacer"/><button className="icon-button refresh-button" onClick={load} title="Tải lại"><Icon name="refresh"/></button></div>
    {error&&<div className="alert alert-error"><Icon name="incident" size={16}/>{error}</div>}
    <section className="content-card workflow-card"><div className="card-heading"><div><h2>Đang chờ duyệt</h2><p>{accounts.length} tài khoản</p></div><span className="module-count">ADMIN</span></div>
      {accounts.length ? <div className="workflow-list">{accounts.map(account=><article className="workflow-row" key={account.id}><span className="workflow-icon"><Icon name="employee" size={18}/></span><div className="workflow-main"><b>{account.username}</b><span>Mã tài khoản: {account.id}</span></div><span className="status-pill status-pending">Chờ xác minh</span><button className="button button-primary" disabled={busy} onClick={()=>approve(account)}>Duyệt làm chủ nhà</button></article>)}</div> : <div className="workflow-empty"><Icon name="check" size={22}/><span>{error?'Không tải được danh sách.':'Không có tài khoản nào đang chờ duyệt.'}</span></div>}
    </section>
  </div>
}
