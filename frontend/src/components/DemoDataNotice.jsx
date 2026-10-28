import {useState} from 'react'
export default function DemoDataNotice({user,request,onDone}){
 const[busy,setBusy]=useState(false),[message,setMessage]=useState(''),[error,setError]=useState('')
 async function create(){setBusy(true);setError('');try{const result=await request('/api/demo/my-rental',{method:'POST'});setMessage(result.message);onDone()}catch(e){setError(e.message)}finally{setBusy(false)}}
 return <section className="content-card demo-notice"><div><h2>Dữ liệu thực hành</h2><p>Các bản ghi có mã DEMO_ là dữ liệu giả lập cho bài tập lớn. Bạn có thể xem, lọc và thử các chức năng trên những bản ghi này.</p>{user?.role==='USER'&&<p>Tạo một hồ sơ thuê mẫu riêng để thấy hợp đồng, hóa đơn và thử báo sự cố. Nếu tài khoản đã liên kết khách thuê, hệ thống giữ nguyên dữ liệu hiện có.</p>}</div>{user?.role==='USER'&&<button className="button button-primary" disabled={busy} onClick={create}>{busy?'Đang tạo…':'Tạo hồ sơ thuê mẫu cho tôi'}</button>}{message&&<p role="status">{message}</p>}{error&&<p role="alert" className="alert alert-error">{error}</p>}</section>
}
