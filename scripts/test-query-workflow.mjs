import assert from 'node:assert/strict'
const base=process.env.TEST_BASE_URL||'http://127.0.0.1:18082'
if(!['localhost','127.0.0.1'].includes(new URL(base).hostname))throw new Error('Use only an isolated local database')
let checks=0;const suffix=Date.now().toString(36),password='InventoryTest-2026!'
async function call(path,token='',method='GET',body,status=200){const r=await fetch(base+'/api'+path,{method,headers:{'Content-Type':'application/json',...(token?{Authorization:'Bearer '+token}:{})},...(body?{body:JSON.stringify(body)}:{})});const raw=await r.text();let data;try{data=raw?JSON.parse(raw):null}catch{data=raw}assert.equal(r.status,status,method+' '+path+' '+raw);checks++;return data}
async function account(role,s,count){const username='inventory_'+suffix+s;const user=await call('/auth/register','','POST',{username,password,role,rooms:role==='ADMIN'?Array.from({length:count},(_,i)=>({roomId:String(101+i),floor:1,area:20,roomType:'Studio',basePrice:2000000})):undefined},201);return{...user,username,token:(await call('/auth/login','','POST',{username,password})).accessToken}}
const a=await account('ADMIN','a',2),b=await account('ADMIN','b',3),user=await account('USER','u')
const ownA=await call('/rooms',a.token),ownB=await call('/rooms',b.token);assert.equal(ownA.length,2);assert.equal(ownB.length,3)
const roomId=ownA[0].roomId,payload={roomId,floor:1,area:30,roomType:'Studio',status:'AVAILABLE',basePrice:3000000}
await call('/rooms/'+roomId,b.token,'PUT',payload,403)
await call('/rooms/'+roomId,a.token,'PUT',payload)
await call('/rooms',a.token,'POST',payload,409)
await call('/rooms/'+roomId,a.token,'DELETE',null,409)
await call('/landlords/property',a.token,'POST',{roomCount:10},409)
const catalog=await call('/landlords/rooms',user.token);assert.ok(catalog.some(r=>r.roomId===roomId&&r.landlordName===a.username));assert.ok(catalog.some(r=>r.landlordName===b.username));
assert.equal((await call('/rooms/'+ownB[0].roomId,user.token)).landlordName,b.username)
assert.equal((await call('/queries/rooms/status?value=AVAILABLE',a.token)).length,2)
assert.equal((await call('/queries/rooms/status?value=AVAILABLE',b.token)).length,3)
const before=(await call('/rooms',user.token)).length
await call('/demo/my-rental',user.token,'POST',null,410)
assert.equal((await call('/rooms',user.token)).length,before)
const today=new Date().toLocaleDateString('en-CA'),end=new Date(Date.now()+86400000*365).toLocaleDateString('en-CA')
const tenant={fullName:'Khách thử',cccd:'I'+suffix,dateOfBirth:'2000-01-01',phoneNumber:'0900000000',permanentAddress:'Hà Nội'}
const rental=await call('/contract-requests',user.token,'POST',{roomId,tenant,requestedStartDate:today,requestedEndDate:end},201)
assert.ok((await call('/contract-requests',a.token)).some(r=>r.requestId===rental.requestId))
assert.ok(!(await call('/contract-requests',b.token)).some(r=>r.requestId===rental.requestId))
await call('/contract-requests/'+rental.requestId+'/status',b.token,'PATCH',{status:'APPROVED'},403)
await call('/contract-requests/'+rental.requestId+'/status',a.token,'PATCH',{status:'APPROVED'})
assert.equal((await call('/rooms',a.token)).length,2)
assert.equal((await call('/rooms',user.token)).length,before)
assert.equal((await call('/rooms/'+roomId,user.token)).status,'OCCUPIED')
await call('/auth/register','','POST',{username:'invalid_'+suffix,password,role:'ADMIN',roomCount:0},400)
console.log(JSON.stringify({passed:checks,flows:['fixed capacity','owner selection','cross owner denial','rental uses existing room','old demo disabled']},null,2))
