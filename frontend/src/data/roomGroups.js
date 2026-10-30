export function roomStatusGroup(value){
 const status=String(value||'').trim().toLocaleLowerCase('vi').replace(/đ/g,'d').normalize('NFD').replace(/[\u0300-\u036f]/g,'');
 if(!status)return null;
 if(['unoccupied','not occupied','not rented','khong thue'].some(w=>status.includes(w)))return 'vacant';
 if(['khong trong','not vacant'].some(w=>status.includes(w)))return 'rented';
 if(['chua thue','trong','available','vacant','empty'].some(w=>status.includes(w)))return 'vacant';
 if(['dang thue','da thue','cho thue','occupied','rented','leased','dang su dung','co khach'].some(w=>status.includes(w)))return 'rented';return null;
}
export function activeContract(c,today){return Boolean(c.room&&(c.actualCheckIn||c.startDate)&&(c.actualCheckOut||c.endDate)&&(c.actualCheckIn||c.startDate)<=today&&(c.actualCheckOut?c.actualCheckOut>today:c.endDate>=today))}
export function roomGroups(rooms,contracts,role,today){
 const byRoom=new Map(rooms.map(r=>[r.roomId,r]));
 const active=contracts.filter(c=>activeContract(c,today)&&byRoom.has(c.room.roomId));
 const rentedIds=new Set(active.map(c=>c.room.roomId));
 // A room with an incomplete contract cannot safely be declared vacant.
 const uncertainIds=new Set(contracts.filter(c=>c.room&&byRoom.has(c.room.roomId)&&(!(c.actualCheckIn||c.startDate)||!c.endDate)&&!c.actualCheckOut).map(c=>c.room.roomId));
 const unknown=role==='ADMIN'?rooms.filter(r=>r.occupancyKnown===false||(typeof r.occupied!=='boolean'&&uncertainIds.has(r.roomId)&&!rentedIds.has(r.roomId))):[];
 const vacant=role==='ADMIN'?rooms.filter(r=>typeof r.occupied==='boolean'?r.occupancyKnown!==false&&!r.occupied:!rentedIds.has(r.roomId)&&!uncertainIds.has(r.roomId)):rooms.filter(r=>typeof r.occupied==='boolean'?r.occupancyKnown!==false&&!r.occupied:roomStatusGroup(r.status)==='vacant');
 const rented=rooms.filter(r=>role==='ADMIN'&&typeof r.occupied==='boolean'?r.occupied:rentedIds.has(r.roomId));
 const roomContractCounts=new Map();for(const c of active)roomContractCounts.set(c.room.roomId,(roomContractCounts.get(c.room.roomId)||0)+1);
 const conflictingRooms=rooms.filter(r=>(roomContractCounts.get(r.roomId)||0)>1);
 return{vacant,rented,unknown,activeContracts:active,conflictingRooms};
}
