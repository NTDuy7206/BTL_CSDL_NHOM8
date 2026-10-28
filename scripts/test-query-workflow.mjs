import assert from 'node:assert/strict'
const base=process.env.TEST_BASE_URL||'http://127.0.0.1:18082'
if(!['localhost','127.0.0.1'].includes(new URL(base).hostname))throw new Error('Use only an isolated local database')
let checks=0;const suffix=Date.now().toString(36),password='QueryTest-2026!'
async function call(path,token='',method='GET',body,status=200){const r=await fetch(base+'/api'+path,{method,headers:{'Content-Type':'application/json',...(token?{Authorization:'Bearer '+token}:{})},...(body?{body:JSON.stringify(body)}:{})});const raw=await r.text();let data;try{data=raw?JSON.parse(raw):null}catch{data=raw}assert.equal(r.status,status,method+' '+path+' '+raw);checks++;return data}
async function account(role,s){const username='query_'+suffix+s;await call('/auth/register','','POST',{username,password,role},201);return {username,token:(await call('/auth/login','','POST',{username,password})).accessToken}}
const admin=await account('ADMIN','a'),user=await account('USER','u'),other=await account('USER','o')
for(const [table,id] of [['tenants','DEMO_KT01'],['employees','DEMO_NV01'],['rooms','DEMO_P01'],['contracts','DEMO_HD01'],['invoices','DEMO_HDon01'],['utility-readings','DEMO_CS01'],['incidents','DEMO_SC01'],['incident-resolutions','DEMO_XL01'],['cleaning-supports','DEMO_DD01'],['rules','DEMO_NQ01'],['violations','DEMO_VP01']]){assert.ok((await call('/'+table,admin.token)).length>=10);assert.ok(await call('/'+table+'/'+id,admin.token))}
const queries=[['tenants','name','An'],['employees','name','An'],['rooms','status','AVAILABLE'],['rooms','type','Studio'],['rooms','max-price','3000000'],['contracts','expired'],['contracts','tenant','DEMO_KT01'],['contracts','room','DEMO_P01'],['invoices','contract','DEMO_HD01'],['incidents','room','DEMO_P01'],['incidents','status','PENDING'],['incident-resolutions','incident','DEMO_SC01'],['incident-resolutions','employee','DEMO_NV01'],['incident-resolutions','uncompleted'],['cleaning-supports','room','DEMO_P01'],['cleaning-supports','employee','DEMO_NV01'],['cleaning-supports','uncompleted'],['utility-readings','room','DEMO_P01'],['violations','room','DEMO_P01']]
for(const[m,a,v]of queries){const result=await call('/queries/'+m+'/'+a+(v?'?value='+encodeURIComponent(v):''),admin.token);assert.ok(Array.isArray(result)&&result.length>0,m+'/'+a+' should have sample rows')}
const total=await call('/queries/invoices/total',admin.token);assert.ok(total.amount>0)
for(const m of ['invoices','contracts','tenants','utility-readings','incident-resolutions','cleaning-supports'])await call('/queries/'+m+'/room?value=DEMO_P01',user.token,'GET',null,403)
for(const[m,a,v]of queries.filter(([m])=>['rooms','employees','incidents','violations'].includes(m)))await call('/queries/'+m+'/'+a+'?value='+encodeURIComponent(v),user.token)
await call('/queries/rooms/max-price?value=-1',user.token,'GET',null,400)
await call('/queries/rooms/max-price?value=abc',user.token,'GET',null,400)
await call('/queries/rooms/type',user.token,'GET',null,400)
await call('/queries/rooms/unknown',user.token,'GET',null,404)
await call('/incident-resolutions/DEMO_XL06/complete',admin.token,'PUT')
assert.equal((await call('/incidents/DEMO_SC06',user.token)).status,'COMPLETED')
await call('/cleaning-supports/DEMO_DD02/complete',admin.token,'PUT')
assert.equal((await call('/cleaning-supports/DEMO_DD02',admin.token)).isCompleted,true)
await call('/incident-resolutions/DEMO_XL07/complete',user.token,'PUT',null,403)
const demo=await call('/demo/my-rental',user.token,'POST');assert.equal(demo.created,true)
assert.equal((await call('/demo/my-rental',user.token,'POST')).created,false)
assert.equal((await call('/contracts',user.token)).length,1)
assert.equal((await call('/invoices',user.token)).length,1)
assert.equal((await call('/contracts',other.token)).length,0)
await call('/contracts/'+demo.contractId,other.token,'GET',null,403)
await call('/demo/my-rental',admin.token,'POST',null,403)
console.log(JSON.stringify({passed:checks,admin:admin.username,user:user.username,queries:queries.length+1},null,2))
