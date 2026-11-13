import { useCallback, useEffect, useState } from 'react'
import {requireRows} from '../data/listResponse.js'
import ScopeNotice from '../components/ScopeNotice.jsx'
import DataTable from '../components/DataTable.jsx'
import RecordDialog from '../components/RecordDialog.jsx'
import Icon from '../components/Icon.jsx'
import InventorySetup from '../components/InventorySetup.jsx'
import {completionModules,isComplete,visibleWork} from '../data/completion.js'
import StayDialog from '../components/StayDialog.jsx'
import QueryPanel from '../components/QueryPanel.jsx'
import {todayVN} from '../data/finance.js'
import {getValue} from '../data/modules.js'

export default function EntityPage({ module, request, notify, readOnly = false, onPay,onNavigate }) {
  const [keyword,setKeyword]=useState('')
  const [meterPeriod,setMeterPeriod]=useState(()=>todayVN().slice(0,7))
  const [meterUsage,setMeterUsage]=useState('all')
  const [stay,setStay]=useState(null)
  const tracked=completionModules.has(module.key)
  const [workView,setWorkView]=useState('pending'),[completing,setCompleting]=useState(null)
  const [rows,setRows]=useState([]);const [loading,setLoading]=useState(true);const [error,setError]=useState('');const [editing,setEditing]=useState(undefined)
  const visibleRows=module.key==='utility-readings'
    ? rows.filter(row=>{
      if(meterPeriod&&`${row.year}-${String(row.month).padStart(2,'0')}`!==meterPeriod)return false
      const electricity=getValue(row,'electricityUsage')
      const water=getValue(row,'waterUsage')
      switch(meterUsage){
        case 'both': return electricity>0&&water>0
        case 'electricity': return electricity>0
        case 'water': return water>0
        case 'none': return electricity===0&&water===0
        default: return true
      }
    })
    : rows
  const load=useCallback(async()=>{setLoading(true);setError('');try{const result=await request(module.path);setRows(requireRows(result))}catch(err){setError(err.message||'Không tải được dữ liệu.')}finally{setLoading(false)}},[module,request])
  useEffect(()=>{load()},[load])
  async function save(payload){const isEdit=Boolean(editing);const path=isEdit?`${module.path}/${encodeURIComponent(editing[module.id])}`:module.path;await request(path,{method:isEdit?'PUT':'POST',body:JSON.stringify(payload)});notify(isEdit?'Cập nhật thành công.':'Đã thêm bản ghi mới.');await load()}
  async function remove(row){const id=row?.[module.id];if(!id){notify('Không đọc được mã bản ghi.','error');return}if(!window.confirm(`Xóa ${module.singular} “${id}”? Thao tác này không thể hoàn tác.`))return;try{await request(`${module.path}/${encodeURIComponent(id)}`,{method:'DELETE'});notify('Đã xóa bản ghi.');await load()}catch(err){notify(err.message||'Không xóa được bản ghi.','error')}}
  async function complete(row){const id=row[module.id];if(!window.confirm('Xác nhận đã xử lý xong '+id+'?'))return;setCompleting(id);try{await request(module.path+'/'+encodeURIComponent(id)+'/complete',{method:'PUT'});setRows(current=>current.map(r=>r[module.id]===id?{...r,...(module.key==='incidents'?{status:'COMPLETED'}:{isCompleted:true})}:r));notify('Đã xử lý xong. Mục này được chuyển sang lịch sử hoàn thành.');await load()}catch(e){notify(e.message||'Không cập nhật được trạng thái.','error')}finally{setCompleting(null)}}
  return <div className="page-stack"><div className="page-intro"><div className="page-intro-icon"><Icon name={module.icon} size={21}/></div><div><h2>{module.title}</h2></div><span className="intro-spacer"/><button className="icon-button refresh-button" onClick={load} title="Tải lại"><Icon name="refresh"/></button></div>
    {module.key==='invoices'&&!readOnly&&<button className="button button-primary" onClick={()=>onNavigate?.('payments')}>Mở danh sách chờ xác nhận thanh toán</button>}
    {module.key==='rooms'&&!readOnly&&<InventorySetup request={request} onDone={load}/>}
    {module.key==='utility-readings'&&<section className="content-card workflow-card"><div className="rental-form">
      <label className="form-field"><span>Kỳ chỉ số điện nước</span><input type="month" value={meterPeriod} onChange={event=>setMeterPeriod(event.target.value)}/></label>
      <label className="form-field"><span>Mức tiêu thụ trong kỳ</span><select value={meterUsage} onChange={event=>setMeterUsage(event.target.value)}>
        <option value="all">Tất cả phòng</option>
        <option value="both">Có dùng cả điện và nước</option>
        <option value="electricity">Có dùng điện</option>
        <option value="water">Có dùng nước</option>
        <option value="none">Không phát sinh điện nước</option>
      </select></label>
      </div>
      <button className="button button-secondary" onClick={()=>setMeterPeriod('')}>Tất cả các kỳ</button>
    </section>}
    <QueryPanel keyword={keyword} onKeywordChange={setKeyword} module={module} request={request} readOnly={readOnly} onResults={result=>{setRows(result);setError('');setLoading(false);setKeyword('')}}/>
    {tracked&&<section className="content-card workflow-card"><div className="workflow-buttons" role="group" aria-label="Lọc công việc"><button className={'button '+(workView==='pending'?'button-primary':'button-secondary')} onClick={()=>setWorkView('pending')}>Chờ xử lý ({rows.filter(r=>!isComplete(module.key,r)).length})</button><button className={'button '+(workView==='completed'?'button-primary':'button-secondary')} onClick={()=>setWorkView('completed')}>Đã xong ({rows.filter(r=>isComplete(module.key,r)).length})</button></div></section>}
    {!readOnly&&!loading&&!error&&rows.length===0&&<ScopeNotice moduleKey={module.key} request={request} onNavigate={onNavigate}/>}
    <DataTable key={workView} module={module} rows={tracked?visibleWork(module.key,rows,workView):visibleRows} loading={loading} error={error} onRetry={load} readOnly={readOnly} onCreate={()=>setEditing(null)} searchValue={keyword} onSearchChange={setKeyword} searchInToolbar={false} onPay={onPay} onStay={module.key==='contracts'&&!readOnly?(row,mode)=>setStay({row,mode}):undefined} onComplete={tracked&&!readOnly&&workView==='pending'?complete:undefined} completing={completing} onEdit={row=>setEditing(row)} onDelete={remove}/>
    {stay&&<StayDialog row={stay.row} mode={stay.mode} request={request} onClose={()=>setStay(null)} onSaved={load}/>}
    {editing!==undefined&&<RecordDialog request={request} key={`${module.key}-${editing?.[module.id]||'new'}`} module={module} record={editing} onClose={()=>setEditing(undefined)} onSave={save}/>}
  </div>
}
