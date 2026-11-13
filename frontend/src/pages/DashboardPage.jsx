import { useEffect, useState } from 'react'
import KeywordSearch from '../components/KeywordSearch.jsx'
import ScopeNotice from '../components/ScopeNotice.jsx'
import{matchesKeyword}from '../data/search.js'
import StatCard from '../components/StatCard.jsx'
import Icon from '../components/Icon.jsx'
import { visibleModules } from '../data/permissions.js'
import {contractsByRoom,roomGroups} from '../data/roomGroups.js'
import RoomRentalPeriod from '../components/RoomRentalPeriod.jsx'


const highlights = [
  { key: 'rooms', label: 'Tổng số phòng', icon: 'building', accent: 'blue' },
  { key: 'vacant', label: 'Phòng đang trống', icon: 'check', accent: 'green' },
  { key: 'rented', label: 'Phòng đang thuê', icon: 'contract', accent: 'violet' },
  { key: 'tenants', label: 'Khách thuê', icon: 'users', accent: 'violet' },
  { key: 'contracts', label: 'Hợp đồng đang hiệu lực', icon: 'contract', accent: 'orange' },
  { key: 'invoices', label: 'Hóa đơn', icon: 'invoice', accent: 'green' },
]

function RoomGroup({ title, kind, rooms, loading, unavailable, rentalContracts, onNavigate,target='rooms',actionLabel='Xem phòng' }) {
  const shown = rooms
  return <section className={`content-card room-group room-group-${kind}`}>
    <div className="card-heading room-group-heading">
      <div><h2>{title}</h2><p>{loading ? 'Đang tải danh sách phòng…' : unavailable ? 'Dữ liệu chưa kết nối' : `${rooms.length} phòng`}</p></div>
      <span className={`room-group-count room-count-${kind}`}>{loading ? '…' : rooms.length}</span>
    </div>
    {shown.length ? <div className="room-list room-list-scroll" tabIndex="0" role="region" aria-label={title}>{shown.map(room => <article className="room-list-item" key={room.roomId}>
      <span className="room-list-icon"><Icon name="building" size={17}/></span>
      <div className="room-list-main"><b>{room.roomId || 'Phòng chưa có mã'}</b><span>{[room.landlordName ? `Chủ nhà: ${room.landlordName}` : null, room.availableForRent===false&&!room.occupied?'Chưa mở cho thuê':null, room.roomType, room.floor != null ? `Tầng ${room.floor}` : null, room.area != null ? `${room.area} m²` : null].filter(Boolean).join(' · ') || room.status}</span>{rentalContracts&&<RoomRentalPeriod contracts={rentalContracts.get(room.roomId)} unavailable={unavailable}/>}</div>
      <span className="room-list-price">{room.basePrice != null ? `${Number(room.basePrice).toLocaleString('vi-VN')} ₫/đêm` : ''}</span>
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
  const [rentalRequests,setRentalRequests]=useState([])
  const [contractsUnavailable,setContractsUnavailable]=useState(false)
  const [busy, setBusy] = useState(true)


  useEffect(() => {
    let mounted = true
    const update=()=>Promise.all([...modules,{key:'rentalRequests',path:'/api/contract-requests'}].map(async module => {
      try {
        const result = await request(module.path)
        return [module.key, Array.isArray(result) ? result : null]
      } catch {
        return [module.key, null]
      }
    })).then(results => {
      if (!mounted) return
      setRentalRequests(results.find(([key])=>key==='rentalRequests')?.[1]??null)
      setCounts(Object.fromEntries(results.filter(([key])=>key!=='rentalRequests').map(([key, rows]) => [key, rows === null ? null : rows.length])))
      setRooms(results.find(([key]) => key === 'rooms')?.[1] || [])
      const own=results.find(([key])=>key==='contracts')?.[1];setContracts(own||[]);setContractsUnavailable(own==null)
    }).finally(() => {
      if (mounted) setBusy(false)
    })
    update();const timer=setInterval(update,15000);const onFocus=()=>{if(!document.hidden)update()};window.addEventListener('focus',onFocus);document.addEventListener('visibilitychange',onFocus)
    return () => { mounted = false;clearInterval(timer);window.removeEventListener('focus',onFocus);document.removeEventListener('visibilitychange',onFocus) }
  }, [request,user?.role])

  const todayVN=new Intl.DateTimeFormat('sv-SE',{timeZone:'Asia/Ho_Chi_Minh'}).format(new Date());
  const upcoming=contracts.filter(c=>!c.actualCheckOut&&(c.actualCheckIn||c.startDate)>todayVN);
  const pending=(rentalRequests||[]).filter(r=>r.status==='PENDING');
  const valid = Object.values(counts).filter(value => value !== null).length
  const {vacant:vacantRooms,rented:rentedRooms,unknown,activeContracts,conflictingRooms}=roomGroups(rooms,contracts,user?.role,todayVN);
  const adminRentalContracts=user?.role==='ADMIN'?contractsByRoom(activeContracts):null
  const occupancyUnavailable=counts.rooms===null||contractsUnavailable;
  const values={...counts,contracts:occupancyUnavailable?null:activeContracts.length,vacant:(user?.role==='ADMIN'?occupancyUnavailable:counts.rooms===null)?null:vacantRooms.length,rented:occupancyUnavailable?null:rentedRooms.length};

  return <div className="page-stack">
    <section className="content-card workflow-card"><KeywordSearch value={keyword} onChange={setKeyword} label="Tìm phòng trong tổng quan" placeholder="Nhập một phần mã hoặc loại phòng…"/></section>
    {user?.role==='ADMIN'&&!busy&&rooms.length===0&&<ScopeNotice moduleKey="contracts" request={request} onNavigate={onNavigate}/>}
    <section className="welcome-banner"><div><span className="welcome-tag">TỔNG QUAN HỆ THỐNG</span><h2>Xin chào, {user?.username || 'bạn'} <span>✦</span></h2></div><div className="welcome-graphic"><div className="graphic-circle circle-one"/><div className="graphic-circle circle-two"/><Icon name="building" size={82} strokeWidth={1.2}/></div></section>
    <div className="section-heading"><div><h2>Chỉ số chính</h2></div><button className="text-action" onClick={() => onNavigate('rooms')}>Xem phòng <Icon name="chevron" size={15}/></button></div>
    <section className="stat-grid">{highlights.filter(item=>user?.role==='ADMIN'||item.key!=='tenants').map(item => <StatCard key={item.key} {...item} label={item.key==='rented'&&user?.role==='USER'?'Phòng tôi đang thuê':item.label} value={busy ? '…' : values[item.key] === null ? '—' : values[item.key] ?? 0}/>)}</section>
    <section className="stat-grid"><StatCard label="Yêu cầu thuê chờ duyệt" icon="contract" accent="orange" value={busy?'…':rentalRequests===null?'—':pending.length}/><StatCard label="Hợp đồng sắp bắt đầu" icon="contract" accent="blue" value={busy?'…':contractsUnavailable?'—':upcoming.length}/></section>
    <div className="section-heading room-section-heading"><div><h2>Tình trạng phòng</h2></div></div>
    {user?.role==='ADMIN'&&!busy&&!occupancyUnavailable&&(unknown.length>0||conflictingRooms.length>0)&&<section className="content-card">{unknown.length>0&&<div className="alert alert-error">Có hợp đồng thiếu ngày bắt đầu/kết thúc. Vui lòng kiểm tra tại Hợp đồng; các phòng này chưa được tính là trống.</div>}{conflictingRooms.length>0&&<div className="alert alert-error">Có nhiều hợp đồng đồng thời hiệu lực tại phòng: {conflictingRooms.map(r=>r.roomId).join(', ')}. Cần đối soát hợp đồng, không tự xóa dữ liệu.</div>}</section>}
    <section className="room-groups-grid">
      <RoomGroup title="Phòng đang trống" kind="vacant" rooms={vacantRooms.filter(r=>matchesKeyword(r,keyword))} loading={busy} unavailable={user?.role==='ADMIN'?occupancyUnavailable:counts.rooms===null} onNavigate={onNavigate}/>
      <RoomGroup title={user?.role==='USER'?'Phòng tôi đang thuê':'Phòng đã thuê'} kind="rented" target={user?.role==='USER'?'contracts':'rooms'} actionLabel={user?.role==='USER'?'Xem hợp đồng của tôi':'Quản lý phòng'} rooms={rentedRooms.filter(r=>matchesKeyword(r,keyword))} rentalContracts={adminRentalContracts} loading={busy} unavailable={occupancyUnavailable} onNavigate={onNavigate}/>
    </section>
    <div className="dashboard-lower"><section className="content-card module-card"><div className="card-heading"><div><h2>Phân hệ quản lý</h2></div><span className="module-count">{modules.length} phân hệ</span></div><div className="module-grid">{modules.map((module, index) => <button className="module-tile" key={module.key} onClick={() => onNavigate(module.key)}><span className={`tile-icon tile-color-${index % 5}`}><Icon name={module.icon} size={19}/></span><span className="tile-text"><b>{module.title}</b><small>{counts[module.key] === null ? 'Chưa kết nối' : busy ? 'Đang tải' : `${counts[module.key] ?? 0} bản ghi${module.key==='contracts'?' · gồm lịch sử':''}`}</small></span><Icon name="chevron" className="tile-chevron" size={17}/></button>)}</div></section>
      <section className="content-card system-card"><div className="card-heading"><div><h2>Tình trạng kết nối</h2></div><span className={`connection-mark ${valid === modules.length ? 'online' : ''}`}><i/></span></div><div className="connection-summary"><div className="connection-number">{busy ? '…' : `${valid}/${modules.length}`}</div><div><b>{valid === modules.length ? 'Đang hoạt động' : 'Đã kiểm tra'}</b><span>phân hệ có phản hồi</span></div></div><div className="progress-track"><div className="progress-value" style={{ width: `${busy ? 0 : valid / modules.length * 100}%` }}/></div><div className="system-foot"><span><i className="dot dot-green"/> Spring Boot API</span><span>{window.location.host}</span></div><div className="system-foot"><span><i className="dot dot-blue"/> Database</span><span>MySQL</span></div></section>
    </div>
  </div>
}
