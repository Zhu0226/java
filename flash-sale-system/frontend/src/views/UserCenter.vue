<template>
  <div class="user-center container">
    <aside class="sidebar jd-card">
      <div class="user-profile">
        <div class="avatar">{{ username.charAt(0).toUpperCase() }}</div>
        <div class="name">{{ username }}</div>
        <div class="level-tag">💎 银牌会员</div>
      </div>

      <div class="menu-group">
        <div class="menu-title">订单中心</div>
        <div class="menu-item" :class="{ active: activeTab === 'orders' }" @click="switchTab('orders')">我的订单</div>
        <div class="menu-item" :class="{ active: activeTab === 'drops' }" @click="switchTab('drops')">降价商品</div>
      </div>

      <div class="menu-group">
        <div class="menu-title">资产中心</div>
        <div class="menu-item" :class="{ active: activeTab === 'coins' }" @click="switchTab('coins')">我的喜币</div>
        <div class="menu-item" :class="{ active: activeTab === 'coupons' }" @click="switchTab('coupons')">我的优惠券</div>
      </div>

      <div class="menu-group">
        <div class="menu-title">关注中心</div>
        <div class="menu-item" :class="{ active: activeTab === 'favorites' }" @click="switchTab('favorites')">我的关注</div>
      </div>
    </aside>

    <main class="content jd-card">
      
      <div v-if="activeTab === 'orders'" class="tab-panel">
        <h3 class="panel-title">我的订单</h3>
        <div v-if="loadingOrders" class="loading-tip">正在获取订单数据...</div>
        <div v-else-if="orders.length === 0" class="empty-tip">您还没有下过单，快去秒杀抢购吧！</div>
        
        <div v-else class="order-list">
          <div class="order-item" v-for="o in orders" :key="o.id">
            <div class="order-header">
              <span class="order-time">{{ new Date(o.createTime).toLocaleString() }}</span>
              <span class="order-id">订单号：#{{ o.id }}</span>
            </div>
            <div class="order-body">
              <div class="goods-info">
                <div class="goods-img">📦</div>
                <div class="goods-detail">
                  <p class="goods-name">秒杀商品 (ID: {{ o.goodsId }})</p>
                  <p class="goods-qty">数量：1</p>
                </div>
              </div>
              <div class="order-status" :class="statusClass(o.status)">
                {{ statusText(o.status) }}
              </div>
              <div class="order-actions">
                <button v-if="o.status === 0" class="jd-btn pay-btn" @click="pay(o.id)">立即支付</button>
                <button v-if="o.status === 0" class="cancel-btn" @click="cancel(o.id)">取消订单</button>
                <button v-if="o.status === 1 || o.status === 2" class="jd-btn btn-default">查看详情</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="activeTab === 'coins'" class="tab-panel">
        <h3 class="panel-title">我的喜币</h3>
        <div class="coin-dashboard">
          <div class="coin-balance">
            <span class="coin-icon">💰</span>
            <span class="coin-num">1,250</span>
            <span class="coin-unit">枚</span>
          </div>
          <button class="jd-btn">兑换超值权益</button>
        </div>
        <p class="mock-desc">今日签到可领取 50 喜币，连续签到奖励翻倍！</p>
      </div>

      <div v-else-if="activeTab === 'coupons'" class="tab-panel">
        <h3 class="panel-title">我的优惠券</h3>
        <div class="coupon-list">
          <div class="coupon-card usable">
            <div class="coupon-left">
              <span class="rmb">¥</span><span class="amount">100</span>
              <div class="condition">满 999 可用</div>
            </div>
            <div class="coupon-right">
              <div class="c-title">手机数码专享券</div>
              <div class="c-time">有效期至 2026.12.31</div>
              <button class="use-btn">立即使用</button>
            </div>
          </div>
          <div class="coupon-card used">
            <div class="coupon-left">
              <span class="rmb">¥</span><span class="amount">20</span>
              <div class="condition">无门槛</div>
            </div>
            <div class="coupon-right">
              <div class="c-title">全品类通用券</div>
              <div class="c-time">已使用</div>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="activeTab === 'drops'" class="tab-panel">
        <h3 class="panel-title">降价商品</h3>
        <div class="drop-goods">
          <div class="goods-img">🎮</div>
          <div class="drop-info">
            <div class="d-title">您关注的「索尼 PS5 主机」降价了！</div>
            <div class="d-prices">
              <span class="current-price">¥2999</span>
              <span class="old-price">原价 ¥3599</span>
              <span class="drop-tag">直降 ¥600</span>
            </div>
          </div>
          <button class="jd-btn">立即抢购</button>
        </div>
      </div>

      <div v-else-if="activeTab === 'favorites'" class="tab-panel">
        <h3 class="panel-title">我的关注</h3>
        <div class="empty-tip">您还没有关注任何店铺或商品哦。</div>
      </div>

    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
