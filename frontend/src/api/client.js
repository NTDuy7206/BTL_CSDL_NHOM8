export async function request(path, { token = '', responseType, ...options } = {}) {
  const isFormData = typeof FormData !== 'undefined' && options.body instanceof FormData
  const headers = {
    Accept: responseType==='blob'?'image/*':'application/json',
    ...(options.body && !isFormData ? { 'Content-Type': 'application/json' } : {}),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  }
  let response
  try {
    response = await fetch(path, { ...options, headers, cache:'no-store', credentials:'omit' })
  } catch {
    throw new Error('Không kết nối được máy chủ. Hãy kiểm tra Spring Boot và thử lại.')
  }
  if(response.ok&&responseType==='blob')return response.blob()
  const raw = await response.text()
  let payload = null
  try { payload = raw ? JSON.parse(raw) : null } catch { payload = raw }
  if (!response.ok) {
    const message = typeof payload === 'string'
      ? payload
      : payload?.message || payload?.error || payload?.detail || `Yêu cầu thất bại (${response.status})`
    const error = new Error(message)
    error.status = response.status
    throw error
  }
  return payload
}

export async function signIn(username, password) {
  return request('/api/auth/login', { method: 'POST', body: JSON.stringify({ username, password }) })
}

export async function signUp(username, password, role,roomCount,rooms) {
  return request('/api/auth/register', { method: 'POST', body: JSON.stringify({ username, password, role,roomCount,rooms }) })
}
