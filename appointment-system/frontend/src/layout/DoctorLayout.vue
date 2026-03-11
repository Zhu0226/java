<template>
  <el-container class="doctor-layout">
    <el-aside width="220px" class="sidebar pc-sidebar">
      <div class="logo-area">
        <span class="logo-icon">🏥</span>
        <span class="logo-text">医生工作台</span>
      </div>
      <el-menu
        :default-active="route.path"
        class="el-menu-vertical"
        background-color="#2c3e50"
        text-color="#fff"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/doctor/workbench">
          <el-icon><Monitor /></el-icon>
          <span>今日接诊</span>
        </el-menu-item>
        <el-menu-item index="/doctor/schedule">
          <el-icon><Calendar /></el-icon>
          <span>我的排班</span>
        </el-menu-item>
        <el-menu-item index="/doctor/history">
          <el-icon><DocumentChecked /></el-icon>
          <span>历史患者</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="mobile-menu-btn" @click="drawerVisible = true"><Expand /></el-icon>
          <el-tag type="success" effect="dark" round>👨‍⚕️ 医生专用端</el-tag>
        </div>
        <div class="header-right">
          <span class="welcome-text">欢迎您，测试医生</span>
          <el-button type="danger" link @click="handleLogout">退出登录</el-button>
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

    <el-drawer
      v-model="drawerVisible"
      direction="ltr"
      size="220px"
      :with-header="false"
    >
      <div class="sidebar" style="height: 100%; width: 100%;">
        <div class="logo-area">
          <span class="logo-icon">🏥</span>
          <span class="logo-text">系统菜单</span>
        </div>
        <el-menu
          :default-active="route.path"
          class="el-menu-vertical"
          background-color="#2c3e50"
          text-color="#fff"
          active-text-color="#409eff"
          router
          @select="drawerVisible = false" 
        >
          <el-menu-item index="/doctor/workbench">
            <el-icon><Monitor /></el-icon>
            <span>今日接诊</span>
          </el-menu-item>
          <el-menu-item index="/doctor/schedule">
            <el-icon><Calendar /></el-icon>
            <span>我的排班</span>
          </el-menu-item>
          <el-menu-item index="/doctor/history">
            <el-icon><DocumentChecked /></el-icon>
            <span>历史患者</span>
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
// 【引入 Expand 汉堡折叠图标】
import { Monitor, Calendar, DocumentChecked, Expand } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 控制移动端抽屉显示/隐藏的开关
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
.doctor-layout {
  height: 100vh;
  
  .sidebar {
    background-color: #2c3e50;
    transition: width 0.3s;
    overflow-x: hidden;

    .logo-area {
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 20px;
      font-weight: bold;
      border-bottom: 1px solid #1a252f;
      
      .logo-icon { margin-right: 8px; font-size: 24px; }
    }
    
    .el-menu-vertical {
      border-right: none;
    }
  }

  .header {
    background-color: #fff;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    z-index: 10;

    .header-left {
      display: flex;
      align-items: center;
      
      /* 移动端汉堡菜单按钮默认隐藏 */
      .mobile-menu-btn {
        display: none; 
        font-size: 22px;
        cursor: pointer;
        margin-right: 15px;
        color: #606266;
      }
      .mobile-menu-btn:active { color: #409eff; }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 15px;
      .welcome-text { font-size: 14px; color: #606266; font-weight: 500; }
    }
  }

  .main-content {
    background-color: #f0f2f5;
    padding: 20px;
  }
}

/* 路由切换动画 */
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* 消除抽屉自带的内边距，让菜单完全贴合 */
:deep(.el-drawer__body) {
  padding: 0;
  background-color: #2c3e50;
}

/* ========== 响应式魔法：当屏幕小于 768px 时 ========== */
@media screen and (max-width: 768px) {
  /* 隐藏 PC 端侧边栏 */
  .pc-sidebar { width: 0 !important; display: none; }
  
  /* 显示移动端汉堡菜单按钮 */
  .doctor-layout .header .header-left .mobile-menu-btn { display: block; }
  
  /* 移动端空间小，把“欢迎您，测试医生”这几个字藏起来，只留退出按钮 */
  .welcome-text { display: none; }
  
  .doctor-layout .header { padding: 0 15px; }
  .doctor-layout .main-content { padding: 15px 10px; }
}
</style>