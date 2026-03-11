<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="sidebar pc-sidebar">
      <div class="logo-area">
        <span class="logo-icon">👑</span>
        <span class="logo-text">系统控制台</span>
      </div>
      <el-menu
        :default-active="route.path"
        class="el-menu-vertical"
        background-color="#1e1e2d"
        text-color="#b2bec3"
        active-text-color="#00a8ff"
        router
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><PieChart /></el-icon>
          <span>数据大盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/schedule-engine">
          <el-icon><Operation /></el-icon>
          <span>排班调度中心</span>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <el-icon><List /></el-icon>
          <span>全院订单监控</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="mobile-menu-btn" @click="drawerVisible = true"><Expand /></el-icon>
          <el-tag type="danger" effect="dark" round>🛠️ 超级管理员</el-tag>
        </div>
        <div class="header-right">
          <span class="welcome-text">欢迎您，Admin</span>
          <el-button type="danger" link @click="handleLogout">退出系统</el-button>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>

    <el-drawer v-model="drawerVisible" direction="ltr" size="220px" :with-header="false">
      <div class="sidebar" style="height: 100%; width: 100%; background-color: #1e1e2d;">
        <div class="logo-area">
          <span class="logo-icon">👑</span>
          <span class="logo-text">系统控制台</span>
        </div>
        <el-menu
          :default-active="route.path"
          class="el-menu-vertical"
          background-color="#1e1e2d"
          text-color="#b2bec3"
          active-text-color="#00a8ff"
          router
          @select="drawerVisible = false"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><PieChart /></el-icon>
            <span>数据大盘</span>
          </el-menu-item>
          <el-menu-item index="/admin/schedule-engine">
            <el-icon><Operation /></el-icon>
            <span>排班调度中心</span>
          </el-menu-item>
          <el-menu-item index="/admin/orders">
            <el-icon><List /></el-icon>
            <span>全院订单监控</span>
          </el-menu-item>
        </el-menu>
      </div>
    </el-drawer>
  </el-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { PieChart, Operation, List, Expand } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const drawerVisible = ref(false)

const handleLogout = async () => {
  try {
    await request.post('/api/auth/logout')
  } catch (e) {} finally {
    userStore.clearAuth()
    ElMessage.success('已安全退出')
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.admin-layout {
  height: 100vh;
  .sidebar {
    background-color: #1e1e2d;
    transition: width 0.3s;
    overflow-x: hidden;
    .logo-area {
      height: 60px; display: flex; align-items: center; justify-content: center;
      color: white; font-size: 18px; font-weight: bold; border-bottom: 1px solid #11111a;
      .logo-icon { margin-right: 8px; font-size: 22px; }
    }
    .el-menu-vertical { border-right: none; }
  }
  .header {
    background-color: #fff; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
    display: flex; align-items: center; justify-content: space-between; padding: 0 20px; z-index: 10;
    .header-left {
      display: flex; align-items: center;
      .mobile-menu-btn { display: none; font-size: 22px; cursor: pointer; margin-right: 15px; color: #606266; }
    }
    .header-right {
      display: flex; align-items: center; gap: 15px;
      .welcome-text { font-size: 14px; color: #606266; font-weight: 500; }
    }
  }
  .main-content { background-color: #f4f6f9; padding: 20px; }
}
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
:deep(.el-drawer__body) { padding: 0; background-color: #1e1e2d; }

@media screen and (max-width: 768px) {
  .pc-sidebar { width: 0 !important; display: none; }
  .admin-layout .header .header-left .mobile-menu-btn { display: block; }
  .welcome-text { display: none; }
  .admin-layout .header { padding: 0 15px; }
  .admin-layout .main-content { padding: 15px 10px; }
}
</style>