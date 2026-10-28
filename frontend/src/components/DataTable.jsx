import { useMemo, useState } from 'react'
import { getValue, labels } from '../data/modules.js'
import Icon from './Icon.jsx'

function display(record, key) {
  const value = getValue(record, key)
  if (value === true) return <span className="badge badge-green">Hoàn tất</span>
  if (value === false) return <span className="badge badge-gray">Chưa xong</span>
  if (value == null || value === '') return <span className="muted">—</span>
  if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}/.test(value)) return new Intl.DateTimeFormat('vi-VN').format(new Date(`${value.slice(0,10)}T00:00:00`))
  if (typeof value === 'number' && /Amount|Price|Fine|Salary|deposit|basePrice/i.test(key)) return new Intl.NumberFormat('vi-VN').format(value) + ' ₫'
  return String(value)
}

export default function DataTable({ module, rows, loading, onCreate, onEdit, onDelete, error, onRetry, readOnly = false }) {
  const [query, setQuery] = useState('')
  const [page, setPage] = useState(1)
  const pageSize = 8
  const filtered = useMemo(() => {
    const q = query.trim().toLocaleLowerCase('vi')
    return q ? rows.filter(row => JSON.stringify(row).toLocaleLowerCase('vi').includes(q)) : rows
  }, [rows, query])
  const pageCount = Math.max(1, Math.ceil(filtered.length / pageSize))
  const items = filtered.slice((page - 1) * pageSize, page * pageSize)
  const download = () => {
    const quote = value => `"${String(value ?? '').replaceAll('"','""')}"`
    const csv = [module.columns.map(key => labels[key] || key).map(quote).join(','), ...filtered.map(row => module.columns.map(key => quote(getValue(row,key))).join(','))].join('\r\n')
    const anchor = document.createElement('a'); anchor.href = URL.createObjectURL(new Blob(['\ufeff',csv],{type:'text/csv;charset=utf-8'})); anchor.download = `${module.key}.csv`; anchor.click(); URL.revokeObjectURL(anchor.href)
  }

  return <section className="data-card">
    <div className="data-toolbar"><div><h2>Danh sách {module.title.toLocaleLowerCase('vi')}</h2><p>{filtered.length} bản ghi {query && `· lọc từ ${rows.length} dòng`}{readOnly&&' · Chế độ chỉ xem'}</p></div><div className="table-actions"><label className="search-control"><Icon name="search" size={17}/><input value={query} onChange={e=>{setQuery(e.target.value);setPage(1)}} placeholder="Tìm kiếm dữ liệu…" /></label><button className="button button-secondary" onClick={download}><Icon name="download" size={16}/> Xuất CSV</button>{!readOnly&&module.key!=='rooms'&&<button className="button button-primary" onClick={onCreate}><Icon name="plus" size={17}/> Thêm {module.singular}</button>}</div></div>
    {error ? <div className="table-state"><div className="state-icon error-state"><Icon name="incident" size={23}/></div><h3>Không tải được dữ liệu</h3><p>{error}</p><button className="button button-secondary" onClick={onRetry}><Icon name="refresh" size={16}/> Thử lại</button></div> : loading ? <div className="table-state"><span className="spinner"/><p>Đang tải dữ liệu…</p></div> : filtered.length===0 ? <div className="table-state"><div className="state-icon"><Icon name="search" size={23}/></div><h3>{rows.length?'Không tìm thấy kết quả':'Chưa có dữ liệu'}</h3><p>{rows.length?'Thử từ khóa khác.':readOnly?'Chưa có dữ liệu trong phân hệ này.':'Thêm bản ghi đầu tiên để bắt đầu quản lý.'}</p>{!rows.length&&!readOnly&&module.key!=='rooms'&&<button className="button button-primary" onClick={onCreate}><Icon name="plus" size={17}/> Thêm dữ liệu</button>}</div> : <>
      <div className="table-scroll"><table className="data-table"><thead><tr>{module.columns.map(key=><th key={key}>{labels[key]||key}</th>)}{!readOnly&&<th className="actions-heading">Thao tác</th>}</tr></thead><tbody>{items.map((row,index)=><tr key={row[module.id]??index}>{module.columns.map((key,colIndex)=><td key={key} className={colIndex===0?'primary-cell':''}>{display(row,key)}</td>)}{!readOnly&&<td><div className="row-actions"><button className="icon-button row-edit" title="Chỉnh sửa" onClick={()=>onEdit(row)}><Icon name="edit" size={16}/></button>{module.key!=='rooms'&&<button className="icon-button row-delete" title="Xóa" onClick={()=>onDelete(row)}><Icon name="trash" size={16}/></button>}</div></td>}</tr>)}</tbody></table></div>
      <div className="table-footer"><span>Hiển thị <b>{(page-1)*pageSize+1}–{Math.min(page*pageSize,filtered.length)}</b> trong <b>{filtered.length}</b> bản ghi</span><div className="pagination"><button className="icon-button" disabled={page===1} onClick={()=>setPage(page-1)} aria-label="Trang trước"><span className="rotate-left"><Icon name="chevron" size={16}/></span></button><span>Trang <b>{page}</b> / {pageCount}</span><button className="icon-button" disabled={page===pageCount} onClick={()=>setPage(page+1)} aria-label="Trang sau"><Icon name="chevron" size={16}/></button></div></div>
    </>}
  </section>
}
