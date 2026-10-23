import { useCallback, useEffect, useState } from 'react'
import Icon from '../components/Icon.jsx'

function date(value) {
  if (!value) return '—'
  return new Intl.DateTimeFormat('vi-VN').format(new Date(`${value.slice(0, 10)}T00:00:00`))
}

function statusLabel(value) {
  return value === 'APPROVED' ? 'Đã chấp thuận' : value === 'REJECTED' ? 'Đã từ chối' : 'Đang chờ'
}

function available(room) {
  const value = String(room.status || '').toLowerCase().replace('đ', 'd').normalize('NFD').replace(/[\u0300-\u036f]/g, '')
  if (['not available', 'not vacant', 'not occupied', 'occupied', 'rented', 'leased', 'khong trong', 'da thue', 'dang thue'].some(word => value.includes(word))) return false
  return ['available', 'vacant', 'unoccupied', 'empty', 'trong', 'chua thue', 'chua cho thue'].some(word => value.includes(word))
}

export default function ContractRequestsPage({ request, role, notify }) {
  const isTenant = role === 'TENANT'
  const [requests, setRequests] = useState([])
  const [rooms, setRooms] = useState([])
  const [roomId, setRoomId] = useState('')
  const [startDate, setStartDate] = useState('')
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    setError('')
    try {
      const rows = await request('/api/contract-requests')
      setRequests(Array.isArray(rows) ? rows : [])
      if (isTenant) {
        const availableRooms = await request('/api/rooms')
        const choices = (Array.isArray(availableRooms) ? availableRooms : []).filter(available)
        setRooms(choices)
        setRoomId(current => current || choices[0]?.roomId || '')
      }
    } catch (err) { setError(err.message || 'Không tải được yêu cầu thuê.') }
  }, [request, isTenant])

  useEffect(() => { load() }, [load])

  async function submit(event) {
    event.preventDefault(); setBusy(true); setError('')
    try {
      await request('/api/contract-requests', { method: 'POST', body: JSON.stringify({ roomId, requestedStartDate: startDate, message }) })
      setMessage(''); setStartDate(''); notify('Đã gửi yêu cầu thuê. Chủ nhà sẽ xem và phản hồi trong hệ thống.')
      await load()
    } catch (err) { setError(err.message || 'Không gửi được yêu cầu.') }
    finally { setBusy(false) }
  }

  async function decide(row, status) {
    setBusy(true); setError('')
    try {
      await request(`/api/contract-requests/${row.requestId}/status`, { method: 'PATCH', body: JSON.stringify({ status }) })
      notify(status === 'APPROVED' ? 'Đã chấp thuận yêu cầu. Hãy tạo hợp đồng chính thức trong mục Hợp đồng.' : 'Đã từ chối yêu cầu.')
      await load()
    } catch (err) { setError(err.message || 'Không cập nhật được yêu cầu.') }
    finally { setBusy(false) }
  }

  return <div className="page-stack">
    <div className="page-intro"><div className="page-intro-icon"><Icon name="contract" size={21}/></div><div><h2>{isTenant ? 'Đăng ký thuê phòng' : 'Yêu cầu thuê phòng'}</h2><p>{isTenant ? 'Gửi đề nghị thuê phòng và theo dõi phản hồi của chủ nhà.' : 'Các yêu cầu mới hiển thị ở đây để chủ nhà xem và phản hồi.'}</p></div><span className="intro-spacer"/><button className="icon-button refresh-button" onClick={load} title="Tải lại"><Icon name="refresh"/></button></div>
    {isTenant && <section className="content-card workflow-card"><div className="workflow-heading"><span className="workflow-icon"><Icon name="building"/></span><div><h2>Gửi yêu cầu thuê</h2><p>Đề nghị sẽ được lưu ở trạng thái chờ. Hợp đồng có hiệu lực sau khi chủ nhà duyệt và lập hợp đồng chính thức.</p></div></div>
      <form className="workflow-form" onSubmit={submit}><label className="form-field"><span>Phòng còn trống</span><select required value={roomId} onChange={event=>setRoomId(event.target.value)}><option value="">Chọn phòng</option>{rooms.map(room=><option key={room.roomId} value={room.roomId}>{room.roomId} · {room.roomType || 'Phòng'} · {Number(room.basePrice || 0).toLocaleString('vi-VN')} ₫/tháng</option>)}</select></label><label className="form-field"><span>Ngày dự kiến bắt đầu</span><input required type="date" min={new Date().toISOString().slice(0,10)} value={startDate} onChange={event=>setStartDate(event.target.value)}/></label><label className="form-field workflow-message"><span>Lời nhắn cho chủ nhà</span><textarea maxLength="500" rows="3" value={message} onChange={event=>setMessage(event.target.value)} placeholder="Thông tin thêm về nhu cầu thuê (không bắt buộc)"/></label><button className="button button-primary" disabled={busy || !rooms.length || !roomId}><Icon name="contract" size={16}/>{busy ? 'Đang gửi…' : 'Gửi yêu cầu thuê'}</button>{!rooms.length&&<small className="workflow-help">Hiện chưa có phòng nào được đánh dấu là còn trống.</small>}</form>
    </section>}
    {error&&<div className="alert alert-error"><Icon name="incident" size={16}/>{error}</div>}
    <section className="content-card workflow-card"><div className="card-heading"><div><h2>{isTenant?'Yêu cầu của tôi':'Hộp thư yêu cầu thuê'}</h2><p>{requests.length} yêu cầu · yêu cầu mới là thông báo cho chủ nhà trong ứng dụng</p></div><span className="module-count">{requests.filter(row=>row.status==='PENDING').length} đang chờ</span></div>
      {requests.length ? <div className="workflow-list">{requests.map(row=><article className="workflow-row" key={row.requestId}><span className="workflow-icon"><Icon name="contract" size={18}/></span><div className="workflow-main"><b>Phòng {row.roomId}</b><span>Tài khoản {row.username} · muốn bắt đầu {date(row.requestedStartDate)}</span>{row.message&&<p>{row.message}</p>}<small>Gửi lúc {row.createdAt ? new Intl.DateTimeFormat('vi-VN',{dateStyle:'short',timeStyle:'short'}).format(new Date(row.createdAt)) : '—'}</small></div><span className={`status-pill status-${String(row.status).toLowerCase()}`}>{statusLabel(row.status)}</span>{!isTenant&&row.status==='PENDING'&&<div className="workflow-buttons"><button className="button button-secondary" disabled={busy} onClick={()=>decide(row,'REJECTED')}>Từ chối</button><button className="button button-primary" disabled={busy} onClick={()=>decide(row,'APPROVED')}>Chấp thuận</button></div>}</article>)}</div> : <div className="workflow-empty"><Icon name="contract" size={22}/><span>Chưa có yêu cầu thuê nào.</span></div>}
    </section>
  </div>
}
