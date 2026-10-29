export function roomStatusGroup(value){
 const status=String(value||'').trim().toLocaleLowerCase('vi').replace(/đ/g,'d').normalize('NFD').replace(/[\u0300-\u036f]/g,'');
 if(!status)return null;
 if(['unoccupied','not occupied','not rented','khong thue'].some(w=>status.includes(w)))return 'vacant';
 if(['khong trong','not vacant'].some(w=>status.includes(w)))return 'rented';
 if(['chua thue','trong','available','vacant','empty'].some(w=>status.includes(w)))return 'vacant';
 if(['dang thue','da thue','cho thue','occupied','rented','leased','dang su dung','co khach'].some(w=>status.includes(w)))return 'rented';return null;
}
export function activeContract(c,today){return Boolean(c.room&&c.startDate&&c.endDate&&(c.actualCheckIn||c.startDate)<=today&&(c.actualCheckOut?c.actualCheckOut>today:c.endDate>=today))}
export function roomGroups(rooms,contracts,role,today){
 const vacant=rooms.filter(r=>roomStatusGroup(r.status)==='vacant');
 const rented=role==='ADMIN'?rooms.filter(r=>roomStatusGroup(r.status)==='rented'):[...new Map(contracts.filter(c=>activeContract(c,today)&&rooms.some(r=>r.roomId===c.room.roomId)).map(c=>[c.room.roomId,rooms.find(r=>r.roomId===c.room.roomId)])).values()];
 return{vacant,rented};
}
