<template>
  <div class="order-list-container">
    <van-tabs v-model:active="activeTab" sticky @click-tab="onTabChange">
      <van-tab title="全部" name="-1"></van-tab>
      <van-tab title="待支付" name="0"></van-tab>
      <van-tab title="待就诊" name="1"></van-tab>
      <van-tab title="已完成" name="2"></van-tab>
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div class="list-content">
        <van-empty v-if="filteredOrders.length === 0" description="暂无挂号记录" />
        
        <van-card
          v-for="order in filteredOrders"
          :key="order.orderNo"
          class="order-card"
        >
          <template #title>
            <div class="card-header">
              <span class="dept-name">{{ order.deptName }}</span>
              <van-tag :type="getStatusTag(order.orderStatus)">
                {{ getStatusText(order.orderStatus) }}
              </van-tag>
            </div>
          </template>

          <template #desc>
            <div class="card-body">
              <p>就诊医生：{{ order.doctorName }}</p>
              <p>挂号金额：<span class="price">￥{{ order.amount }}</span></p>
              <p class="time">下单时间：{{ formatTime(order.createTime) }}</p>
              <p class="order-no">订单号：{{ order.orderNo }}</p>
            </div>
          </template>

          <template #footer>
            <div class="card-footer">
              <van-button 
                v-if="order.orderStatus <= 1" 
                size="small" 
                plain 
                round
                @click="handleCancel(order.orderNo)"
              >取消预约</van-button>
              
              <van-button 
                v-if="order.orderStatus === 0" 
                size="small" 
                type="primary" 
                round
                @click="handlePay(order.orderNo)"
              >立即支付</van-button>
            </div>
          </template>
        </van-card>
      </div>
    </van-pull-refresh>
  </div>
</template>

<script setup lang="ts">
// 1. 在顶部引入相关的 Vant 轻提示组件，加入了 showLoadingToast 和 showSuccessToast
import { ref, onMounted, computed } from 'vue'
import request from '../../utils/request'
import { showConfirmDialog, showToast, showLoadingToast, showSuccessToast } from 'vant'

const activeTab = ref("-1")
const allOrders = ref<any[]>([])
const refreshing = ref(false)

// 获取数据
const fetchOrders = async () => {
  try {
    const res: any = await request.get('/api/patient/order/my-orders')
    allOrders.value = res || []
  } finally {
    refreshing.value = false
  }
}

// 过滤列表
const filteredOrders = computed(() => {
  if (activeTab.value === "-1") return allOrders.value
  return allOrders.value.filter(o => o.orderStatus === parseInt(activeTab.value))
})

const onRefresh = () => {
  fetchOrders()
}

const onTabChange = () => {
  // 切换Tab时的逻辑
}

// 状态样式
const getStatusTag = (status: number) => {
  const map: any = { 0: 'primary', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] || 'default'
}

const getStatusText = (status: number) => {
  const map: any = { 0: '待支付', 1: '待就诊', 2: '已完成', 3: '已取消' }
  return map[status] || '未知'
}

const formatTime = (timeStr: string) => {
  return timeStr ? timeStr.replace('T', ' ').substring(0, 16) : ''
}

// 取消预约
const handleCancel = (orderNo: string) => {
  showConfirmDialog({
    title: '提示',
    message: '确定要取消本次挂号预约吗？号源将重新释放。',
  }).then(async () => {
    await request.post(`/api/patient/order/cancel/${orderNo}`)
    showToast('已取消预约')
    fetchOrders()
  })
}

// 2. 将原本只弹一个 Toast 的 handlePay 替换为以下沉浸式逻辑
const handlePay = async (orderNo: string) => {
  // 开启防点透的全局 Loading，模拟微信支付网关唤起
  const toast = showLoadingToast({
    message: '唤起微信支付中...',
    forbidClick: true,
    duration: 0 // 持续展示，直到手动关闭
  })

  try {
    // 强制挂起 1.5 秒，模拟用户在微信中输入密码的停顿感
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    toast.message = '支付处理中...'
    
    // 发起模拟支付请求，通知后端该单已付款
    await request.post(`/api/patient/order/mock-pay/${orderNo}`)
    
    showSuccessToast('微信支付成功！')
    
    // 刷新订单列表，此时订单状态会从 "待支付" 变成 "待就诊"
    fetchOrders() 
  } catch (e: any) {
    // 错误统一交由 request.ts 的拦截器处理
  } finally {
    toast.close()
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped lang="scss">
.order-list-container {
  background: #f7f8fa;
  min-height: calc(100vh - 100px);

  .list-content {
    padding: 12px;
  }

  .order-card {
    background: white;
    border-radius: 12px;
    margin-bottom: 12px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.04);

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;
      .dept-name { font-size: 16px; font-weight: bold; color: #323233; }
    }

    .card-body {
      p { margin: 4px 0; color: #646566; font-size: 14px; }
      .price { color: #ee0a24; font-weight: bold; }
      .time { font-size: 12px; color: #969799; }
      .order-no { font-size: 11px; color: #c8c9cc; margin-top: 8px; }
    }

    .card-footer {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      padding-top: 10px;
    }
  }
}
</style>