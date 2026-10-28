import { useEffect, useState } from 'react'
import { available } from '../data/permissions.js'
const blank={fullName:'',cccd:'',dateOfBirth:'',permanentAddress:'',phoneNumber:''}
const today=()=>new Date().toLocaleDateString('en-CA')
export default function RentalForm({request,admin=false,onSaved,notify}) {
 const [rooms,setRooms]=useState([]),[landlordId,setLandlordId]=useState(''),[tenants,setTenants]=useState([]),[accounts,setAccounts]=useState([])
 const [person,setPerson]=useState(blank),[tenantId,setTenantId]=useState(''),[account,setAccount]=useState('')
 const [roomId,setRoomId]=useState(''),[startDate,setStart]=useState(''),[endDate,setEnd]=useState(''),[deposit,setDeposit]=useState('0'),[message,setMessage]=useState('')
 const [busy,setBusy]=useState(false),[loading,setLoading]=useState(true),[error,setError]=useState('')
 useEffect(()=>{let active=true;Promise.all([request(admin?'/api/landlords/my-rooms':'/api/landlords/rooms'),admin?request('/api/tenants'):request('/api/rentals/me'),admin?request('/api/rentals/accounts'):Promise.resolve([])]).then(([r,t,a])=>{
 if(!active)return;setRooms(r);if(admin){setTenants(t);setAccounts(a)}else if(t){setPerson({...blank,...t})}
 }).catch(e=>{if(active)setError(e.message)}).finally(()=>{if(active)setLoading(false)});return()=>{active=false}},[request,admin])
 function selectTenant(id){setTenantId(id);setPerson(id?{...blank,...tenants.find(t=>t.tenantId===id)}:blank);setAccount(accounts.find(a=>a.tenantId===id)?.username||'')}
 function selectAccount(username){setAccount(username);const linked=accounts.find(a=>a.username===username)?.tenantId;if(linked){setTenantId(linked);setPerson({...blank,...tenants.find(t=>t.tenantId===linked)})}}
 async function submit(e){e.preventDefault();if(endDate<=startDate){setError('Ngày kết thúc phải sau ngày bắt đầu.');return}setBusy(true);setError('')
 try{const body=admin?{tenant:person,tenantId:tenantId||null,accountUsername:account||null,roomId,startDate,endDate,deposit:Number(deposit)}:{tenant:person,roomId,requestedStartDate:startDate,requestedEndDate:endDate,message}
 const result=await request(admin?'/api/rentals':'/api/contract-requests',{method:'POST',body:JSON.stringify(body)})
 notify(admin?'Đã lưu khách thuê, phòng và hợp đồng '+result.contractId:'Đã gửi yêu cầu cho chủ nhà.');if(onSaved)await onSaved(result)
 if(admin){setRooms(r=>r.filter(x=>x.roomId!==roomId));setRoomId('');setTenantId('');setAccount('');setPerson(blank)}
 }catch(err){setError(err.message)}finally{setBusy(false)}}
 const owners=[...new Map(rooms.map(r=>[String(r.landlordId),r.landlordName])).entries()];
 const shownRooms=rooms.filter(r=>String(r.landlordId)===landlordId&&available(r));
 const room=rooms.find(r=>r.roomId===roomId)
 const birthday=person.dateOfBirth?new Date(person.dateOfBirth+'T00:00:00'):null
 const now=new Date();const age=birthday?now.getFullYear()-birthday.getFullYear()-((now.getMonth()<birthday.getMonth()||(now.getMonth()===birthday.getMonth()&&now.getDate()<birthday.getDate()))?1:0):null
 return <section className="content-card workflow-card"><div className="card-heading"><div><h2>{admin?'Thêm khách thuê và tạo hợp đồng':'Thông tin đăng ký hợp đồng'}</h2><p>{admin?'Nhập một lần, hệ thống tự liên kết khách thuê với phòng và hợp đồng.':'Điền hồ sơ, chọn phòng và thời hạn. Chủ nhà duyệt sẽ tự tạo hợp đồng.'}</p></div></div>
 {error&&<div className="alert alert-error">{error}</div>}{loading?<p>Đang tải…</p>:<form className="rental-form" onSubmit={submit}>
 {admin&&<><label className="form-field"><span>Khách thuê</span><select value={tenantId} onChange={e=>selectTenant(e.target.value)}><option value="">Thêm khách thuê mới</option>{tenants.map(t=><option key={t.tenantId} value={t.tenantId}>{t.fullName} · {t.cccd}</option>)}</select></label><label className="form-field"><span>Tài khoản khách thuê trên website</span><select value={account} onChange={e=>selectAccount(e.target.value)}><option value="">Chưa liên kết tài khoản</option>{accounts.filter(a=>!a.tenantId||!tenantId||a.tenantId===tenantId).map(a=><option key={a.username} value={a.username}>{a.username}{a.tenantId?' · Đã có hồ sơ':''}</option>)}</select><small>Liên kết để khách xem hợp đồng, hóa đơn và báo sự cố.</small></label></>}
 {[['fullName','Họ và tên','text',50],['cccd','CCCD','text',20],['dateOfBirth','Ngày sinh','date'],['phoneNumber','Số điện thoại','tel',10],['permanentAddress','Địa chỉ thường trú','text',200]].map(([key,label,type,max])=><label className="form-field" key={key}><span>{label}</span><input required type={type} maxLength={max} max={key==='dateOfBirth'?today():undefined} pattern={key==='phoneNumber'?'[0-9]{10}':undefined} value={person[key]||''} onChange={e=>setPerson({...person,[key]:e.target.value})}/></label>)}
 <label className="form-field"><span>Thuê của chủ nhà</span><select required value={landlordId} onChange={e=>{setLandlordId(e.target.value);setRoomId('')}}><option value="">Chọn chủ nhà</option>{owners.map(([id,name])=><option key={id} value={id}>{name}</option>)}</select><small>Chọn người cho thuê để xem đúng danh sách phòng của họ.</small></label><label className="form-field"><span>Phòng còn trống</span><select required value={roomId} onChange={e=>setRoomId(e.target.value)}><option value="">Chọn phòng</option>{shownRooms.map(r=><option key={r.roomId} value={r.roomId}>{r.roomId} · {r.roomType} · {Number(r.basePrice||0).toLocaleString('vi-VN')} ₫/tháng</option>)}</select>{!shownRooms.length&&<small>{landlordId?'Chủ nhà này chưa có phòng trống sẵn sàng cho thuê.':'Hãy chọn chủ nhà trước.'}</small>}</label>
 <label className="form-field"><span>Ngày bắt đầu</span><input required type="date" min={admin?undefined:today()} value={startDate} onChange={e=>setStart(e.target.value)}/></label>
 <label className="form-field"><span>Ngày kết thúc</span><input required type="date" min={startDate} value={endDate} onChange={e=>setEnd(e.target.value)}/></label>
 {admin?<label className="form-field"><span>Tiền đặt cọc (₫)</span><input required type="number" min="0" step="0.01" value={deposit} onChange={e=>setDeposit(e.target.value)}/></label>:<label className="form-field"><span>Lời nhắn cho chủ nhà</span><textarea maxLength={500} value={message} onChange={e=>setMessage(e.target.value)}/></label>}
 <div className="rental-preview"><h3>Nội dung hợp đồng</h3><dl><dt>Khách thuê</dt><dd>{person.fullName||'Chưa nhập'}{age!==null?' · '+age+' tuổi':''}</dd><dt>CCCD / Điện thoại</dt><dd>{person.cccd||'—'} / {person.phoneNumber||'—'}</dd><dt>Chủ nhà</dt><dd>{room?.landlordName||'Chưa chọn'}</dd><dt>Phòng</dt><dd>{room?room.roomId+' · '+room.roomType:'Chưa chọn'}</dd><dt>Giá thuê cơ bản</dt><dd>{room?Number(room.basePrice||0).toLocaleString('vi-VN')+' ₫/tháng':'—'}</dd><dt>Thời hạn thuê</dt><dd>{startDate||'—'} → {endDate||'—'}</dd><dt>Tiền cọc</dt><dd>{admin?Number(deposit||0).toLocaleString('vi-VN')+' ₫':'0 ₫ khi duyệt; chủ nhà cập nhật theo thỏa thuận'}</dd></dl></div>
 <button className="button button-primary" disabled={busy||!shownRooms.length}>{busy?'Đang lưu…':admin?'Lưu khách thuê & hợp đồng':'Gửi đăng ký hợp đồng'}</button>
 </form>}</section>
}

