import assert from 'node:assert/strict'
import {matchesKeyword,normalizeKeyword} from '../frontend/src/data/search.js'
const row={contractId:'HD2f98ab701234',tenant:{fullName:'Nguyễn Thị Ánh',phoneNumber:'0912345678'},room:{roomId:'P_A1039',roomType:'Studio',status:'OCCUPIED'},startDate:'2026-10-09',deposit:3000000};
for(const q of ['nguyen','THI ANH','Nguyễn Ánh','2f98','103','phong 103','studio anh','0912','9/10/2026','09/10/2026','da thue','3.000.000','   '])assert.equal(matchesKeyword(row,q),true,q);
for(const q of ['nguyen binh','khong-ton-tai','%','user2'])assert.equal(matchesKeyword(row,q),false,q);
assert.equal(normalizeKeyword('  ĐẶNG   VĂN '),'dang van');
assert.equal(matchesKeyword({isCompleted:true},'hoan tat'),true);
assert.equal(matchesKeyword({isCompleted:false},'chua xong'),true);
assert.equal(matchesKeyword({password:'secret-value',fullName:'An'},'secret-value'),false);
console.log('21 keyword search checks passed');
