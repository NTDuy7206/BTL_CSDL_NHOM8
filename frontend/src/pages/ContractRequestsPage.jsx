import {useCallback,useEffect,useState} from 'react'
import RentalForm from '../components/RentalForm.jsx'
export default function ContractRequestsPage({request,role,notify}){
 const isTenant=role==='USER'
 const[rows,setRows]=useState([]),[error,setError]=useState(''),[busy,setBusy]=useState(false),[formKey,setFormKey]=useState(0)
 const load=useCallback(async()=>{try{setRows(await request('/api/contract-requests'))}catch(e){setError(e.message)}},[request])
 useEffect(()=>{load()},[load])
 async function decide(row,status){setBusy(true);setError('');try{await request('/api/contract-requests/'+row.requestId+'/status',{method:'PATCH',body:JSON.stringify({status})});notify(status==='APPROVED'?'Đã duyệt, tạo hợp đồng và cập nhật trạng thái phòng.':'Đã từ chối yêu cầu.');await load()}catch(e){setError(e.message)}finally{setBusy(false)}}
 return <div className="page-stack">{isTenant&&<RentalForm key={formKey} request={request} notify={notify} onSaved={async()=>{await load();setFormKey(k=>k+1)}}/>}
 <section className="content-card workflow-card"><div className="card-heading"><div><h2>{isTenant?'Yêu cầu thuê của tôi':'Thông báo đăng ký hợp đồng'}</h2><p>{isTenant?'Chủ nhà sẽ duyệt yêu cầu và tạo hợp đồng cho bạn.':'Xem khách thuê, phòng và thời hạn trước khi chấp thuận. Hợp đồng sẽ tự được tạo.'}</p></div><button className="button button-secondary" onClick={load}>Tải lại</button></div>
 {error&&<div className="alert alert-error">{error}</div>}{!rows.length&&<p>Chưa có yêu cầu thuê.</p>}
 <div className="workflow-list">{rows.map(row=><article key={row.requestId} className="workflow-row"><div className="workflow-main"><b>{row.tenant?.fullName||row.username} · Phòng {row.roomId}</b><span>Tài khoản: {row.username} · Thời hạn: {row.requestedStartDate} → {row.requestedEndDate||'Chưa bổ sung'}</span>{row.tenant&&<span>Ngày sinh: {row.tenant.dateOfBirth} · CCCD: {row.tenant.cccd} · Điện thoại: {row.tenant.phoneNumber}</span>}{row.message&&<p>{row.message}</p>}{row.contractId&&<strong>Hợp đồng: {row.contractId}</strong>}</div><span className={'status-pill status-'+row.status.toLowerCase()}>{row.status==='APPROVED'?'Đã duyệt':row.status==='REJECTED'?'Đã từ chối':'Đang chờ'}</span>{!isTenant&&row.status==='PENDING'&&<div className="workflow-buttons"><button className="button button-secondary" disabled={busy} onClick={()=>decide(row,'REJECTED')}>Từ chối</button><button className="button button-primary" disabled={busy} onClick={()=>decide(row,'APPROVED')}>Duyệt & tạo hợp đồng</button></div>}</article>)}</div></section></div>
}
