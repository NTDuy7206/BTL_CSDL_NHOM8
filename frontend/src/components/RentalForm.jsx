import {useEffect,useState} from 'react'
import {requireRows} from '../data/listResponse.js'
import {available,isVipRoom} from '../data/permissions.js'
import {money} from '../data/finance.js'
const blank={fullName:'',cccd:'',dateOfBirth:'',permanentAddress:'',phoneNumber:''}
const today=()=>new Date().toLocaleDateString('en-CA',{timeZone:'Asia/Ho_Chi_Minh'})
export default function RentalForm({request,onSaved,notify,initialRoomId='',onCancel}){
 const[rooms,setRooms]=useState([]),[person,setPerson]=useState(blank),[roomId,setRoomId]=useState(initialRoomId),[startDate,setStart]=useState(''),[endDate,setEnd]=useState(''),[message,setMessage]=useState(''),[busy,setBusy]=useState(false),[loading,setLoading]=useState(true),[error,setError]=useState('');
 useEffect(()=>{let active=true;setLoading(true);request('/api/room-listings').then(r=>{if(active)setRooms(requireRows(r))}).catch(e=>{if(active)setError(e.message)}).finally(()=>{if(active)setLoading(false)});request('/api/rentals/me').then(t=>{if(active&&t)setPerson(current=>Object.fromEntries(Object.keys(blank).map(key=>[key,current[key]||t[key]||''])))}).catch(()=>{});return()=>{active=false}},[request]);
 const shownRooms=rooms.filter(available),room=shownRooms.find(r=>r.roomId===roomId);
 const nights=startDate&&endDate>startDate
  ? (Date.parse(endDate)-Date.parse(startDate))/86400000 : 0
 const totalRent=room&&nights>0?Number(room.basePrice)*nights:null
 const vip=isVipRoom(room), [pricing,setPricing]=useState(null), [deposit,setDeposit]=useState(null)
 const quoteKey=vip&&startDate&&endDate>startDate?new URLSearchParams({roomId,start:startDate,end:endDate}).toString():''
 const quote=pricing?.key===quoteKey?pricing.quote:null
 const depositId=quote&&deposit?.key===quoteKey&&deposit.amount===quote.depositAmount?deposit.id:null

 useEffect(()=>{
  if(!quoteKey)return
  let active=true
  request('/api/contract-requests/vip-deposit?'+quoteKey).then(result=>{
   if(active)setPricing({key:quoteKey,quote:result})
  }).catch(err=>{if(active)setPricing({key:quoteKey,error:err.message})})
  return ()=>{active=false}
 },[quoteKey,request])

 async function simulateDeposit(){
  if(busy||!quote||depositId)return
  setBusy(true);setError('')
  try{
   const result=await request('/api/contract-requests/vip-deposit',{method:'POST',body:JSON.stringify({roomId,start:startDate,end:endDate,amount:quote.depositAmount})})
   setDeposit({key:quoteKey,id:result.depositId,amount:quote.depositAmount})
   notify('Đã đặt cọc giả lập. Bạn có thể gửi yêu cầu thuê.')
  }catch(err){setError(err.message)}finally{setBusy(false)}
 }

 async function submit(e){e.preventDefault();if(endDate<=startDate){setError('Ngày đi phải sau ngày bắt đầu.');return}if(busy||!room||(vip&&!depositId))return;setBusy(true);setError('');try{const result=await request('/api/contract-requests',{method:'POST',body:JSON.stringify({tenant:person,roomId,requestedStartDate:startDate,requestedEndDate:endDate,message,expectedNightlyRate:room.basePrice,vipDepositId:vip?depositId:undefined})});notify('Đã gửi yêu cầu thuê của tài khoản đang đăng nhập cho chủ nhà.');if(onSaved)await onSaved(result)}catch(e){setError(e.message)}finally{setBusy(false)}}
 return <section className="content-card workflow-card"><h2>Đăng ký thuê · {initialRoomId}</h2>{onCancel&&<button className="button button-secondary" onClick={onCancel}>Quay lại chi tiết phòng</button>}{error&&<div role="alert" className="alert alert-error">{error}</div>}{loading?<p>Đang tải…</p>:<form className="rental-form" onSubmit={submit}>

 {[['fullName','Họ và tên','text',50],['cccd','CCCD','text',20],['dateOfBirth','Ngày sinh','date'],['phoneNumber','Số điện thoại','tel',10],['permanentAddress','Địa chỉ thường trú','text',200]].map(([key,label,type,max])=><label className="form-field" key={key}><span>{label}</span><input required type={type} maxLength={max} max={key==='dateOfBirth'?today():undefined} pattern={key==='phoneNumber'?'[0-9]{10}':undefined} value={person[key]||''} onChange={e=>setPerson({...person,[key]:e.target.value})}/></label>)}

 <label className="form-field"><span>Chọn phòng thuê · chủ nhà</span><select required disabled={busy} value={roomId} onChange={e=>setRoomId(e.target.value)}><option value="">Chọn phòng</option>{shownRooms.map(r=><option key={r.roomId} value={r.roomId}>{r.roomId} · Chủ nhà: {r.landlordName} · {r.roomType} · {Number(r.basePrice||0).toLocaleString('vi-VN')} ₫/đêm</option>)}</select>{!shownRooms.length&&<small>Chưa có phòng trống đã được chủ nhà khai báo.</small>}</label>
 <label className="form-field"><span>Ngày đến</span><input required disabled={busy} type="date" min={today()} value={startDate} onChange={e=>setStart(e.target.value)}/></label><label className="form-field"><span>Ngày đi</span><input required disabled={busy} type="date" min={startDate} value={endDate} onChange={e=>setEnd(e.target.value)}/></label>
 <label className="form-field"><span>Lời nhắn cho chủ nhà</span><textarea maxLength={500} value={message} onChange={e=>setMessage(e.target.value)}/></label>
 <div className="rental-preview"><h3>Nội dung hợp đồng</h3><dl><dt>Khách thuê</dt><dd>{person.fullName||'Chưa nhập'}</dd><dt>Ngày sinh</dt><dd>{person.dateOfBirth||'—'}</dd><dt>CCCD / Điện thoại</dt><dd>{person.cccd||'—'} / {person.phoneNumber||'—'}</dd><dt>Chủ nhà nhận yêu cầu</dt><dd>{room?.landlordName||'Chưa chọn'}</dd><dt>Phòng</dt><dd>{room?room.roomId+' · '+room.roomType:'Chưa chọn'}</dd><dt>Giá mỗi đêm</dt><dd>{room?Number(room.basePrice||0).toLocaleString('vi-VN')+' ₫/đêm':'—'}</dd><dt>Thời hạn thuê</dt><dd>{startDate||'—'} → {endDate||'—'}</dd><dt>Số đêm</dt><dd>{nights||'—'}</dd><dt>Tổng tiền phòng</dt><dd>{totalRent==null?'—':money(totalRent)}</dd></dl></div>
 {vip&&<div className="rental-preview"><h3>Đặt cọc phòng VIP</h3>{quote?<><dl><dt>Tổng tiền phòng</dt><dd>{money(quote.totalRent)}</dd><dt>Cọc trước 50%</dt><dd>{money(quote.depositAmount)}</dd><dt>Còn lại khi hết hạn</dt><dd>{money(quote.balance)}</dd></dl>{depositId?<p role="status">Đã đặt cọc giả lập.</p>:<button type="button" className="button button-secondary" disabled={busy} onClick={simulateDeposit}>Đặt cọc giả lập {money(quote.depositAmount)}</button>}</>:<p>{!quoteKey?'Chọn thời hạn thuê để xem số tiền cọc.':pricing?.key===quoteKey?pricing.error:'Đang tính tiền cọc…'}</p>}</div>}
 <button className="button button-primary" disabled={busy||!room||!nights||(vip&&!depositId)}>{busy?'Đang lưu…':'Gửi đăng ký hợp đồng'}</button></form>}</section>
}
