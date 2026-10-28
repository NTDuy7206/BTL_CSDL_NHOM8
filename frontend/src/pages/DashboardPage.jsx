import { useEffect, useState } from 'react'
import StatCard from '../components/StatCard.jsx'
import Icon from '../components/Icon.jsx'
import { visibleModules } from '../data/permissions.js'
import DemoDataNotice from '../components/DemoDataNotice.jsx'

const highlights = [
  { key: 'rooms', label: 'Tổng số phòng', icon: 'building', accent: 'blue', note: 'Phòng & mặt bằng' },
  { key: 'vacant', label: 'Phòng đang trống', icon: 'check', accent: 'green', note: 'Theo tình trạng phòng' },
  { key: 'rented', label: 'Phòng đã thuê', icon: 'contract', accent: 'violet', note: 'Theo tình trạng phòng' },
  { key: 'tenants', label: 'Khách thuê', icon: 'users', accent: 'violet', note: 'Hồ sơ người thuê' },
  { key: 'contracts', label: 'Hợp đồng', icon: 'contract', accent: 'orange', note: 'Đang quản lý' },
  { key: 'invoices', label: 'Hóa đơn', icon: 'invoice', accent: 'green', note: 'Theo dõi thu phí' },
]

function normalizeStatus(value) {
  return String(value || '').trim().toLocaleLowerCase('vi').replace(/đ/g, 'd').normalize('NFD').replace(/[\u0300-\u036f]/g, '')
}

function roomStatusGroup(value) {
  const status = normalizeStatus(value)
  if (!status) return null
  if (['unoccupied', 'not occupied', 'not rented', 'khong thue'].some(word => status.includes(word))) return 'vacant'
  if (['khong trong', 'not vacant'].some(word => status.includes(word))) return 'rented'
  if (['chua thue', 'trong', 'available', 'vacant', 'empty'].some(word => status.includes(word))) return 'vacant'
  if (['dang thue', 'da thue', 'cho thue', 'occupied', 'rented', 'leased', 'dang su dung', 'co khach'].some(word => status.includes(word))) return 'rented'
  return null
}

function RoomGroup({ title, kind, rooms, loading, unavailable, onNavigate }) {
  const shown = rooms.slice(0, 6)
  return <section className={`content-card room-group room-group-${kind}`}>
    <div className="card-heading room-group-heading">
      <div><h2>{title}</h2><p>{loading ? 'Đang tải danh sách phòng…' : unavailable ? 'Dữ liệu chưa kết nối' : `${rooms.length} phòng`}</p></div>
      <span className={`room-group-count room-count-${kind}`}>{loading ? '…' : rooms.length}</span>
    </div>
    {shown.length ? <div className="room-list">{shown.map(room => <article className="room-list-item" key={room.roomId}>
      <span className="room-list-icon"><Icon name="building" size={17}/></span>
      <div className="room-list-main"><b>{room.roomId || 'Phòng chưa có mã'}</b><span>{[room.roomType, room.floor != null ? `Tầng ${room.floor}` : null, room.area != null ? `${room.area} m²` : null].filter(Boolean).join(' · ') || room.status}</span></div>
      <span className="room-list-price">{room.basePrice != null ? `${Number(room.basePrice).toLocaleString('vi-VN')} ₫` : ''}</span>
    </article>)}</div> : <div className="room-empty-state">{loading ? 'Đang tải…' : unavailable ? 'Không tải được danh sách phòng. Hãy kiểm tra kết nối API.' : 'Chưa có phòng thuộc nhóm này.'}</div>}
    {rooms.length > shown.length && <p className="room-list-more">Hiển thị {shown.length} trong {rooms.length} phòng</p>}
    <button className="text-action room-group-action" onClick={() => onNavigate('rooms')}>Quản lý phòng <Icon name="chevron" size={15}/></button>
  </section>
}

