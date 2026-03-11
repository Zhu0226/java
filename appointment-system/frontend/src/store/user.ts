import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 1. 初始化时从 sessionStorage 读取
  const accessToken = ref(sessionStorage.getItem('accessToken') || '')
  const refreshToken = ref(sessionStorage.getItem('refreshToken') || '')
  const role = ref(sessionStorage.getItem('role') || '')

  const setTokens = (access: string, refresh: string) => {
    accessToken.value = access
    refreshToken.value = refresh
    // 2. 存入 sessionStorage (仅当前标签页有效)
    sessionStorage.setItem('accessToken', access)
    sessionStorage.setItem('refreshToken', refresh)
    
    try {
      const [, payloadBase64] = access.split('.')
      
      if (payloadBase64) {
        const payload = JSON.parse(atob(payloadBase64))
        role.value = payload.role
        sessionStorage.setItem('role', payload.role)
      }
    } catch (e) {
      console.error('Token 解析失败', e)
    }
  }

  const clearAuth = () => {
    accessToken.value = ''
    refreshToken.value = ''
    role.value = ''
    // 3. 退出时清理 sessionStorage
    sessionStorage.removeItem('accessToken')
    sessionStorage.removeItem('refreshToken')
    sessionStorage.removeItem('role')
  }

  return { accessToken, refreshToken, role, setTokens, clearAuth }
})