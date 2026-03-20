<template>
  <div class="enterprise-page container">
    
    <div class="vip-header jd-card">
      <div class="vip-info">
        <span class="boss-icon">🏢</span>
        <div>
          <h2>尊贵的企业 VIP：{{ username }}，欢迎来到双喜大宗采购中心</h2>
          <p>您享有全场底价批发特权，采购量越大，折扣越低！</p>
        </div>
      </div>
      <div class="vip-stats">
        <div class="stat"><span>本月专属可用额度</span><strong>¥ 5,000,000</strong></div>
        <div class="stat"><span>企业白条账期</span><strong>90 天</strong></div>
      </div>
    </div>

    <div class="wholesale-list">
      <h3>📦 内部直供货源报价单</h3>
      <table class="ws-table jd-card">
        <thead>
          <tr>
            <th>商品名称</th>
            <th>单件指导价</th>
            <th>企业批发底价 (100件起)</th>
            <th>仓储情况</th>
            <th class="text-center">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>
              <div class="goods-name">Apple iPhone 15 Pro Max 256GB</div>
              <span class="spec-tag">企业定制版 / 员工年会福利优选</span>
            </td>
            <td class="old-price">¥5999.00</td>
            <td class="vip-price">¥5150.00</td>
            <td><span class="stock-ok">充裕 (1000+台)</span></td>
            <td class="text-center"><button class="jd-btn" @click="handleBulkOrder">批量下单</button></td>
          </tr>
          <tr>
            <td>
              <div class="goods-name">索尼（SONY）PlayStation 5 光驱版</div>
              <span class="spec-tag">网吧 / 电竞酒店专供版</span>
            </td>
            <td class="old-price">¥2999.00</td>
            <td class="vip-price">¥2380.00</td>
            <td><span class="stock-ok">充裕 (500+台)</span></td>
            <td class="text-center"><button class="jd-btn" @click="handleBulkOrder">批量下单</button></td>
          </tr>
          <tr>
            <td>
              <div class="goods-name">双喜定制 纯棉高支加厚四件套</div>
              <span class="spec-tag">高端连锁酒店专供 120S</span>
            </td>
            <td class="old-price">¥199.50</td>
            <td class="vip-price">¥85.00</td>
            <td><span class="stock-ok">极好 (5000+套)</span></td>
            <td class="text-center"><button class="jd-btn" @click="handleBulkOrder">批量下单</button></td>
          </tr>
        </tbody>
      </table>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'

// 获取当前登录的老板名称
const username = ref(localStorage.getItem('username') || '企业老板')

function handleBulkOrder() {
  alert('您正在进行大宗采购操作，专属客户经理将在 3 分钟内致电与您确认订单细节！')
}
</script>

<style scoped>
.enterprise-page {
  padding: 30px 0 80px 0;
}

/* 黑金风格 VIP 头部 */
.vip-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40px;
  margin-bottom: 30px;
  background: linear-gradient(135deg, #1e1e1e 0%, #3a3a3a 100%);
  color: #f2dbb5;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.15);
}
.vip-info {
  display: flex;
  align-items: center;
  gap: 25px;
}
.boss-icon {
  font-size: 70px;
}
.vip-info h2 {
  margin: 0 0 10px 0;
  color: #fbd69b;
  font-size: 24px;
  letter-spacing: 1px;
}
.vip-info p {
  margin: 0;
  color: #b0a89a;
  font-size: 14px;
}
.vip-stats {
  display: flex;
  gap: 50px;
}
.stat {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}
.stat span {
  font-size: 14px;
  color: #b0a89a;
  margin-bottom: 8px;
}
.stat strong {
  font-size: 32px;
  color: #fbd69b;
  font-family: Arial, Helvetica, sans-serif;
}

/* 报价单列表 */
.wholesale-list h3 {
  font-size: 20px;
  color: #333;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.ws-table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  text-align: left;
}
.ws-table th {
  background: #f9f9f9;
  padding: 16px 20px;
  color: #666;
  font-weight: normal;
  border-bottom: 2px solid #eee;
}
.ws-table td {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  vertical-align: middle;
}
.goods-name {
  font-size: 16px;
  color: #333;
  font-weight: bold;
  margin-bottom: 6px;
}
.spec-tag {
  background: #fdf0f0;
  color: var(--jd-red);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}
.old-price {
  text-decoration: line-through;
  color: #999;
}
.vip-price {
  color: var(--jd-red);
  font-size: 22px;
  font-weight: bold;
}
.stock-ok {
  color: #52c41a;
  font-weight: bold;
}
.text-center {
  text-align: center;
}
</style>