import assert from 'node:assert/strict'
import {monthRange,shiftMonth,reportRange} from '../frontend/src/data/finance.js'
assert.deepEqual(monthRange('2026-10'),{from:'2026-10-01',to:'2026-10-31'})
assert.deepEqual(monthRange('2024-02'),{from:'2024-02-01',to:'2024-02-29'})
assert.equal(shiftMonth('2026-01',-1),'2025-12')
assert.equal(shiftMonth('2026-12',1),'2027-01')
assert.deepEqual(reportRange('2026-10-05','day'),{from:'2026-10-01',to:'2026-10-31'})
assert.deepEqual(reportRange('2026-10-05','month'),{from:'2026-01-01',to:'2026-12-31'})
console.log('Historical month, month-end, leap year and year boundary checks passed')
