import { useEffect, useState } from 'react'
import { getValue } from '../data/modules.js'
import Icon from './Icon.jsx'

export default function RecordDialog({ module, record, onClose, onSave }) {
  const editing = Boolean(record)
  const [values, setValues] = useState({})
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  useEffect(() => {
    const initial = {}
    module.fields.forEach(([key,,type]) => { initial[key] = type === 'checkbox' ? Boolean(getValue(record,key)) : getValue(record,key) ?? '' })
    setValues(initial)
  }, [module,record])
  function update(key,value){setValues(current=>({...current,[key]:value}))}
  async function submit(event){
    event.preventDefault();setError('');setBusy(true)
    const payload={}
    for(const [key,,type] of module.fields){const value=values[key];payload[key]=type==='checkbox'?Boolean(value):value===''?null:type==='number'?Number(value):value}
    if(editing)payload[module.id]=record[module.id]
    try{await onSave(payload);onClose()}catch(err){setError(err.message||'Không lưu được dữ liệu.');setBusy(false)}
  }
  return <div className="dialog-backdrop" onMouseDown={event=>{if(event.target===event.currentTarget)onClose()}}><section className="record-dialog" role="dialog" aria-modal="true" aria-labelledby="dialog-title">
    <div className="dialog-head"><div className="dialog-mark"><Icon name={module.icon} size={19}/></div><div><h2 id="dialog-title">{editing?'Cập nhật':'Thêm'} {module.singular}</h2></div><button className="icon-button dialog-close" onClick={onClose} aria-label="Đóng"><Icon name="close"/></button></div>
    <form onSubmit={submit}><div className="dialog-content"><div className="field-grid">{module.fields.map(([key,label,type])=>{
      if(module.key==='rooms'&&key==='status')return <label className="form-field" key={key}><span>Tình trạng phòng</span><select required value={values[key]||'UNCONFIGURED'} onChange={e=>update(key,e.target.value)}><option value="UNCONFIGURED">Chưa cấu hình</option><option value="AVAILABLE">Phòng trống, mở cho thuê</option><option value="OCCUPIED">Đã thuê</option></select></label>
      if(type==='checkbox')return <label className="checkbox-field" key={key}><input type="checkbox" checked={Boolean(values[key])} onChange={e=>update(key,e.target.checked)}/><span className="checkbox-box">✓</span><span>{label}</span></label>
      const common={id:`field-${key}`,name:key,value:values[key]??'',disabled:editing&&(key===module.id||(module.key==='contracts'&&['tenantId','roomId'].includes(key))||(module.key==='invoices'&&['contractId','month','year'].includes(key))),required:key===module.id,onChange:e=>update(key,e.target.value),placeholder:key===module.id?`Nhập ${label.toLocaleLowerCase('vi')}`:'Nhập thông tin'}
      return <label className={`form-field ${type==='textarea'?'field-span':''}`} htmlFor={common.id} key={key}><span>{label}{key===module.id&&<i> bắt buộc</i>}</span>{type==='textarea'?<textarea {...common} rows="3"/>:<input {...common} type={type} step={type==='number'?'any':undefined}/>}</label>
    })}</div>{error&&<div className="alert alert-error">{error}</div>}</div><div className="dialog-footer"><button type="button" className="button button-secondary" onClick={onClose}>Hủy</button><button className="button button-primary" disabled={busy}>{busy?<span className="spinner small-spinner"/>:null}{editing?'Lưu thay đổi':'Tạo bản ghi'}</button></div></form>
  </section></div>
}
