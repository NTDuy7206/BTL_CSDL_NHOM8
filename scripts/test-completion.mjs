import assert from 'node:assert/strict'
import{isComplete,visibleWork}from '../frontend/src/data/completion.js'
for(const module of ['incidents','incident-resolutions','cleaning-supports']){
 const pending=module==='incidents'?{status:'PENDING'}:{isCompleted:false};const done=module==='incidents'?{status:'COMPLETED'}:{isCompleted:true};
 assert.deepEqual(visibleWork(module,[pending,done],'pending'),[pending]);assert.deepEqual(visibleWork(module,[pending,done],'completed'),[done]);assert.deepEqual(visibleWork(module,[done],'pending'),[]);assert.equal(isComplete(module,pending),false);
}
console.log('12 completion and history checks passed');
