import assert from 'node:assert/strict'
import {roomGroups} from '../frontend/src/data/roomGroups.js'
const rooms=Array.from({length:15},(_,i)=>({roomId:'R'+i,status:i<10?'OCCUPIED':'AVAILABLE'}));const contract={room:rooms[0],startDate:'2026-01-01',endDate:'2027-01-01'};
assert.deepEqual(roomGroups(rooms,[contract],'USER','2026-10-29').rented,[rooms[0]]);
assert.deepEqual(roomGroups(rooms,[],'USER','2026-10-29').rented,[],'user2 must not inherit global occupied rooms');
assert.equal(roomGroups(rooms,[contract,contract],'USER','2026-10-29').rented.length,1);
assert.equal(roomGroups(rooms,[{...contract,endDate:'2026-02-01'}],'USER','2026-10-29').rented.length,0);
assert.equal(roomGroups(rooms,[{...contract,startDate:'2026-12-01'}],'USER','2026-10-29').rented.length,0);
assert.equal(roomGroups(rooms,[],'ADMIN','2026-10-29').rented.length,10);
assert.equal(roomGroups(rooms,[],'USER','2026-10-29').vacant.length,5);
console.log('7 dashboard room-isolation checks passed');
