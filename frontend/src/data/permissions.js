import { modules } from './modules.js'
const tenantModules = new Set(['employees','rooms','contracts','invoices','incidents','rules','violations'])
export function visibleModules(role) { return role === 'ADMIN' ? modules : modules.filter(m=>tenantModules.has(m.key)).map(m=>({...m,title:m.key==='contracts'?'Hợp đồng của tôi':m.key==='invoices'?'Hóa đơn của tôi':m.title})) }
export function available(room) {
 const s=String(room.status||'').toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g,'').replace(/đ/g,'d').trim()
 return ['available','vacant','unoccupied','not occupied','empty','trong','con trong','chua thue','chua cho thue'].includes(s)
}

