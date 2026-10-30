import {useCallback,useEffect,useState} from 'react'
import KeywordSearch from '../components/KeywordSearch.jsx'
import{matchesKeyword}from '../data/search.js'
import{requireRows}from '../data/listResponse.js'
import RoomCatalog from './RoomCatalog.jsx'
import RentalForm from '../components/RentalForm.jsx'
export default function ContractRequestsPage({request,role,notify,onNavigate=()=>{},route={}}){
 const isTenant=role==='USER'
 const[query,setQuery]=useState('')
 const[rows,setRows]=useState([]),[error,setError]=useState(''),[busy,setBusy]=useState(false)
 const load=useCallback(async()=>{try{setRows(requireRows(await request('/api/contract-requests')));setError('')}catch(e){setError(e.message)}},[request])
 useEffect(()=>{load()},[load])
 async function decide(row,status){setBusy(true);setError('');try{await request('/api/contract-requests/'+row.requestId+'/status',{method:'PATCH',body:JSON.stringify({status})});notify(status==='APPROVED'?'Đã duyệt, tạo hợp đồng và cập nhật trạng thái phòng.':'Đã từ chối yêu cầu.');await load()}catch(e){setError(e.message)}finally{setBusy(false)}}
 const shown=rows.filter(row=>matchesKeyword(row,query))
 return <div className="page-stack">{isTenant&&(route.apply&&route.roomId?<RentalForm key={route.roomId} initialRoomId={route.roomId} request={request} notify={notify} onCancel={()=>onNavigate('rental-requests',{roomId:route.roomId})} onSaved={async()=>{await load();onNavigate('rental-requests')}}/>:<RoomCatalog basePage="rental-requests" request={request} role={role} notify={notify} selectedRoomId={route.roomId} onOpen={id=>onNavigate('rental-requests',{roomId:id})} onBack={()=>onNavigate('rental-requests')} onRent={id=>onNavigate('rental-requests',{roomId:id,apply:true})}/>)}
 <section className="content-card workflow-card"><div className="card-heading"><div><h2>{isTenant?'Yêu cầu thuê của tôi':'Thông báo đăng ký hợp đồng'}</h2><p>{isTenant?'Chủ nhà sẽ duyệt yêu cầu và tạo hợp đồng cho bạn.':'Xem khách thuê, phòng và thời hạn trước khi chấp thuận. Hợp đồng sẽ tự được tạo.'}</p></div><button className="button button-secondary" onClick={load}>Tải lại</button></div>
 <KeywordSearch value={query} onChange={setQuery} label="Tìm yêu cầu thuê theo từ khóa"/><p>{shown.length} / {rows.length} yêu cầu</p>{error&&<div className="alert alert-error">{error}</div>}{!shown.length&&!error&&<p>{query?'Không tìm thấy yêu cầu khớp từ khóa.':'Chưa có yêu cầu thuê.'}</p>}
 <div className="workflow-list">{shown.map(row=><article key={row.requestId} className="workflow-row"><div className="workflow-main"><b>{row.tenant?.fullName||row.username} · Phòng {row.roomId}</b><span>Tài khoản: {row.username} · Thời hạn: {row.requestedStartDate} → {row.requestedEndDate||'Chưa bổ sung'}</span>{row.tenant&&<span>Ngày sinh: {row.tenant.dateOfBirth} · CCCD: {row.tenant.cccd} · Điện thoại: {row.tenant.phoneNumber}</span>}{row.message&&<p>{row.message}</p>}{row.contractId&&<strong>Hợp đồng: {row.contractId}</strong>}</div><span className={'status-pill status-'+row.status.toLowerCase()}>{row.status==='APPROVED'?'Đã duyệt':row.status==='REJECTED'?'Đã từ chối':'Đang chờ'}</span>{!isTenant&&row.status==='PENDING'&&<div className="workflow-buttons"><button className="button button-secondary" disabled={busy} onClick={()=>decide(row,'REJECTED')}>Từ chối</button><button className="button button-primary" disabled={busy} onClick={()=>decide(row,'APPROVED')}>Duyệt & tạo hợp đồng</button></div>}</article>)}</div></section></div>
}
