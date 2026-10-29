import assert from 'node:assert/strict'
// Use ONLY an isolated local database. This test creates fixtures and keeps them for inspection.
const base=process.env.TEST_BASE_URL||'http://127.0.0.1:18082'
if(!['127.0.0.1','localhost'].includes(new URL(base).hostname))throw new Error('Only an isolated local server is allowed')
const suffix=Date.now().toString(36), password='RentalTest-2026!'
let checks=0
async function call(path,token='',method='GET',body,expected=200){
 const r=await fetch(base+'/api'+path,{method,headers:{'Content-Type':'application/json',...(token?{Authorization:'Bearer '+token}:{})},...(body?{body:JSON.stringify(body)}:{})});const raw=await r.text();let data;try{data=raw?JSON.parse(raw):null}catch{data=raw}
 assert.equal(r.status,expected,method+' '+path+' => '+raw);checks++;return data
}
async function account(role,n){const username='test_'+suffix+n;const u=await call('/auth/register','','POST',{username,password,role,...(role==='ADMIN'?{rooms:Array.from({length:3},(_,i)=>({roomId:String(101+i),floor:1,area:20,roomType:'Studio',basePrice:2000000}))}:{})},201);assert.equal(u.role,role);const l=await call('/auth/login','','POST',{username,password});return{...u,username,token:l.accessToken}}
const admin=await account('ADMIN','a'),user=await account('USER','u'),other=await account('USER','o')
await call('/auth/register','','POST',{username:admin.username,password,role:'ADMIN'},400)
await call('/auth/register','','POST',{username:'bad_'+suffix,password,role:'OWNER'},400)
for(const tenant of [user,other])await call('/landlords/selection',tenant.token,'PUT',{landlordId:admin.id})
const inventory=await call('/rooms',admin.token);assert.equal(inventory.length,3)
const [room1,room2,room3]=inventory.map(r=>r.roomId)
for(const roomId of [room1,room2])await call('/rooms/'+roomId,admin.token,'PUT',{roomId,floor:1,area:30,roomType:'Studio',status:'Trống',basePrice:3000000})
for(const p of ['/rooms','/employees','/incidents','/rules','/violations','/contracts','/invoices'])await call(p,user.token)
for(const p of ['/tenants','/utility-readings','/incident-resolutions','/cleaning-supports','/rentals/accounts','/admin/accounts'])await call(p,user.token,'GET',null,403)
for(const method of ['POST','PUT','DELETE'])await call('/rooms'+(method==='POST'?'':'/'+room1),user.token,method,{roomId:room1},403)
await call('/rooms','','GET',null,401)
const today=new Date().toLocaleDateString('en-CA'), end=new Date(Date.now()+86400000*365).toLocaleDateString('en-CA')
const person={fullName:'Nguyễn Khách Thuê',cccd:'C'+suffix,dateOfBirth:'2000-01-02',phoneNumber:'0912345678',permanentAddress:'Hà Nội'}
const req=await call('/contract-requests',user.token,'POST',{roomId:room1,requestedStartDate:today,requestedEndDate:end,tenant:person,message:'Đăng ký phòng'},201)
await call('/contract-requests',user.token,'POST',{roomId:room1,requestedStartDate:today,requestedEndDate:end,tenant:person},409)
await call('/contract-requests/'+req.requestId+'/status',user.token,'PATCH',{status:'APPROVED'},403)
const approved=await call('/contract-requests/'+req.requestId+'/status',admin.token,'PATCH',{status:'APPROVED'})
assert.ok(approved.contractId);assert.equal(approved.tenant.fullName,person.fullName)
await call('/contract-requests/'+req.requestId+'/status',admin.token,'PATCH',{status:'APPROVED'},409)
const mine=await call('/contracts',user.token);assert.equal(mine.length,1);assert.equal(mine[0].room.roomId,room1)
assert.equal((await call('/contracts',other.token)).length,0)
await call('/contracts/'+approved.contractId,other.token,'GET',null,403)
await call('/contracts/'+approved.contractId,user.token,'DELETE',null,403)
const incident=await call('/incidents',user.token,'POST',{roomId:room1,incidentContent:'Vòi nước bị rò',status:'COMPLETED',incidentId:'forged'},201)
assert.equal(incident.status,'PENDING');assert.notEqual(incident.incidentId,'forged')
await call('/incidents',other.token,'POST',{roomId:room1,incidentContent:'Other room'},403)
await call('/incidents/'+incident.incidentId,user.token,'PUT',{status:'COMPLETED'},403)
const invoiceId='INV'+suffix
await call('/invoices',admin.token,'POST',{invoiceId,month:1,year:2027,rentAmount:3000000,electricityAmount:100000,waterAmount:50000,fineAmount:0,contractId:approved.contractId},201)
assert.equal((await call('/invoices',user.token)).length,1)
assert.equal((await call('/invoices',other.token)).length,0)
await call('/invoices/'+invoiceId,other.token,'GET',null,403)
await call('/payment-reports',other.token,'POST',{invoiceId,paymentMethod:'BANK_TRANSFER',transactionReference:'TEST'},403)
const payment=await call('/payment-reports',user.token,'POST',{invoiceId,transactionReference:'LOCAL-TEST'},201)
assert.equal(payment.amount,3150000)
await call('/payment-reports',user.token,'POST',{invoiceId,transactionReference:'AGAIN'},409)
await call('/payment-reports/'+payment.paymentReportId+'/status',user.token,'PATCH',{status:'PAID'},403)
await call('/payment-reports/'+payment.paymentReportId+'/status',admin.token,'PATCH',{status:'PAID'})
const tenant2={...person,cccd:'D'+suffix,fullName:'Trần Khách Mới'}
await call('/rentals',admin.token,'POST',{tenant:tenant2,roomId:room2,startDate:today,endDate:end,deposit:500000},403)
await call('/contracts',admin.token,'POST',{},403)
const request2=await call('/contract-requests',other.token,'POST',{tenant:tenant2,roomId:room2,requestedStartDate:today,requestedEndDate:end},201)
const rental2=await call('/contract-requests/'+request2.requestId+'/status',admin.token,'PATCH',{status:'APPROVED'})
assert.equal((await call('/contracts',other.token))[0].contractId,rental2.contractId)
assert.equal((await call('/contracts',user.token))[0].contractId,approved.contractId)
await call('/rooms/'+room3,admin.token,'PUT',{roomId:room3,floor:1,area:30,roomType:'Studio',status:'AVAILABLE',basePrice:3000000})
const competing=await Promise.all([[user,person],[other,tenant2]].map(([a,p])=>call('/contract-requests',a.token,'POST',{roomId:room3,requestedStartDate:today,requestedEndDate:end,tenant:p},201)))
const approvalStatuses=await Promise.all(competing.map(r=>fetch(base+'/api/contract-requests/'+r.requestId+'/status',{method:'PATCH',headers:{Authorization:'Bearer '+admin.token,'Content-Type':'application/json'},body:JSON.stringify({status:'APPROVED'})}).then(r=>r.status)))
assert.deepEqual(approvalStatuses.sort(),[200,409],'Exactly one concurrent approval may book a room');checks++
assert.equal((await call('/contracts',admin.token)).filter(c=>c.room.roomId===room3).length,1)
console.log(JSON.stringify({passed:checks,flows:['register roles','role permissions','tenant request approval','own contracts and invoices','incident reporting','payment confirmation','landlord cannot create rental on behalf of user','concurrent approval prevents double booking'],fixtures:{admin:admin.username,user:user.username,other:other.username}},null,2))
