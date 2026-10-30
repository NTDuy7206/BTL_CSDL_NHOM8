import assert from 'node:assert/strict'
import {roomGroups} from '../frontend/src/data/roomGroups.js'
const rooms=Array.from({length:15},(_,i)=>({roomId:'R'+i,status:i<10?'OCCUPIED':'AVAILABLE'}));const contract={room:rooms[0],startDate:'2026-01-01',endDate:'2027-01-01'};
assert.deepEqual(roomGroups(rooms,[contract],'USER','2026-10-29').rented,[rooms[0]]);
assert.deepEqual(roomGroups(rooms,[],'USER','2026-10-29').rented,[],'user2 must not inherit global occupied rooms');
assert.equal(roomGroups(rooms,[contract,contract],'USER','2026-10-29').rented.length,1);
assert.equal(roomGroups(rooms,[{...contract,endDate:'2026-02-01'}],'USER','2026-10-29').rented.length,0);
assert.equal(roomGroups(rooms,[{...contract,startDate:'2026-12-01'}],'USER','2026-10-29').rented.length,0);
assert.equal(roomGroups(rooms,[contract],'ADMIN','2026-10-29').rented.length,1,'active contracts override stale room status');
assert.equal(roomGroups(rooms,[],'USER','2026-10-29').vacant.length,5);
console.log('7 dashboard room-isolation checks passed');

assert.equal(roomGroups(rooms,[{...contract,actualCheckOut:'2026-10-29'}],'USER','2026-10-29').rented.length,0);
assert.equal(roomGroups(rooms,[{...contract,startDate:'2026-11-01',actualCheckIn:'2026-10-28'}],'USER','2026-10-29').rented.length,1);
console.log('2 actual check-in/check-out dashboard checks passed');

assert.equal(roomGroups(rooms.slice(1),[contract],'USER','2026-10-29').rented.length,0,'other landlord rooms excluded from dashboard even when own old contract exists');

const inventory=Array.from({length:65},(_,i)=>({roomId:'P'+i,status:i<6?'OCCUPIED':'UNCONFIGURED'}));
const six=inventory.slice(0,6).map((room,i)=>({contractId:'C'+i,room,startDate:'2026-01-01',endDate:'2027-01-01'}));
const expired={contractId:'old',room:inventory[0],startDate:'2025-01-01',endDate:'2025-12-31'};
let summary=roomGroups(inventory,[...six,expired],'ADMIN','2026-10-29');
assert.equal(summary.vacant.length,59);assert.equal(summary.rented.length,6);assert.equal(summary.activeContracts.length,6);assert.equal(summary.unknown.length,0);
assert.equal(summary.vacant.length+summary.rented.length,65);
summary=roomGroups(inventory,[...six,{...expired,startDate:'2026-01-01',endDate:'2027-01-01'}],'ADMIN','2026-10-29');
assert.equal(summary.activeContracts.length,7,'do not hide a genuine overlapping seventh contract');assert.equal(summary.conflictingRooms.length,1);assert.equal(summary.rented.length,6);
summary=roomGroups(inventory,[...six,{contractId:'missing',room:inventory[6],startDate:null,endDate:null}],'ADMIN','2026-10-29');assert.equal(summary.unknown.length,1);assert.equal(summary.vacant.length,58);
summary=roomGroups(inventory,[{...six[0],actualCheckOut:'2026-10-29'}],'ADMIN','2026-10-29');assert.equal(summary.rented.length,0);assert.equal(summary.vacant.length,65);
summary=roomGroups(inventory,[{...six[0],startDate:'2026-11-01',actualCheckIn:'2026-10-28'}],'ADMIN','2026-10-29');assert.equal(summary.rented.length,1);
console.log('65-room inventory: 59 vacant + 6 occupied, active vs historical contracts, conflicts and incomplete dates passed');

const publicRooms=inventory.map((r,i)=>({...r,occupied:i<6,occupancyKnown:true,availableForRent:false}));
const tenantSummary=roomGroups(publicRooms,[],'USER','2026-10-30'),ownerSummary=roomGroups(publicRooms,six,'ADMIN','2026-10-30');
assert.equal(tenantSummary.vacant.length,59);assert.equal(ownerSummary.vacant.length,59);assert.equal(tenantSummary.rented.length,0,'tenant must not inherit six other tenants contracts');assert.equal(ownerSummary.rented.length,6);
console.log('ADMIN / USER public vacancy parity and private tenant-contract isolation passed');

const {available}=await import('../frontend/src/data/permissions.js');assert.equal(available({status:'AVAILABLE',occupied:true,availableForRent:false}),false);assert.equal(available({status:'UNCONFIGURED',occupied:false,availableForRent:false}),false);assert.equal(available({status:'AVAILABLE',occupied:false,availableForRent:true}),true);
