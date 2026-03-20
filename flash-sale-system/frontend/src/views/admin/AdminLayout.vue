<template>
  <div class="admin-shell">
    <aside class="admin-aside">
      <div class="aside-brand">
        <span class="brand-icon">⚡</span>
        <span class="brand-text">双喜后台管理</span>
      </div>
      
      <nav class="aside-nav">
        <router-link v-if="isAdmin" to="/admin/dashboard" class="nav-link">
          <span class="nav-icon">🏠</span> 管理概览
        </router-link>
        
        <div class="nav-section" v-if="isAdmin">
          <div class="section-title">核心经营</div>
          <router-link to="/admin/goods" class="nav-link">
            <span class="nav-icon">📦</span> 商品中心
          </router-link>
          <router-link to="/admin/metrics" class="nav-link">
            <span class="nav-icon">📈</span> 秒杀指标
          </router-link>
        </div>

        <div class="nav-section" v-if="isAdmin || isKefu">
          <div class="section-title">全局调度</div>
          <router-link to="/admin/orders" class="nav-link">
            <span class="nav-icon">📋</span> 全局订单调度
          </router-link>
          <router-link to="/admin/customers" class="nav-link">
            <span class="nav-icon">🛠️</span> 异常链路诊断
          </router-link>
        </div>
      </nav>

      <div class="aside-footer">
        <div class="admin-badge">
          <div class="badge-avatar">{{ username.charAt(0).toUpperCase() }}</div>
          <div class="badge-info">
            <p class="badge-name">{{ username }}</p>
            <p class="badge-role">{{ isAdmin ? '超级管理员' : '客服专员' }}</p>
          </div>
        </div>
        <button class="exit-btn" @click="handleLogout">安全登出</button>
      </div>
    </aside>

    <main class="admin-body">
      <header class="admin-topbar">
        <div class="page-location">当前位置：{{ route.meta.title }}</div>
        <div class="topbar-right">
          <button @click="router.push('/')" class="ghost-btn">🌐 访问商城前端</button>
        </div>
      </header>
      
      <section class="admin-stage">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { ref } from 'vue'

const router = useRouter()
const route = useRoute()
const username = ref(localStorage.getItem('username') || 'Admin')

// 1. 获取当前用户的权限数组
const perms = ref(JSON.parse(localStorage.getItem('perms') || '[]'))

// 2. 定义角色判断逻辑
// 具有 admin:view 或 admin:goods:list 均视为管理员
const isAdmin = ref(perms.value.includes('admin:view') || perms.value.includes('admin:goods:list'))
// 具有 kefu:view 视为客服
const isKefu = ref(perms.value.includes('kefu:view'))

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('username')
  localStorage.removeItem('perms')
  router.push('/login')
}
</script>

<style scoped>
.admin-shell { display: flex; height: 100vh; width: 100vw; background: #f0f2f5; overflow: hidden; }

/* 侧边栏：深色系专业风格 */
.admin-aside { width: 260px; background: #001529; color: #fff; display: flex; flex-direction: column; z-index: 100; }
.aside-brand { padding: 24px; display: flex; align-items: center; gap: 12px; background: #002140; font-size: 18px; font-weight: bold; border-bottom: 1px solid #000; }
.brand-icon { font-size: 24px; color: #1890ff; }

.aside-nav { flex: 1; padding: 20px 0; overflow-y: auto; }
.nav-section { margin-top: 25px; }
.section-title { padding: 0 24px; font-size: 12px; color: rgba(255,255,255,0.45); letter-spacing: 1px; margin-bottom: 10px; text-transform: uppercase; }

.nav-link { display: flex; align-items: center; gap: 12px; padding: 14px 24px; color: rgba(255,255,255,0.65); text-decoration: none; transition: 0.2s; font-size: 14px; }
.nav-link:hover { color: #fff; background: rgba(255,255,255,0.05); }
.router-link-active { color: #fff !important; background: #1890ff !important; font-weight: bold; }
.nav-icon { font-size: 18px; opacity: 0.8; }

.aside-footer { padding: 20px; background: #002140; border-top: 1px solid rgba(255,255,255,0.1); }
.admin-badge { display: flex; align-items: center; gap: 12px; margin-bottom: 15px; }
.badge-avatar { width: 36px; height: 36px; background: #1890ff; border-radius: 4px; display: flex; align-items: center; justify-content: center; font-weight: bold; }
.badge-info p { margin: 0; line-height: 1.4; }
.badge-name { font-size: 14px; color: #fff; }
.badge-role { font-size: 12px; color: rgba(255,255,255,0.45); }
.exit-btn { width: 100%; height: 34px; background: transparent; border: 1px solid rgba(255,255,255,0.2); color: #fff; border-radius: 4px; cursor: pointer; transition: 0.2s; }
.exit-btn:hover { border-color: #ff4d4f; color: #ff4d4f; }

/* 内容区 */
.admin-body { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.admin-topbar { height: 64px; background: #fff; display: flex; align-items: center; justify-content: space-between; padding: 0 24px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); z-index: 90; }
.page-location { font-size: 14px; color: #666; font-weight: 500; }
.ghost-btn { background: #fff; border: 1px solid #d9d9d9; padding: 6px 16px; border-radius: 4px; cursor: pointer; font-size: 13px; color: #666; }
.ghost-btn:hover { color: #1890ff; border-color: #1890ff; }

.admin-stage { flex: 1; padding: 24px; overflow-y: auto; box-sizing: border-box; }
</style>