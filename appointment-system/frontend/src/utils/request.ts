import axios from 'axios'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'
import router from '../router'

const service = axios.create({
  baseURL: 'http://localhost:8080', // 后端地址
  timeout: 10000
})

// 无感刷新核心变量
let isRefreshing = false
let requestsQueue: any[] = []

// 请求拦截器
service.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.accessToken) {
      config.headers['Authorization'] = `Bearer ${userStore.accessToken}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    // 后端统一定义 200 为成功
    if (res.code === 200) {
      return res.data
    } else {
      ElMessage.error(res.message || '系统错误')
      return Promise.reject(new Error(res.message || 'Error'))
    }
  },
  async error => {
    const userStore = useUserStore()
    const originalRequest = error.config

    // 拦截到后端抛出的 401 错误 (Token过期或无效)
    if (error.response && error.response.status === 401) {
      const msg = error.response.data.message

      // 情况 1: Access Token 过期，触发无感刷新
      if (msg === 'token_expired' && !originalRequest._retry) {
        if (isRefreshing) {
          // 如果正在刷新，把当前请求挂起放入队列
          return new Promise((resolve) => {
            requestsQueue.push((token: string) => {
              originalRequest.headers['Authorization'] = `Bearer ${token}`
              resolve(service(originalRequest))
            })
          })
        }

        originalRequest._retry = true
        isRefreshing = true

        try {
          // 拿着 Refresh Token 去换新 Token
          const { data } = await axios.post(`http://localhost:8080/api/auth/refresh?refreshToken=${userStore.refreshToken}`)
          if (data.code === 200) {
            const newAccess = data.data.accessToken
            userStore.setTokens(newAccess, data.data.refreshToken)
            
            // 执行队列中积压的请求
            requestsQueue.forEach(cb => cb(newAccess))
            requestsQueue = []
            
            // 重试当前报错的请求
            originalRequest.headers['Authorization'] = `Bearer ${newAccess}`
            return service(originalRequest)
          }
        } catch (refreshError) {
          // 刷新失败 (比如 Refresh Token 也过期了)，踢回登录页
          userStore.clearAuth()
          router.push('/login')
          ElMessage.error('登录已失效，请重新登录')
          return Promise.reject(refreshError)
        } finally {
          isRefreshing = false
        }
      } else {
        // 情况 2: 凭证彻底无效被拦截
        userStore.clearAuth()
        router.push('/login')
        ElMessage.error('没有权限或未登录')
      }
    } else {
      ElMessage.error(error.message)
    }
    return Promise.reject(error)
  }
)

export default service