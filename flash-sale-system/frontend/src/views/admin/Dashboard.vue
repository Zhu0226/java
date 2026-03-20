<template>
  <div class="dashboard">
    <div class="stats-grid">
      <div class="stat-card">
        <div class="label">今日秒杀订单</div>
        <div class="value">1,284</div>
        <div class="trend up">+12.5%</div>
      </div>
      <div class="stat-card">
        <div class="label">当前在线客诉</div>
        <div class="value">3</div>
        <div class="trend">正常</div>
      </div>
      <div class="stat-card highlight">
        <div class="label">库存警报商品</div>
        <div class="value">0</div>
        <div class="trend">安全</div>
      </div>
    </div>

    <div class="welcome-section">
      <h3>欢迎回来，{{ username }}</h3>
      <p>当前系统版本：Enterprise v3.2.0 | 运行状态：<span class="status-ok">健康</span></p>
      <div class="quick-actions">
        <button @click="router.push('/admin/goods')">快速发布商品</button>
        <button @click="router.push('/admin/orders')">查看未处理订单</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
const username = localStorage.getItem('username')
</script>

<style scoped>
.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-bottom: 30px; }
.stat-card { background: #fff; padding: 24px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.stat-card .label { color: #888; font-size: 14px; margin-bottom: 8px; }
.stat-card .value { font-size: 28px; font-weight: bold; color: #333; }
.stat-card .trend { font-size: 12px; margin-top: 8px; }
.trend.up { color: #52c41a; }
.highlight .value { color: #ff4d4f; }

.welcome-section { background: #fff; padding: 40px; border-radius: 8px; text-align: center; }
.status-ok { color: #52c41a; font-weight: bold; }
.quick-actions { margin-top: 24px; display: flex; justify-content: center; gap: 15px; }
.quick-actions button { padding: 10px 20px; background: #1890ff; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
</style>