// 引入原有的真实订单接口
import { listOrders, payOrder, cancelOrder } from '../api/seckill.js'

const route = useRoute()
const router = useRouter()
const username = ref(localStorage.getItem('username') || '买家')
const userId = ref(Number(localStorage.getItem('userId')) || 1001)

// 当前选中的 Tab，默认为 orders
const activeTab = ref(route.query.tab || 'orders')

// 监听地址栏 ?tab=xxx 的变化，从而切换页面内容
watch(() => route.query.tab, (newTab) => {
  if (newTab) {
    activeTab.value = newTab
    if (newTab === 'orders') fetchOrders()
  }
})

function switchTab(tab) {
  // 通过 router 改变 url，这会触发上面的 watch
  router.push({ query: { tab } })
}

// === 真实订单处理逻辑 ===
const orders = ref([])
const loadingOrders = ref(false)

function statusText(s) {
  return { 0: '等待付款', 1: '已完成', 2: '交易关闭' }[s] || '未知状态'
}
function statusClass(s) {
  return { 0: 'text-danger', 1: 'text-success', 2: 'text-muted' }[s] || ''
}

async function fetchOrders() {
  loadingOrders.value = true
  try {
    const res = await listOrders(userId.value)
    if (res.code === 0) {
      orders.value = res.data || []
    }
  } finally {
    loadingOrders.value = false
  }
}

async function pay(id) {
  try {
    const res = await payOrder(id, userId.value)
    if (res.code === 0) {
      alert('模拟支付成功！')
      fetchOrders()
    } else {
      alert(res.msg || '支付失败')
    }
  } catch (e) {
    alert(e.message)
  }
}

async function cancel(id) {
  if (!confirm('确定要取消这个订单吗？')) return
  try {
    const res = await cancelOrder(id, userId.value)
    if (res.code === 0) {
      alert('订单已成功取消，库存已回退！')
      fetchOrders()
    } else {
      alert(res.msg || '取消失败')
    }
  } catch (e) {
    alert(e.message)
  }
}

onMounted(() => {
  if (activeTab.value === 'orders') {
    fetchOrders()
  }
})
</script>

<style scoped>
.user-center {
  display: flex;
  gap: 20px;
  padding: 30px 0 60px 0;
  align-items: flex-start;
}

/* 左侧边栏 */
.sidebar {
  width: 220px;
  padding: 30px 0;
  min-height: 500px;
  flex-shrink: 0;
}
.user-profile {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px dashed #eee;
}
.avatar {
  width: 60px;
  height: 60px;
  background: var(--jd-red);
  color: #fff;
  border-radius: 50%;
  margin: 0 auto 10px auto;
  font-size: 28px;
  line-height: 60px;
  font-weight: bold;
}
.name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 6px;
}
.level-tag {
  font-size: 12px;
  color: #d4a350;
  background: #fdfaf2;
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  border: 1px solid #f3e5c9;
}

.menu-group {
  margin-bottom: 20px;
}
.menu-title {
  font-size: 16px;
  font-weight: bold;
  padding-left: 30px;
  margin-bottom: 10px;
  color: #333;
}
.menu-item {
  padding: 10px 30px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}
