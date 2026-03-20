<template>
  <div class="admin-container">
    <header class="admin-header">
      <h1>🎧 客服诊断工作台</h1>
      <p class="subtitle">输入买家ID，进行秒杀链路诊断与人工干预</p>
    </header>

    <div class="workspace-layout">
      <aside class="side-panel jd-card">
        <h3>🔍 用户检索</h3>
        <div class="search-v-stack">
          <input 
            v-model.number="searchUserId" 
            type="number" 
            class="jd-input diagnostic-input" 
            placeholder="请输入买家 ID"
            @keyup.enter="doDiagnostic"
          />
          <button class="jd-btn search-btn-huge" @click="doDiagnostic" :disabled="loading">
            {{ loading ? '诊断中...' : '启动诊断' }}
          </button>
        </div>
        <div class="guide-box">
          <h4>💡 操作指引</h4>
          <p>若用户反馈无法下单，请先在此查询是否有未处理的待支付订单。</p>
        </div>
      </aside>

      <main class="main-result-area">
        <div class="jd-card result-container">
          <h3>📊 诊断结果 <span v-if="lastSearchedId" class="target-tag">ID: {{ lastSearchedId }}</span></h3>
          
          <div v-if="!hasSearched" class="empty-placeholder">等待输入 ID 开始诊断...</div>
          <div v-else-if="userOrders.length === 0" class="empty-placeholder">该用户暂无订单记录</div>
          
          <div v-else class="order-cards-list">
            <div v-for="o in userOrders" :key="o.id" class="order-item-card">
              <div class="card-top">
                <span class="order-no">订单#{{ o.id }}</span>
                <span :class="statusColorClass(o.status)">{{ statusText(o.status) }}</span>
              </div>
              <div class="card-mid">
                <p>商品ID: {{ o.goodsId }}</p>
                <p>时间: {{ formatTime(o.createTime) }}</p>
              </div>
              <div class="card-bottom">
                <button 
                  v-if="o.status !== 2" 
                  class="btn-danger-outline"
                  @click="doCancel(o.id)"
                >
                  🛠️ 强制取消并释放库存
                </button>
                <span v-else class="text-muted">订单已关闭</span>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { listAdminOrders, cancelAdminOrder } from '../api/admin.js'

const searchUserId = ref('')
const lastSearchedId = ref('')
const hasSearched = ref(false)
const loading = ref(false)
const userOrders = ref([])

const statusText = (s) => ({ 0: '待支付', 1: '已支付', 2: '已关闭' }[s] || '未知')
const statusColorClass = (s) => ({ 0: 'color-warn', 1: 'color-success', 2: 'color-muted' }[s] || '')
const formatTime = (t) => t ? new Date(t).toLocaleString() : '-'

async function doDiagnostic() {
  if (!searchUserId.value) return
  loading.value = true
  lastSearchedId.value = searchUserId.value
  const res = await listAdminOrders({ userId: searchUserId.value, page: 1, size: 20 })
  if (res.code === 0) {
    userOrders.value = res.data.records || []
    hasSearched.value = true
  }
  loading.value = false
}

async function doCancel(orderId) {
  if (!confirm('确定执行人工干预？')) return
  const res = await cancelAdminOrder(orderId)
  if (res.code === 0) { alert('干预成功'); doDiagnostic() }
}
</script>

<style scoped>
.admin-container { padding: 24px; background: #f4f4f4; min-height: calc(100vh - 110px); }
.workspace-layout { display: flex; gap: 24px; max-width: 1100px; margin: 0 auto; align-items: flex-start; }
.side-panel { flex: 0 0 300px; padding: 20px; background: #fff; border-radius: 8px; position: sticky; top: 20px; }
.search-v-stack { display: flex; flex-direction: column; gap: 12px; }
.main-result-area { flex: 1; min-width: 0; }
.result-container { padding: 20px; background: #fff; border-radius: 8px; min-height: 500px; }
.order-item-card { border: 1px solid #eee; padding: 15px; margin-bottom: 15px; border-radius: 8px; }
.card-top { display: flex; justify-content: space-between; margin-bottom: 10px; font-weight: bold; }
.btn-danger-outline { border: 1px solid #e1251b; color: #e1251b; background: #fff; padding: 5px 15px; cursor: pointer; border-radius: 4px; }
.btn-danger-outline:hover { background: #e1251b; color: #fff; }
.color-warn { color: #faad14; }
.color-success { color: #52c41a; }
.target-tag { font-size: 12px; background: #e6f7ff; padding: 2px 8px; border-radius: 10px; color: #1890ff; }
</style>