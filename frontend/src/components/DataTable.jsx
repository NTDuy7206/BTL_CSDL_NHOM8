import { useMemo, useState } from 'react'
import { getValue, labels } from '../data/modules.js'
import Icon from './Icon.jsx'

function display(record, key) {
  const value = getValue(record, key)
  if (value === true) return <span className="badge badge-green">Hoàn tất</span>
  if (value === false) return <span className="badge badge-gray">Chưa xong</span>
  if (value == null || value === '') return <span className="muted">—</span>
  if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}/.test(value)) return new Intl.DateTimeFormat('vi-VN').format(new Date(`${value.slice(0,10)}T00:00:00`))
  if(key==='agreedRent')return Number(value).toLocaleString('vi-VN')+' ₫/tháng'
  if(['electricityUsage','waterUsage'].includes(key))return String(value)+(key==='electricityUsage'?' kWh':' m³')
  if (key==='area')return String(value)+' m²'
  if (/^electricity(Start|End)Index$/.test(key))return String(value)+' kWh'
  if (/^water(Start|End)Index$/.test(key))return String(value)+' m³'
  if(key==='electricityPrice')return String(value)+' ₫/kWh'
  if(key==='waterPrice')return String(value)+' ₫/m³'
  if (typeof value === 'number' && /Amount|Price|Fine|Salary|deposit|basePrice/i.test(key)) return new Intl.NumberFormat('vi-VN').format(value) + ' ₫'
  return String(value)
}

export default function DataTable({ module, rows, loading, onCreate, onEdit, onDelete, error, onRetry, readOnly = false, onPay, onComplete,completing,onStay }) {
  const [query, setQuery] = useState('')
  const filtered = useMemo(() => {
    const q = query.trim().toLocaleLowerCase('vi')
    return q ? rows.filter(row => JSON.stringify(row).toLocaleLowerCase('vi').includes(q)) : rows
  }, [rows, query])
  const items=filtered
  const download = () => {
    const quote = value => `"${String(value ?? '').replaceAll('"','""')}"`
    const csv = [module.columns.map(key => labels[key] || key).map(quote).join(','), ...filtered.map(row => module.columns.map(key => quote(getValue(row,key))).join(','))].join('\r\n')
    const anchor = document.createElement('a'); anchor.href = URL.createObjectURL(new Blob(['\ufeff',csv],{type:'text/csv;charset=utf-8'})); anchor.download = `${module.key}.csv`; anchor.click(); URL.revokeObjectURL(anchor.href)
  }

  return <section className="data-card">
    <div className="data-toolbar"><div><h2>Danh sách {module.title.toLocaleLowerCase('vi')}</h2><p>{filtered.length} bản ghi {query && `· lọc từ ${rows.length} dòng`}{readOnly&&' · Chế độ chỉ xem'}</p></div><div className="table-actions"><label className="search-control"><Icon name="search" size={17}/><input value={query} onChange={e=>{setQuery(e.target.value)}} placeholder="Tìm kiếm dữ liệu…" /></label><button className="button button-secondary" onClick={download}><Icon name="download" size={16}/> Xuất CSV</button>{!readOnly&&!['rooms','contracts'].includes(module.key)&&<button className="button button-primary" onClick={onCreate}><Icon name="plus" size={17}/> Thêm {module.singular}</button>}</div></div>
    {error ? <div className="table-state"><div className="state-icon error-state"><Icon name="incident" size={23}/></div><h3>Không tải được dữ liệu</h3><p>{error}</p><button className="button button-secondary" onClick={onRetry}><Icon name="refresh" size={16}/> Thử lại</button></div> : loading ? <div className="table-state"><span className="spinner"/><p>Đang tải dữ liệu…</p></div> : filtered.length===0 ? <div className="table-state"><div className="state-icon"><Icon name="search" size={23}/></div><h3>{rows.length?'Không tìm thấy kết quả':'Chưa có dữ liệu'}</h3><p>{rows.length?'Thử từ khóa khác.':readOnly?'Chưa có dữ liệu trong phân hệ này.':module.key==='contracts'?'Khách thuê gửi yêu cầu tại Đăng ký thuê; chủ nhà duyệt để tạo hợp đồng.':'Thêm bản ghi đầu tiên để bắt đầu quản lý.'}</p>{!rows.length&&!readOnly&&!['rooms','contracts'].includes(module.key)&&<button className="button button-primary" onClick={onCreate}><Icon name="plus" size={17}/> Thêm dữ liệu</button>}</div> : <>
      <div className="table-scroll data-scroll-frame" tabIndex="0" role="region" aria-label={module.title}><table className="data-table"><thead><tr>{module.columns.map(key=><th key={key}>{labels[key]||key}</th>)}{(!readOnly||onPay)&&<th className="actions-heading">Thao tác</th>}</tr></thead><tbody>{items.map((row,index)=><tr key={row[module.id]??index}>{module.columns.map((key,colIndex)=><td key={key} className={colIndex===0?'primary-cell':''}>{display(row,key)}</td>)}{(!readOnly||onPay)&&<td><div className="row-actions">{onStay&&<>{row.agreedRent==null&&<button className="button button-secondary" onClick={()=>onStay(row,'rent-rate')}>Xác nhận giá thuê</button>}{!row.actualCheckIn&&!row.actualCheckOut&&<button className="button button-secondary" onClick={()=>onStay(row,'check-in')}>Check-in</button>}{!row.actualCheckOut&&<button className="button button-primary" onClick={()=>onStay(row,'check-out')}>Check-out</button>}</>}{onComplete&&<button className="button button-primary" disabled={completing!=null} onClick={()=>onComplete(row)}>{completing===row[module.id]?'Đang lưu…':'Đã xử lý xong'}</button>}{onPay&&<button className="button button-primary" onClick={()=>onPay(row)}>Thanh toán</button>}{!readOnly&&<><button className="icon-button row-edit" title="Chỉnh sửa" onClick={()=>onEdit(row)}><Icon name="edit" size={16}/></button>{module.key!=='rooms'&&<button className="icon-button row-delete" title="Xóa" onClick={()=>onDelete(row)}><Icon name="trash" size={16}/></button>}</>}</div></td>}</tr>)}</tbody></table></div>
      <div className="table-footer"><span>{filtered.length} bản ghi · Cuộn trong khung để xem toàn bộ danh sách</span></div>
    </>}
  </section>
}
