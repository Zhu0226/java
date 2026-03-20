<template>
  <div class="admin-container">
    <header class="admin-header">
      <div class="title-row">
        <h1>📋 订单管理台</h1>
        <router-link 
          :to="hasEditPerm ? '/admin/goods' : '/admin/customers'" 
          class="back-link"
        >
          {{ hasEditPerm ? '📦 返回商品管理' : '🎧 返回客服工作台' }}
        </router-link>
      </div>
    </header>

    <div class="search-panel jd-card">
      <div class="filter-row">
        <div class="filter-group">
          <label>订单编号</label>
          <input v-model.number="filters.orderId" type="number" placeholder="精确搜索" class="jd-input" />
        </div>
        <div class="filter-group">
          <label>买家ID</label>
          <input v-model.number="filters.userId" type="number" placeholder="买家ID" class="jd-input" />
        </div>
        <div class="filter-group">
          <label>订单状态</label>
          <select v-model="filters.status" class="jd-input">
            <option value="">全部状态</option>
            <option :value="0">待支付</option>
            <option :value="1">已支付</option>
            <option :value="2">已取消</option>
          </select>
        </div>
        <div class="filter-actions">
          <button class="jd-btn" @click="handleSearch">查询</button>
          <button class="jd-btn btn-default" @click="resetFilters">重置</button>
        </div>
      </div>
    </div>

    <div class="table-area jd-card">
      <div v-if="loading" class="status-tip">数据加载中...</div>
      <div v-else-if="list.length === 0" class="status-tip empty">未找到相关订单记录</div>
      <div v-else class="table-responsive">
        <table class="admin-table">
          <thead>
            <tr>
              <th>订单编号</th>
              <th>买家ID</th>
              <th>状态</th>
              <th>创建时间</th>
              <th class="text-center">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="o in list" :key="o.id">
              <td class="font-mono">#{{ o.id }}</td>
              <td>{{ o.userId }}</td>
              <td>
                <span class="status-badge" :class="statusClass(o.status)">
                  {{ statusText(o.status) }}
                </span>
              </td>
              <td>{{ formatTime(o.createTime) }}</td>
              <td class="text-center">
                <button v-if="o.status !== 2" class="jd-btn btn-danger btn-sm" @click="doForceCancel(o.id)">
                  强制取消
                </button>
                <span v-else class="text-muted">已处理</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination-bar" v-if="total > 0">
        <span>共 {{ total }} 条</span>
        <div class="page-controls">
          <button :disabled="page <= 1" @click="changePage(page - 1)">上一页</button>
          <span>{{ page }} / {{ Math.ceil(total/size) }}</span>
          <button :disabled="page >= Math.ceil(total/size)" @click="changePage(page + 1)">下一页</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { listAdminOrders, cancelAdminOrder } from '../api/admin.js'

const perms = JSON.parse(localStorage.getItem('perms') || '[]')
const hasEditPerm = computed(() => perms.includes('admin:goods:edit'))

const loading = ref(true)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const filters = reactive({ orderId: '', userId: '', status: '' })

const statusText = (s) => ({ 0: '待支付', 1: '已支付', 2: '已取消' }[s] || '未知')
const statusClass = (s) => ({ 0: 'badge-warn', 1: 'badge-success', 2: 'badge-muted' }[s] || '')
const formatTime = (t) => t ? new Date(t).toLocaleString() : '-'

async function loadData() {
  loading.value = true
  const params = { page: page.value, size: size.value, ...filters }
  const res = await listAdminOrders(params)
  if (res.code === 0) {
    list.value = res.data.records || []
    total.value = res.data.total || 0
  }
  loading.value = false
}

const handleSearch = () => { page.value = 1; loadData() }
const resetFilters = () => { Object.keys(filters).forEach(k => filters[k] = ''); handleSearch() }
const changePage = (p) => { page.value = p; loadData() }

async function doForceCancel(orderId) {
  if (!confirm(`确定要强制取消订单 #${orderId} 吗？`)) return
  const res = await cancelAdminOrder(orderId)
  if (res.code === 0) { alert('操作成功'); loadData() }
}
onMounted(loadData)
</script>

<style scoped>
.admin-container { padding: 24px; background: #f4f4f4; min-height: calc(100vh - 110px); }
.title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.back-link { color: #1890ff; text-decoration: none; font-size: 14px; }
.search-panel { padding: 20px; margin-bottom: 20px; background: #fff; border-radius: 8px; }
.filter-row { display: flex; gap: 20px; align-items: flex-end; }
.filter-group { display: flex; flex-direction: column; gap: 8px; }
.jd-input { width: 160px; height: 34px; border: 1px solid #ddd; border-radius: 4px; padding: 0 10px; }
.table-area { background: #fff; padding: 20px; border-radius: 8px; }
.admin-table { width: 100%; border-collapse: collapse; }
.admin-table th { background: #fafafa; padding: 12px; text-align: left; }
.admin-table td { padding: 12px; border-bottom: 1px solid #f0f0f0; }
.status-badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.badge-warn { background: #fff7e6; color: #fa8c16; }
.badge-success { background: #f6ffed; color: #52c41a; }
.pagination-bar { display: flex; justify-content: space-between; margin-top: 20px; }
.page-controls { display: flex; gap: 10px; align-items: center; }
</style>