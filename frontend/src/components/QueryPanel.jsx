import {useState} from 'react'
import {queryActions} from '../data/queries.js'
export default function QueryPanel({module,request,onResults,readOnly}){
 const extras=readOnly&&['contracts','invoices'].includes(module.key)?[]:queryActions[module.key]||[]
 const actions=[['all','Xem tất cả'],['id','Tìm theo mã','Mã '+module.singular,''],...extras]
 const [selected,setSelected]=useState(actions[0]),[value,setValue]=useState(''),[busy,setBusy]=useState(false),[error,setError]=useState(''),[summary,setSummary]=useState(null),[done,setDone]=useState('')
 async function run(action,term){if(action[2]&&!term.trim()){setError('Vui lòng nhập '+action[2].toLowerCase()+'.');return}setBusy(true);setError('');setSummary(null);setDone('')
 try{let path;let options={};const key=action[0]
 if(key==='all')path=module.path
 else if(key==='id')path=module.path+'/'+encodeURIComponent(term.trim())
 else if(key==='complete'){if(!window.confirm('Xác nhận đã hoàn thành công việc '+term+'?'))return;path=module.path+'/'+encodeURIComponent(term.trim())+'/complete';options={method:'PUT'}}
 else path='/api/queries/'+module.key+'/'+key+(action[2]?'?value='+encodeURIComponent(term.trim()):'')
 const data=await request(path,options)
 if(key==='total')setSummary(data);else onResults(Array.isArray(data)?data:data?[data]:[])
 setDone(action[1]+(action[2]?' · '+term:''))}catch(e){setError(e.message||'Không tra cứu được dữ liệu.')}finally{setBusy(false)}}
 function choose(action){setSelected(action);setError('');setSummary(null);setValue(action[4]?.[0]?.[0]||'');if(!action[2])run(action,'')}
 return <section className="content-card query-panel"><h2>Tra cứu nhanh</h2><p>Chọn thông tin cần xem. Kết quả được lấy trực tiếp từ cơ sở dữ liệu và hiển thị trong bảng bên dưới.</p><div className="query-actions">{actions.map(a=><button key={a[0]} disabled={busy} className={'button '+(selected[0]===a[0]?'button-primary':'button-secondary')} onClick={()=>choose(a)}>{a[1]}</button>)}</div>
 {selected[2]&&<form className="query-input" onSubmit={e=>{e.preventDefault();run(selected,value)}}><label className="form-field"><span>{selected[2]}</span>{selected[4]?<select value={value} onChange={e=>setValue(e.target.value)}>{selected[4].map(([v,l])=><option key={v} value={v}>{l}</option>)}</select>:<input required value={value} onChange={e=>setValue(e.target.value)} placeholder={selected[3]?'Ví dụ: '+selected[3]:selected[2]} type={selected[0]==='max-price'?'number':'text'} min={selected[0]==='max-price'?0:undefined}/>}</label><button className="button button-primary" disabled={busy}>{busy?'Đang tải…':selected[0]==='complete'?'Xác nhận hoàn thành':'Tra cứu'}</button></form>}
 {error&&<div role="alert" className="alert alert-error">{error}</div>}{done&&<p role="status" className="query-done">{done}</p>}{summary&&<div className="query-summary"><span>{summary.label}</span><strong>{Number(summary.amount).toLocaleString('vi-VN')} ₫</strong></div>}</section>
}
