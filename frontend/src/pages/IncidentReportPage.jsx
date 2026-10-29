import {useEffect,useState} from 'react'
import {activeContract} from '../data/roomGroups.js'
import EntityPage from './EntityPage.jsx'
export default function IncidentReportPage({module,request,notify}){
 const[contracts,setContracts]=useState([]),[roomId,setRoom]=useState(''),[content,setContent]=useState(''),[error,setError]=useState(''),[busy,setBusy]=useState(false),[version,setVersion]=useState(0)
 useEffect(()=>{request('/api/contracts').then(rows=>{const today=new Date().toLocaleDateString('en-CA');setContracts(rows.filter(c=>activeContract(c,today)))}).catch(e=>setError(e.message))},[request])
 async function submit(e){e.preventDefault();setBusy(true);setError('');try{await request('/api/incidents',{method:'POST',body:JSON.stringify({roomId,incidentContent:content})});notify('Đã gửi báo cáo sự cố cho chủ nhà.');setContent('');setVersion(v=>v+1)}catch(e){setError(e.message)}finally{setBusy(false)}}
 return <div className="page-stack"><section className="content-card workflow-card"><h2>Báo cáo sự cố</h2><p>Chủ nhà sẽ tiếp nhận và cập nhật tình trạng xử lý.</p>{error&&<div className="alert alert-error">{error}</div>}<form className="workflow-form" onSubmit={submit}><label className="form-field"><span>Phòng đang thuê</span><select required value={roomId} onChange={e=>setRoom(e.target.value)}><option value="">Chọn phòng</option>{[...new Set(contracts.map(c=>c.room?.roomId))].filter(Boolean).map(id=><option key={id}>{id}</option>)}</select></label><label className="form-field"><span>Nội dung sự cố</span><textarea required maxLength={500} rows={3} value={content} onChange={e=>setContent(e.target.value)}/></label><button className="button button-primary" disabled={busy||!contracts.length}>{busy?'Đang gửi…':'Gửi báo cáo'}</button>{!contracts.length&&<small>Bạn cần có hợp đồng đang hiệu lực và được liên kết với tài khoản.</small>}</form></section><EntityPage key={version} module={module} request={request} notify={notify} readOnly/></div>
}

