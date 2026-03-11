<template>
  <div class="patient-app-container">
    <van-nav-bar :title="navTitle" fixed placeholder />

    <div class="main-body">
      <router-view v-slot="{ Component }">
        <transition name="van-fade">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>

    <van-tabbar v-model="activeTab" route active-color="#1989fa" inactive-color="#7d7e80">
      <van-tabbar-item replace to="/patient/home" icon="wap-home-o">挂号大厅</van-tabbar-item>
      <van-tabbar-item replace to="/patient/orders" icon="notes-o">我的订单</van-tabbar-item>
      <van-tabbar-item replace to="/patient/profile" icon="user-o">个人中心</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeTab = ref(0)
const navTitle = ref('预约挂号')

// 根据路由动态修改顶部标题
watch(() => route.path, (path) => {
  if (path.includes('home')) navTitle.value = '预约挂号大厅'
  else if (path.includes('orders')) navTitle.value = '我的挂号单'
  else if (path.includes('profile')) navTitle.value = '个人中心'
}, { immediate: true })
</script>

<style scoped lang="scss">
.patient-app-container {
  min-height: 100vh;
  background-color: #f7f8fa;
  
  .main-body {
    padding-bottom: 60px; /* 为底部导航留出空间 */
  }
}

/* 页面切换动画 */
.van-fade-enter-active,
.van-fade-leave-active {
  transition: opacity 0.3s;
}
.van-fade-enter-from,
.van-fade-leave-to {
  opacity: 0;
}
</style>