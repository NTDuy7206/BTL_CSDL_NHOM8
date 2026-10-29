import { useCallback, useEffect, useState } from 'react'
import DataTable from '../components/DataTable.jsx'
import RecordDialog from '../components/RecordDialog.jsx'
import Icon from '../components/Icon.jsx'
import InventorySetup from '../components/InventorySetup.jsx'
import QueryPanel from '../components/QueryPanel.jsx'

export default function EntityPage({ module, request, notify, readOnly = false, onPay }) {
  const [rows,setRows]=useState([]);const [loading,setLoading]=useState(true);const [error,setError]=useState('');const [editing,setEditing]=useState(undefined)
  const load=useCallback(async()=>{setLoading(true);setError('');try{const result=await request(module.path);setRows(Array.isArray(result)?result:[])}catch(err){setError(err.message||'Không tải được dữ liệu.')}finally{setLoading(false)}},[module,request])
  useEffect(()=>{load()},[load])
  async function save(payload){const isEdit=Boolean(editing);const path=isEdit?`${module.path}/${encodeURIComponent(editing[module.id])}`:module.path;await request(path,{method:isEdit?'PUT':'POST',body:JSON.stringify(payload)});notify(isEdit?'Cập nhật thành công.':'Đã thêm bản ghi mới.');await load()}
  async function remove(row){const id=row?.[module.id];if(!id){notify('Không đọc được mã bản ghi.','error');return}if(!window.confirm(`Xóa ${module.singular} “${id}”? Thao tác này không thể hoàn tác.`))return;try{await request(`${module.path}/${encodeURIComponent(id)}`,{method:'DELETE'});notify('Đã xóa bản ghi.');await load()}catch(err){notify(err.message||'Không xóa được bản ghi.','error')}}
  return <div className="page-stack"><div className="page-intro"><div className="page-intro-icon"><Icon name={module.icon} size={21}/></div><div><h2>{module.title}</h2><p>Xem và quản lý thông tin {module.singular} trong cơ sở dữ liệu.</p></div><span className="intro-spacer"/><button className="icon-button refresh-button" onClick={load} title="Tải lại"><Icon name="refresh"/></button></div>
    {module.key==='rooms'&&readOnly&&<p className="content-card workflow-card">Danh sách phòng của các chủ nhà để tham khảo. Phòng bạn đang thuê được hiển thị tại Hợp đồng của tôi.</p>}
    {module.key==='rooms'&&!readOnly&&<InventorySetup request={request} onDone={load}/>}
    <QueryPanel module={module} request={request} readOnly={readOnly} onResults={result=>{setRows(result);setError('');setLoading(false)}}/>
    <DataTable module={module} rows={rows} loading={loading} error={error} onRetry={load} readOnly={readOnly} onCreate={()=>setEditing(null)} onPay={onPay} onEdit={row=>setEditing(row)} onDelete={remove}/>
    {editing!==undefined&&<RecordDialog key={`${module.key}-${editing?.[module.id]||'new'}`} module={module} record={editing} onClose={()=>setEditing(undefined)} onSave={save}/>}
  </div>
}
