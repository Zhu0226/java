import request from './request.js'

// 动态判断调用哪个接口
export function login(username, password, role) {
  // 如果选了员工，走 /auth/login；如果选了买家，走 /user/login
  const url = role === 'admin' ? '/auth/login' : '/user/login'
  return request.post(url, { username, password })
}