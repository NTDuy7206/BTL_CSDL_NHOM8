import {useEffect, useRef, useState} from 'react'
import {todayVN} from '../data/finance.js'
import {requireRows} from '../data/listResponse.js'
import {activeContract} from '../data/roomGroups.js'

export default function TenantCleaningPage({module, request, notify}) {
  const [rooms, setRooms] = useState(null)
  const [rows, setRows] = useState(null)
  const [roomId, setRoomId] = useState('')
  const [content, setContent] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const listVersion = useRef(0)

  useEffect(() => {
    let active = true
    request('/api/contracts').then(result => {
      if (!active) return
      const today = todayVN()
      const contracts = requireRows(result).filter(contract => activeContract(contract, today))
      setRooms([...new Set(contracts.map(contract => contract.room.roomId).filter(Boolean))])
    }).catch(err => { if (active) setError(err.message) })
    const refresh = () => {
      const version = ++listVersion.current
      request(module.path).then(result => {
        if (active && version === listVersion.current) setRows(requireRows(result))
      }).catch(err => { if (active && version === listVersion.current) setError(err.message) })
    }
    refresh()
    const timer = window.setInterval(refresh, 30000)
    return () => { active = false; window.clearInterval(timer) }
  }, [request, module.path])

  async function submit(event) {
    event.preventDefault()
    if (busy || !rooms?.includes(roomId) || !content.trim()) return
    setBusy(true)
    setError('')
    try {
      const row = await request(module.path, {
        method: 'POST', body: JSON.stringify({roomId, taskContent: content.trim()}),
      })
      listVersion.current++
      setRows(current => [row, ...(current ?? []).filter(item => item.supportId !== row.supportId)])
      setContent('')
      notify('Đã gửi yêu cầu dọn dẹp cho chủ nhà.')
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return <div className="page-stack">
    <section className="content-card workflow-card">
      <h2>Yêu cầu hỗ trợ dọn dẹp</h2>
      {error && <div className="alert alert-error" role="alert">{error}</div>}
      {rooms?.length === 0 && <p>Bạn cần có hợp đồng đang hiệu lực để gửi yêu cầu.</p>}
      <form className="workflow-form" onSubmit={submit}>
        <label className="form-field"><span>Phòng đang thuê</span>
          <select required value={roomId} onChange={event => setRoomId(event.target.value)}>
            <option value="">Chọn phòng</option>
            {rooms?.map(id => <option key={id} value={id}>{id}</option>)}
          </select>
        </label>
        <label className="form-field"><span>Nội dung cần hỗ trợ</span>
          <textarea required maxLength={500} rows={3} value={content} onChange={event => setContent(event.target.value)}/>
        </label>
        <button className="button button-primary" disabled={busy || !roomId || !content.trim()}>{busy ? 'Đang gửi…' : 'Gửi yêu cầu'}</button>
      </form>
    </section>
    <section className="content-card workflow-card">
      <h2>Yêu cầu của tôi</h2>
      {rows?.length === 0 && <p>Chưa có yêu cầu dọn dẹp.</p>}
      {!!rows?.length && <div className="payment-table-wrap"><table className="payment-table">
        <thead><tr><th>Ngày gửi</th><th>Phòng</th><th>Nội dung</th><th>Trạng thái</th></tr></thead>
        <tbody>{rows.map(row => <tr key={row.supportId}>
          <td>{row.supportDate}</td><td>{row.roomId}</td><td>{row.taskContent}</td>
          <td>{row.isCompleted ? 'Đã xong' : 'Chờ xử lý'}</td>
        </tr>)}</tbody>
      </table></div>}
    </section>
  </div>
}
