import { useEffect, useState } from 'react'
import KeywordSearch from '../components/KeywordSearch.jsx'
import ScopeNotice from '../components/ScopeNotice.jsx'
import{matchesKeyword}from '../data/search.js'
import StatCard from '../components/StatCard.jsx'
import Icon from '../components/Icon.jsx'
import { visibleModules } from '../data/permissions.js'
import {roomGroups} from '../data/roomGroups.js'
import DemoDataNotice from '../components/DemoDataNotice.jsx'

const highlights = [
  { key: 'rooms', label: 'Tổng số phòng', icon: 'building', accent: 'blue', note: 'Phòng & mặt bằng' },
  { key: 'vacant', label: 'Phòng đang trống', icon: 'check', accent: 'green', note: 'Không có hợp đồng đang hiệu lực' },
  { key: 'rented', label: 'Phòng đã thuê', icon: 'contract', accent: 'violet', note: 'Có hợp đồng đang hiệu lực' },
  { key: 'tenants', label: 'Khách thuê', icon: 'users', accent: 'violet', note: 'Hồ sơ người thuê' },
  { key: 'contracts', label: 'Hợp đồng đang hiệu lực', icon: 'contract', accent: 'orange', note: 'Đã trừ hợp đồng hết hạn / trả phòng' },
  { key: 'invoices', label: 'Hóa đơn', icon: 'invoice', accent: 'green', note: 'Theo dõi thu phí' },
]

function RoomGroup({ title, kind, rooms, loading, unavailable, onNavigate,target='rooms',actionLabel='Xem phòng' }) {
  const shown = rooms
  return <section className={`content-card room-group room-group-${kind}`}>
    <div className="card-heading room-group-heading">
      <div><h2>{title}</h2><p>{loading ? 'Đang tải danh sách phòng…' : unavailable ? 'Dữ liệu chưa kết nối' : `${rooms.length} phòng`}</p></div>
      <span className={`room-group-count room-count-${kind}`}>{loading ? '…' : rooms.length}</span>
    </div>
    {shown.length ? <div className="room-list room-list-scroll" tabIndex="0" role="region" aria-label={title}>{shown.map(room => <article className="room-list-item" key={room.roomId}>
      <span className="room-list-icon"><Icon name="building" size={17}/></span>
      <div className="room-list-main"><b>{room.roomId || 'Phòng chưa có mã'}</b><span>{[room.landlordName ? `Chủ nhà: ${room.landlordName}` : null, room.roomType, room.floor != null ? `Tầng ${room.floor}` : null, room.area != null ? `${room.area} m²` : null].filter(Boolean).join(' · ') || room.status}</span></div>
      <span className="room-list-price">{room.basePrice != null ? `${Number(room.basePrice).toLocaleString('vi-VN')} ₫` : ''}</span>
    </article>)}</div> : <div className="room-empty-state">{loading ? 'Đang tải…' : unavailable ? 'Không tải được danh sách phòng. Hãy kiểm tra kết nối API.' : 'Chưa có phòng thuộc nhóm này.'}</div>}
    {rooms.length > shown.length && <p className="room-list-more">Hiển thị {shown.length} trong {rooms.length} phòng</p>}
    <button className="text-action room-group-action" onClick={() => onNavigate(target)}>{actionLabel} <Icon name="chevron" size={15}/></button>
  </section>
}