.menu-item:hover {
  color: var(--jd-red);
}
.menu-item.active {
  color: var(--jd-red);
  font-weight: bold;
  background: #fdf0f0;
  border-right: 3px solid var(--jd-red);
}

/* 右侧内容区 */
.content {
  flex: 1;
  min-height: 600px;
  padding: 30px;
}
.panel-title {
  margin: 0 0 20px 0;
  font-size: 20px;
  color: #333;
  padding-bottom: 15px;
  border-bottom: 2px solid #f4f4f4;
}

.empty-tip {
  padding: 60px 0;
  text-align: center;
  color: #999;
  font-size: 16px;
}

/* 订单列表样式 */
.order-item {
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  margin-bottom: 20px;
  overflow: hidden;
}
.order-header {
  background: #f9f9f9;
  padding: 12px 20px;
  font-size: 13px;
  color: #666;
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid #e5e5e5;
}
.order-body {
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.goods-info {
  display: flex;
  align-items: center;
  gap: 15px;
  width: 40%;
}
.goods-img {
  width: 60px;
  height: 60px;
  background: #f4f4f4;
  font-size: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 4px;
}
.goods-name { margin: 0 0 5px 0; color: #333; }
.goods-qty { margin: 0; color: #999; font-size: 13px; }
.order-status {
  width: 20%;
  text-align: center;
  font-weight: bold;
}
.order-actions {
  width: 25%;
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-end;
}
.pay-btn { padding: 6px 20px; }
.cancel-btn { background: none; border: none; color: #999; cursor: pointer; }
.cancel-btn:hover { color: var(--jd-red); }

/* 其他 Mock 界面样式 */
.coin-dashboard {
  display: flex;
  align-items: center;
  gap: 30px;
  background: linear-gradient(135deg, #fffcf0 0%, #fff4d9 100%);
  padding: 40px;
  border-radius: 12px;
  border: 1px solid #ffe9b3;
}
.coin-balance { font-size: 48px; color: #d4a350; font-weight: bold; }
.coin-icon { font-size: 40px; margin-right: 10px; }
.coin-unit { font-size: 16px; margin-left: 5px; font-weight: normal; }
.mock-desc { margin-top: 20px; color: #999; font-size: 14px; }

.coupon-list { display: flex; gap: 20px; flex-wrap: wrap; }
.coupon-card {
  width: 320px; height: 100px; display: flex;
  border-radius: 8px; overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.coupon-left {
  width: 120px; background: #fdf0f0; color: var(--jd-red);
  display: flex; flex-direction: column; justify-content: center; align-items: center;
  border-right: 2px dashed #ffcccc;
}
.coupon-left .rmb { font-size: 16px; }
.coupon-left .amount { font-size: 36px; font-weight: bold; }
.coupon-left .condition { font-size: 12px; margin-top: 4px; }
.coupon-right {
  flex: 1; background: #fff; padding: 15px; display: flex; flex-direction: column; justify-content: space-between;
}
.c-title { font-weight: bold; font-size: 15px; }
.c-time { font-size: 12px; color: #999; }
.use-btn { align-self: flex-end; background: var(--jd-red); color: #fff; border: none; padding: 4px 12px; border-radius: 12px; font-size: 12px; cursor: pointer; }

.coupon-card.used { filter: grayscale(1); opacity: 0.6; }
.coupon-card.used .coupon-left { border-right-color: #ccc; }

.drop-goods { display: flex; align-items: center; padding: 20px; border: 1px solid #eee; border-radius: 8px; gap: 20px; }
.drop-info { flex: 1; }
.d-title { font-size: 16px; font-weight: bold; margin-bottom: 10px; }
.d-prices span { margin-right: 15px; }
.current-price { color: var(--jd-red); font-size: 24px; font-weight: bold; }
.old-price { color: #999; text-decoration: line-through; font-size: 14px; }
.drop-tag { background: #e1251b; color: #fff; padding: 2px 6px; border-radius: 4px; font-size: 12px; }

.text-danger { color: var(--jd-red); }
.text-success { color: #52c41a; }
.text-muted { color: #999; }
</style>