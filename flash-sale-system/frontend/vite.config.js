import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      '/seckill': { target: 'http://localhost:8081', changeOrigin: true },
      '/admin': { target: 'http://localhost:8081', changeOrigin: true },
      // 新增统一认证接口代理
      '/auth': { target: 'http://localhost:8081', changeOrigin: true },
      // 👇 新增下面这一段，让前端把 /user 开头的请求也乖乖交给后端处理
      '/user': { 
        target: 'http://localhost:8081', // 注意：如果你的后端实际在 8081 端口，请改成 8081
        changeOrigin: true 
      }
    }
  }
})