export default function DashboardPage({ request, user, onNavigate }) {
  const[keyword,setKeyword]=useState('')
  const modules=visibleModules(user?.role)
  const [counts, setCounts] = useState({})
  const [rooms, setRooms] = useState([])
  const [contracts,setContracts]=useState([])
  const [contractsUnavailable,setContractsUnavailable]=useState(false)
  const [busy, setBusy] = useState(true)
  const [dataVersion,setDataVersion]=useState(0)

  useEffect(() => {
    let mounted = true
    const update=()=>Promise.all(modules.map(async module => {
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
      const own=results.find(([key])=>key==='contracts')?.[1];setContracts(own||[]);setContractsUnavailable(own==null)
    }).finally(() => {
      if (mounted) setBusy(false)
    })
    update();const timer=setInterval(update,60000)
    return () => { mounted = false;clearInterval(timer) }
  }, [request,user?.role,dataVersion])

  const valid = Object.values(counts).filter(value => value !== null).length
  const {vacant:vacantRooms,rented:rentedRooms,unknown,activeContracts,conflictingRooms}=roomGroups(rooms,contracts,user?.role,new Date().toLocaleDateString('en-CA',{timeZone:'Asia/Ho_Chi_Minh'}));
  const occupancyUnavailable=counts.rooms===null||contractsUnavailable;
  const values={...counts,contracts:occupancyUnavailable?null:activeContracts.length,vacant:(user?.role==='ADMIN'?occupancyUnavailable:counts.rooms===null)?null:vacantRooms.length,rented:occupancyUnavailable?null:rentedRooms.length};

  return <div className="page-stack">
    <section className="content-card workflow-card"><KeywordSearch value={keyword} onChange={setKeyword} label="Tìm phòng trong tổng quan" placeholder="Nhập một phần mã hoặc loại phòng…"/></section>
    {user?.role==='ADMIN'&&!busy&&rooms.length===0&&<ScopeNotice moduleKey="contracts" request={request} onNavigate={onNavigate}/>}
    <DemoDataNotice user={user} request={request} onDone={()=>setDataVersion(v=>v+1)}/>
    <section className="welcome-banner"><div><span className="welcome-tag">TỔNG QUAN HỆ THỐNG</span><h2>Xin chào, {user?.username || 'bạn'} <span>✦</span></h2><p>Theo dõi nhanh hoạt động và dữ liệu của tòa nhà hôm nay.</p></div><div className="welcome-graphic"><div className="graphic-circle circle-one"/><div className="graphic-circle circle-two"/><Icon name="building" size={82} strokeWidth={1.2}/></div></section>
    <div className="section-heading"><div><h2>Chỉ số chính</h2><p>Số liệu được lấy trực tiếp từ các phân hệ</p></div><button className="text-action" onClick={() => onNavigate('rooms')}>Xem phòng <Icon name="chevron" size={15}/></button></div>
    <section className="stat-grid">{highlights.filter(item=>user?.role==='ADMIN'||item.key!=='tenants').map(item => <StatCard key={item.key} {...item} label={item.key==='rented'&&user?.role==='USER'?'Phòng tôi đang thuê':item.label} note={user?.role==='USER'&&item.key==='rented'?'Theo hợp đồng của tài khoản hiện tại':user?.role==='USER'&&item.key==='vacant'?'Theo trạng thái phòng được niêm yết':item.note} value={busy ? '…' : values[item.key] === null ? '—' : values[item.key] ?? 0}/>)}</section>
    <div className="section-heading room-section-heading"><div><h2>Tình trạng phòng</h2><p>{user?.role==='USER'?'Phòng trống để tham khảo; phòng đang thuê chỉ lấy từ hợp đồng của bạn.':'Danh sách phòng thuộc chủ nhà đang đăng nhập.'}</p></div></div>
    {user?.role==='ADMIN'&&!busy&&!occupancyUnavailable&&<section className="content-card"><p>{rooms.length} phòng = {vacantRooms.length} phòng trống + {rentedRooms.length} phòng đã thuê{unknown.length?` + ${unknown.length} phòng chưa xác định`:''}. {activeContracts.length} hợp đồng đang hiệu lực / {contracts.length} hợp đồng trong lịch sử.</p><p>Phòng trống chưa cấu hình hoặc đang bảo trì vẫn cần được chủ nhà hoàn thiện trước khi mở đăng ký thuê.</p>{unknown.length>0&&<div className="alert alert-error">Có hợp đồng thiếu ngày bắt đầu/kết thúc. Vui lòng kiểm tra tại Hợp đồng; các phòng này chưa được tính là trống.</div>}{conflictingRooms.length>0&&<div className="alert alert-error">Có nhiều hợp đồng đồng thời hiệu lực tại phòng: {conflictingRooms.map(r=>r.roomId).join(', ')}. Cần đối soát hợp đồng, không tự xóa dữ liệu.</div>}</section>}
    <section className="room-groups-grid">
      <RoomGroup title="Phòng đang trống" kind="vacant" rooms={vacantRooms.filter(r=>matchesKeyword(r,keyword))} loading={busy} unavailable={user?.role==='ADMIN'?occupancyUnavailable:counts.rooms===null} onNavigate={onNavigate}/>
      <RoomGroup title={user?.role==='USER'?'Phòng tôi đang thuê':'Phòng đã thuê'} kind="rented" target={user?.role==='USER'?'contracts':'rooms'} actionLabel={user?.role==='USER'?'Xem hợp đồng của tôi':'Quản lý phòng'} rooms={rentedRooms.filter(r=>matchesKeyword(r,keyword))} loading={busy} unavailable={occupancyUnavailable} onNavigate={onNavigate}/>
    </section>
    <div className="dashboard-lower"><section className="content-card module-card"><div className="card-heading"><div><h2>Phân hệ quản lý</h2><p>Truy cập nhanh đến các khu vực dữ liệu</p></div><span className="module-count">{modules.length} phân hệ</span></div><div className="module-grid">{modules.map((module, index) => <button className="module-tile" key={module.key} onClick={() => onNavigate(module.key)}><span className={`tile-icon tile-color-${index % 5}`}><Icon name={module.icon} size={19}/></span><span className="tile-text"><b>{module.title}</b><small>{counts[module.key] === null ? 'Chưa kết nối' : busy ? 'Đang tải' : `${counts[module.key] ?? 0} bản ghi${module.key==='contracts'?' · gồm lịch sử':''}`}</small></span><Icon name="chevron" className="tile-chevron" size={17}/></button>)}</div></section>
      <section className="content-card system-card"><div className="card-heading"><div><h2>Tình trạng kết nối</h2><p>Trạng thái truy cập các API dữ liệu</p></div><span className={`connection-mark ${valid === modules.length ? 'online' : ''}`}><i/></span></div><div className="connection-summary"><div className="connection-number">{busy ? '…' : `${valid}/${modules.length}`}</div><div><b>{valid === modules.length ? 'Đang hoạt động' : 'Đã kiểm tra'}</b><span>phân hệ có phản hồi</span></div></div><div className="progress-track"><div className="progress-value" style={{ width: `${busy ? 0 : valid / modules.length * 100}%` }}/></div><div className="system-foot"><span><i className="dot dot-green"/> Spring Boot API</span><span>{window.location.host}</span></div><div className="system-foot"><span><i className="dot dot-blue"/> Database</span><span>MySQL</span></div><div className="system-tip"><Icon name="incident" size={16}/><span>Thống kê của chủ nhà dùng ngày hiệu lực và ngày nhận/trả thực tế của hợp đồng. Hợp đồng hết hạn vẫn được giữ trong lịch sử.</span></div></section>
    </div>
  </div>
}
