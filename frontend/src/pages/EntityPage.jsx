import { useCallback, useEffect, useState } from 'react'
import DataTable from '../components/DataTable.jsx'
import RecordDialog from '../components/RecordDialog.jsx'
import Icon from '../components/Icon.jsx'
import InventorySetup from '../components/InventorySetup.jsx'
import {completionModules,isComplete,visibleWork} from '../data/completion.js'
import StayDialog from '../components/StayDialog.jsx'
import QueryPanel from '../components/QueryPanel.jsx'

export default function EntityPage({ module, request, notify, readOnly = false, onPay }) {
  const [stay,setStay]=useState(null)
  const tracked=completionModules.has(module.key)
  const [workView,setWorkView]=useState('pending'),[completing,setCompleting]=useState(null)
  const [rows,setRows]=useState([]);const [loading,setLoading]=useState(true);const [error,setError]=useState('');const [editing,setEditing]=useState(undefined)
  const load=useCallback(async()=>{setLoading(true);setError('');try{const result=await request(module.path);setRows(Array.isArray(result)?result:[])}catch(err){setError(err.message||'Không tải được dữ liệu.')}finally{setLoading(false)}},[module,request])
  useEffect(()=>{load()},[load])
  async function save(payload){const isEdit=Boolean(editing);const path=isEdit?`${module.path}/${encodeURIComponent(editing[module.id])}`:module.path;await request(path,{method:isEdit?'PUT':'POST',body:JSON.stringify(payload)});notify(isEdit?'Cập nhật thành công.':'Đã thêm bản ghi mới.');await load()}
  async function remove(row){const id=row?.[module.id];if(!id){notify('Không đọc được mã bản ghi.','error');return}if(!window.confirm(`Xóa ${module.singular} “${id}”? Thao tác này không thể hoàn tác.`))return;try{await request(`${module.path}/${encodeURIComponent(id)}`,{method:'DELETE'});notify('Đã xóa bản ghi.');await load()}catch(err){notify(err.message||'Không xóa được bản ghi.','error')}}
  async function complete(row){const id=row[module.id];if(!window.confirm('Xác nhận đã xử lý xong '+id+'?'))return;setCompleting(id);try{await request(module.path+'/'+encodeURIComponent(id)+'/complete',{method:'PUT'});setRows(current=>current.map(r=>r[module.id]===id?{...r,...(module.key==='incidents'?{status:'COMPLETED'}:{isCompleted:true})}:r));notify('Đã xử lý xong. Mục này được chuyển sang lịch sử hoàn thành.');await load()}catch(e){notify(e.message||'Không cập nhật được trạng thái.','error')}finally{setCompleting(null)}}
  return <div className="page-stack"><div className="page-intro"><div className="page-intro-icon"><Icon name={module.icon} size={21}/></div><div><h2>{module.title}</h2><p>Xem và quản lý thông tin {module.singular} trong cơ sở dữ liệu.</p></div><span className="intro-spacer"/><button className="icon-button refresh-button" onClick={load} title="Tải lại"><Icon name="refresh"/></button></div>
    {module.key==='rooms'&&readOnly&&<p className="content-card workflow-card">Danh sách phòng của các chủ nhà để tham khảo. Phòng bạn đang thuê được hiển thị tại Hợp đồng của tôi.</p>}
    {module.key==='rooms'&&!readOnly&&<InventorySetup request={request} onDone={load}/>}
    <QueryPanel module={module} request={request} readOnly={readOnly} onResults={result=>{setRows(result);setError('');setLoading(false)}}/>
    {tracked&&<section className="content-card workflow-card"><div className="workflow-buttons" role="group" aria-label="Lọc công việc"><button className={'button '+(workView==='pending'?'button-primary':'button-secondary')} onClick={()=>setWorkView('pending')}>Chờ xử lý ({rows.filter(r=>!isComplete(module.key,r)).length})</button><button className={'button '+(workView==='completed'?'button-primary':'button-secondary')} onClick={()=>setWorkView('completed')}>Đã xong ({rows.filter(r=>isComplete(module.key,r)).length})</button></div><p>Các mục hoàn thành tự rời danh sách chờ xử lý. Xem lại tại Đã xong.</p></section>}
    <DataTable key={workView} module={module} rows={tracked?visibleWork(module.key,rows,workView):rows} loading={loading} error={error} onRetry={load} readOnly={readOnly} onCreate={()=>setEditing(null)} onPay={onPay} onStay={module.key==='contracts'&&!readOnly?(row,mode)=>setStay({row,mode}):undefined} onComplete={tracked&&!readOnly&&workView==='pending'?complete:undefined} completing={completing} onEdit={row=>setEditing(row)} onDelete={remove}/>
    {stay&&<StayDialog row={stay.row} mode={stay.mode} request={request} onClose={()=>setStay(null)} onSaved={load}/>}
    {editing!==undefined&&<RecordDialog key={`${module.key}-${editing?.[module.id]||'new'}`} module={module} record={editing} onClose={()=>setEditing(undefined)} onSave={save}/>}
  </div>
}
