import { modules } from './modules.js'
const tenantModules = new Set(['employees','rooms','contracts','invoices','incidents','cleaning-supports','rules','violations'])
export function visibleModules(role) {
 if (role === 'ADMIN') return modules
 return modules.filter(module => tenantModules.has(module.key)).map(module => {
  if (module.key === 'cleaning-supports') return {...module, path: `${module.path}/mine`}
  if (module.key === 'contracts') return {...module, title: 'Hợp đồng của tôi'}
  if (module.key === 'invoices') return {...module, title: 'Hóa đơn của tôi'}
  return module
 })
}
export const isVipRoom = room => /\bVIP\b/i.test(room?.roomType || '')

export function available(room) {
 if(typeof room.availableForRent==='boolean')return room.availableForRent
 if(room.occupied===true||room.occupancyKnown===false)return false
 const s=String(room.status||'').toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g,'').replace(/đ/g,'d').trim()
 return ['available','vacant','unoccupied','not occupied','empty','trong','con trong','chua thue','chua cho thue'].includes(s)
}