export default function DashboardPage({ request, user, onNavigate }) {
  const modules=visibleModules(user?.role)
  const [counts, setCounts] = useState({})
  const [rooms, setRooms] = useState([])
  const [busy, setBusy] = useState(true)
  const [dataVersion,setDataVersion]=useState(0)

  useEffect(() => {
    let mounted = true
    Promise.all(modules.map(async module => {
      try {
        const result = await request(module.path)
        return [module.key, Array.isArray(result) ? result : null]
      } catch {
        return [module.key, null]
      }
    })).then(results => {
      if (!mounted) return
      setCounts(Object.fromEntries(results.map(([key, rows]) => [key, rows === null ? null : rows.length])))
      setRooms(results.find(([key]) => key === 'rooms')?.[1] || [])
    }).finally(() => {
      if (mounted) setBusy(false)
    })
    return () => { mounted = false }
  }, [request,user?.role,dataVersion])

  const valid = Object.values(counts).filter(value => value !== null).length
  const vacantCount = rooms.filter(room => roomStatusGroup(room.status) === 'vacant').length
  const rentedCount = rooms.filter(room => roomStatusGroup(room.status) === 'rented').length
  const vacantRooms = rooms.filter(room => roomStatusGroup(room.status) === 'vacant')
  const rentedRooms = rooms.filter(room => roomStatusGroup(room.status) === 'rented')
  const values = { ...counts, vacant: counts.rooms === null ? null : vacantCount, rented: counts.rooms === null ? null : rentedCount }

  return <div className="page-stack">
    <DemoDataNotice user={user} request={request} onDone={()=>setDataVersion(v=>v+1)}/>
    <section className="welcome-banner"><div><span className="welcome-tag">TỔNG QUAN HỆ THỐNG</span><h2>Xin chào, {user?.username || 'bạn'} <span>✦</span></h2><p>Theo dõi nhanh hoạt động và dữ liệu của tòa nhà hôm nay.</p></div><div className="welcome-graphic"><div className="graphic-circle circle-one"/><div className="graphic-circle circle-two"/><Icon name="building" size={82} strokeWidth={1.2}/></div></section>
    <div className="section-heading"><div><h2>Chỉ số chính</h2><p>Số liệu được lấy trực tiếp từ các phân hệ</p></div><button className="text-action" onClick={() => onNavigate('rooms')}>Xem phòng <Icon name="chevron" size={15}/></button></div>
    <section className="stat-grid">{highlights.filter(item=>user?.role==='ADMIN'||item.key!=='tenants').map(item => <StatCard key={item.key} {...item} value={busy ? '…' : values[item.key] === null ? '—' : values[item.key] ?? 0}/>)}</section>
    <div className="section-heading room-section-heading"><div><h2>Tình trạng phòng</h2><p>Danh sách được phân nhóm theo tình trạng lưu trong dữ liệu phòng</p></div></div>
    <section className="room-groups-grid">
      <RoomGroup title="Phòng đang trống" kind="vacant" rooms={vacantRooms} loading={busy} unavailable={counts.rooms === null} onNavigate={onNavigate}/>
      <RoomGroup title="Phòng đã thuê" kind="rented" rooms={rentedRooms} loading={busy} unavailable={counts.rooms === null} onNavigate={onNavigate}/>
    </section>
    <div className="dashboard-lower"><section className="content-card module-card"><div className="card-heading"><div><h2>Phân hệ quản lý</h2><p>Truy cập nhanh đến các khu vực dữ liệu</p></div><span className="module-count">{modules.length} phân hệ</span></div><div className="module-grid">{modules.map((module, index) => <button className="module-tile" key={module.key} onClick={() => onNavigate(module.key)}><span className={`tile-icon tile-color-${index % 5}`}><Icon name={module.icon} size={19}/></span><span className="tile-text"><b>{module.title}</b><small>{counts[module.key] === null ? 'Chưa kết nối' : busy ? 'Đang tải' : `${counts[module.key] ?? 0} bản ghi`}</small></span><Icon name="chevron" className="tile-chevron" size={17}/></button>)}</div></section>
      <section className="content-card system-card"><div className="card-heading"><div><h2>Tình trạng kết nối</h2><p>Trạng thái truy cập các API dữ liệu</p></div><span className={`connection-mark ${valid === modules.length ? 'online' : ''}`}><i/></span></div><div className="connection-summary"><div className="connection-number">{busy ? '…' : `${valid}/${modules.length}`}</div><div><b>{valid === modules.length ? 'Đang hoạt động' : 'Đã kiểm tra'}</b><span>phân hệ có phản hồi</span></div></div><div className="progress-track"><div className="progress-value" style={{ width: `${busy ? 0 : valid / modules.length * 100}%` }}/></div><div className="system-foot"><span><i className="dot dot-green"/> Spring Boot API</span><span>{window.location.host}</span></div><div className="system-foot"><span><i className="dot dot-blue"/> Database</span><span>MySQL</span></div><div className="system-tip"><Icon name="incident" size={16}/><span>Số phòng trống và đã thuê được phân loại theo giá trị tình trạng trong bảng phòng.</span></div></section>
    </div>
  </div>
}
