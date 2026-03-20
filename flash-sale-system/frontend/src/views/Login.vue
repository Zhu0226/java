<template>
  <div class="login-page">
    <header class="login-header container">
      <div class="logo-area" @click="$router.push('/')">
        <img src="/logo.png" alt="双喜" class="logo-img" onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';" />
        <div class="logo-fallback" style="display: none;">
          <span class="icon">⚡</span> 双喜
        </div>
        <span class="welcome-text">欢迎登录</span>
      </div>
      <div class="header-feedback">
        <a href="#">💬 登录页面，调查问卷</a>
      </div>
    </header>

    <div class="login-body">
      <div class="login-bg-wrapper">
        <div class="container login-content">
          
          <div class="login-card">
            <div class="main-tabs">
              <div class="tab-item" :class="{ active: role === 'user' }" @click="role = 'user'">
                买家登录
              </div>
              <div class="tab-divider"></div>
              <div class="tab-item" :class="{ active: role === 'admin' }" @click="role = 'admin'">
                员工登录
              </div>
            </div>

            <div class="card-content">
              <form class="login-form" @submit.prevent="handleLogin">
                <div class="input-group">
                  <span class="input-icon">👤</span>
                  <input 
                    v-model="username" 
                    type="text" 
                    placeholder="请输入用户名" 
                    required 
                  />
                </div>
                
                <div class="input-group">
                  <span class="input-icon">🔒</span>
                  <input 
                    v-model="password" 
                    type="password" 
                    placeholder="请输入密码" 
                    required 
                  />
                </div>

                <div v-if="error" class="error-msg">
                  <span>⚠️</span> {{ error }}
                </div>

                <button type="submit" class="submit-btn" :disabled="loading">
                  {{ loading ? '登录中...' : '登 录' }}
                </button>
              </form>

              <div class="form-footer">
                <div class="links">
                  <a href="#">忘记密码</a>
                  <span class="divider">|</span>
                  <a href="#" @click.prevent="handleRegisterClick">免费注册</a>
                </div>
              </div>

            </div>
          </div>

        </div>
      </div>
    </div>

    <footer class="login-footer">
      <div class="footer-links">
        <a href="#">关于我们</a> | <a href="#">联系我们</a> | <a href="#">人才招聘</a> | <a href="#">商家入驻</a> | <a href="#">广告服务</a>
      </div>
      <div class="copyright">
        Copyright © 2004-2026 双喜 版权所有
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { login } from '../api/auth.js' 

const router = useRouter()
const route = useRoute()

const role = ref('user')
const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

onMounted(() => {
  if (route.query.role === 'admin') {
    role.value = 'admin'
    username.value = 'admin'
  }
})

function handleRegisterClick() {
  alert('系统已开启企业级 RBAC 权限控制，暂不支持外部自行注册。请联系系统管理员分配账号！')
}

async function handleLogin() {
  error.value = ''
  loading.value = true

  try {
    const res = await login(username.value, password.value, role.value)
    
    if (res.code === 0 && res.data?.token) {
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('userId', res.data.userId)
      localStorage.setItem('username', res.data.realName || res.data.username)
      localStorage.setItem('perms', JSON.stringify(res.data.perms || []))

      const perms = res.data.perms || []
      const targetPath = route.query.redirect

      // 智能分流
      if (perms.includes('admin:view') || perms.includes('admin:goods:list')) {
        router.push(targetPath && targetPath !== '/' ? targetPath : '/admin/dashboard')
      } else if (perms.includes('kefu:view')) {
        router.push(targetPath && targetPath !== '/' ? targetPath : '/admin/orders')
      } else {
        router.push(targetPath || '/')
      }
      
    } else {
      throw new Error(res.msg || '登录失败')
    }
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.login-header {
  height: 80px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1000px;
  margin: 0 auto;
  width: 100%;
}
.logo-area { display: flex; align-items: center; gap: 15px; cursor: pointer; }
.logo-img { max-height: 50px; }
.logo-fallback { font-size: 32px; font-weight: 900; color: #e1251b; letter-spacing: 2px; display: flex; align-items: center; gap: 8px;}
.welcome-text { font-size: 24px; color: #333; margin-left: 10px; }

.header-feedback a { color: #999; font-size: 12px; text-decoration: none; }
.header-feedback a:hover { color: #e1251b; }

.login-body {
  flex: 1;
}

.login-bg-wrapper {
  height: 475px;
  width: 100%;
  background: #e1251b; /* 经典京东红底色 */
  position: relative;
  overflow: hidden;
}
.login-bg-wrapper::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  /* 加一层微妙的渐变，让纯色不那么死板 */
  background: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(0,0,0,0.15) 100%);
}

.login-content {
  position: relative;
  height: 100%;
  max-width: 1000px;
  margin: 0 auto;
}

.login-card {
  position: absolute;
  right: 0;
  top: 40px;
  width: 350px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  overflow: hidden;
  z-index: 10;
}

.main-tabs {
  display: flex;
  height: 55px;
  border-bottom: 1px solid #f4f4f4;
}
.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #666;
  cursor: pointer;
  font-weight: bold;
  transition: color 0.2s;
}
.tab-item:hover { color: #e1251b; }
.tab-item.active { color: #e1251b; }
.tab-divider { width: 1px; height: 20px; background: #eee; margin-top: 17px; }

.card-content { padding: 25px 20px; }

.input-group {
  display: flex;
  align-items: center;
  border: 1px solid #ccc;
  height: 42px;
  margin-bottom: 20px;
  border-radius: 2px;
  background: #fff;
  transition: border-color 0.2s;
}
.input-group:focus-within { border-color: #999; }
.input-icon { width: 40px; text-align: center; color: #999; font-size: 16px; border-right: 1px solid #eee; }
.input-group input {
  flex: 1; height: 100%; border: none; padding: 0 10px; outline: none; font-size: 14px; box-sizing: border-box;
}

.error-msg {
  color: #e1251b; font-size: 12px; margin-bottom: 15px; display: flex; align-items: center; gap: 5px;
  background: #ffebeb; padding: 6px 10px; border: 1px solid #faccc6; border-radius: 2px;
}

.submit-btn {
  width: 100%; height: 46px; font-size: 20px; font-weight: bold; letter-spacing: 5px;
  background: #e1251b; color: #fff; border: none; border-radius: 2px; cursor: pointer; transition: 0.2s;
}
.submit-btn:hover { background: #c81623; }
.submit-btn:disabled { background: #f59b98; cursor: not-allowed; }

.form-footer { display: flex; justify-content: flex-end; margin-top: 15px; font-size: 12px; }
.links a { color: #999; text-decoration: none; }
.links a:hover { color: #e1251b; }
.links .divider { margin: 0 10px; color: #eee; }

.login-footer { text-align: center; padding: 40px 0; }
.footer-links { margin-bottom: 10px; font-size: 12px; }
.footer-links a { color: #666; margin: 0 10px; text-decoration: none; }
.footer-links a:hover { color: #e1251b; text-decoration: underline;}
.copyright { font-size: 12px; color: #999; }
</style>