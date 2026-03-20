<template>
  <div class="cart-page container">
    <div class="cart-header">
      <h2>全部商品 <span class="cart-count">{{ cartList.length }}</span></h2>
      <div class="header-right">
        <span class="delivery-location">📍 配送至：内华达州拉斯维加斯...</span>
      </div>
    </div>

    <div class="promo-banner" v-if="totalAmount > 0">
      <span class="promo-tag">满减</span>
      <span class="promo-text" v-if="discountAmount > 0">
        已购满 {{ currentRule.threshold }} 元，已减 {{ currentRule.reduce }} 元
      </span>
      <span class="promo-text" v-else>
        购满 {{ nextRule.threshold }} 元，即可减 {{ nextRule.reduce }} 元，<a href="#" class="go-湊单">去凑单 ></a>
      </span>
    </div>

    <div class="cart-list">
      <div class="cart-store">
        <div class="store-header">
          <input type="checkbox" v-model="isAllSelected" @change="toggleAll" class="custom-checkbox" />
          <span class="store-icon">🏪</span>
          <span class="store-name">双喜自营旗舰店</span>
        </div>

        <div class="cart-item" v-for="(item, index) in cartList" :key="item.id">
          <div class="item-checkbox">
            <input type="checkbox" v-model="item.checked" class="custom-checkbox" />
          </div>
          <div class="item-image">
            <div class="img-placeholder">{{ item.name.substring(0, 2) }}</div>
          </div>
          <div class="item-info">
            <div class="item-title">{{ item.name }}</div>
            <div class="item-tags">
              <span class="tag" v-for="tag in item.tags" :key="tag">{{ tag }}</span>
            </div>
            <div class="item-bottom">
              <div class="item-price">
                <span class="currency">¥</span>
                <span class="price-int">{{ Math.floor(item.price) }}</span>
                <span class="price-decimal">.{{ (item.price % 1).toFixed(2).substring(2) }}</span>
              </div>
              <div class="item-stepper">
                <button class="step-btn" @click="updateCount(index, -1)" :disabled="item.count <= 1">-</button>
                <input type="number" class="step-input" v-model.number="item.count" @blur="checkCount(index)" />
                <button class="step-btn" @click="updateCount(index, 1)" :disabled="item.count >= item.stock">+</button>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="cartList.length === 0" class="empty-cart">
          <div class="empty-icon">🛒</div>
          <p>购物车空空如也，快去挑点宝贝吧！</p>
          <button class="go-shop-btn" @click="router.push('/')">去逛逛</button>
        </div>
      </div>
    </div>

    <div class="cart-footer" v-if="cartList.length > 0">
      <div class="footer-left">
        <label class="select-all-label">
          <input type="checkbox" v-model="isAllSelected" @change="toggleAll" class="custom-checkbox" />
          <span>全选</span>
        </label>
        <div class="delete-selected" v-if="selectedCount > 0" @click="removeSelected">删除选中</div>
      </div>
      
      <div class="footer-right">
        <div class="summary-info">
          <div class="total-line">
            总价：<span class="total-price">¥{{ actualPay.toFixed(2) }}</span>
          </div>
          <div class="discount-line" v-if="discountAmount > 0">
            已节省：¥{{ discountAmount.toFixed(2) }}
          </div>
        </div>
        <button class="checkout-btn" :class="{ disabled: selectedCount === 0 }" @click="handleCheckout">
          去结算 <span v-if="selectedCount > 0">({{ selectedCount }}件)</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 1. 定义默认的购物车数据
const defaultCartList = [
  {
    id: 1,
    name: 'Apple iPhone 15 Pro Max 256GB 原色钛金属',
    price: 5999.00,
    count: 1,
    stock: 5,
    checked: true,
    tags: ['七天无理由退货', '秒杀降价']
  },
  {
    id: 2,
    name: '索尼（SONY）PlayStation 5 光驱版主机',
    price: 2999.00,
    count: 1,
    stock: 10,
    checked: false,
    tags: ['假一赔十', '白条免息']
  },
  {
    id: 3,
    name: '双喜定制 纯棉高支加厚四件套',
    price: 199.50,
    count: 2,
    stock: 50,
    checked: true,
    tags: ['自营', '满99减20']
  }
]

// 2. 尝试从本地存储读取购物车数据
const savedCart = localStorage.getItem('my_shopping_cart')
const cartList = ref(savedCart ? JSON.parse(savedCart) : defaultCartList)

// 3. 深度监听：只要数据变动，自动存入本地
watch(cartList, (newVal) => {
  localStorage.setItem('my_shopping_cart', JSON.stringify(newVal))
}, { deep: true })

// 满减阶梯规则
const discountRules = [
  { threshold: 5000, reduce: 400 },
  { threshold: 1000, reduce: 100 },
  { threshold: 299, reduce: 50 },
  { threshold: 99, reduce: 20 }
]

// 选中商品数量
const selectedCount = computed(() => {
  return cartList.value.filter(item => item.checked).reduce((sum, item) => sum + item.count, 0)
})

// 选中商品总原价
const totalAmount = computed(() => {
  return cartList.value
    .filter(item => item.checked)
    .reduce((sum, item) => sum + item.price * item.count, 0)
})

// 计算适用的最高满减规则
const currentRule = computed(() => {
  return discountRules.find(rule => totalAmount.value >= rule.threshold) || { threshold: 0, reduce: 0 }
})

// 计算下一级需要凑单的规则
const nextRule = computed(() => {
  const reversedRules = [...discountRules].reverse()
  return reversedRules.find(rule => totalAmount.value < rule.threshold) || discountRules[0]
})

// 实际优惠金额
const discountAmount = computed(() => currentRule.value.reduce)

// 实际应付金额
const actualPay = computed(() => {
  return Math.max(0, totalAmount.value - discountAmount.value)
})

// 全选/反选逻辑
const isAllSelected = computed({
  get: () => cartList.value.length > 0 && cartList.value.every(item => item.checked),
  set: (val) => {
    cartList.value.forEach(item => { item.checked = val })
  }
})

function toggleAll(e) {
  const isChecked = e.target.checked
  cartList.value.forEach(item => { item.checked = isChecked })
}

// 数量增减
function updateCount(index, delta) {
  const item = cartList.value[index]
  const newCount = item.count + delta
  if (newCount >= 1 && newCount <= item.stock) {
    item.count = newCount
  }
}

// 手动输入数量校验
function checkCount(index) {
  const item = cartList.value[index]
  if (item.count < 1 || isNaN(item.count)) item.count = 1
  if (item.count > item.stock) item.count = item.stock
}

// 删除选中
function removeSelected() {
  if (confirm('确定要删除选中的商品吗？')) {
    cartList.value = cartList.value.filter(item => !item.checked)
  }
}

// 结算跳转
function handleCheckout() {
  if (selectedCount.value === 0) return
  alert(`共需支付 ¥${actualPay.value.toFixed(2)}，即将跳转到支付收银台...`)
}
</script>

<style scoped>
.cart-page {
  padding: 20px 0 100px 0;
  max-width: 1000px;
  margin: 0 auto;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20px;
}
.cart-header h2 {
  font-size: 24px;
  color: #333;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}
.cart-count {
  font-size: 14px;
  background: #e1251b;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: normal;
}
.delivery-location {
  font-size: 14px;
  color: #666;
}

.promo-banner {
  background: #fff3f3;
  border: 1px solid #ffcccc;
  padding: 12px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 12px;
}
.promo-tag {
  background: #e1251b;
  color: #fff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}
.go-凑单 {
  color: #e1251b;
  text-decoration: none;
  font-weight: bold;
}

.cart-store {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  min-height: 300px;
}
.store-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
  font-weight: bold;
  font-size: 16px;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px dashed #f0f0f0;
  gap: 20px;
}
.cart-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.custom-checkbox {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #e1251b;
}

.item-image {
  width: 100px;
  height: 100px;
  background: #f8f8f8;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.img-placeholder {
  font-size: 24px;
  color: #ccc;
  font-weight: bold;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100px;
}
.item-title {
  font-size: 16px;
  color: #333;
  line-height: 1.4;
}
.item-tags {
  display: flex;
  gap: 8px;
}
.item-tags .tag {
  font-size: 12px;
  color: #e1251b;
  background: #fff0f0;
  padding: 2px 6px;
  border-radius: 4px;
}

.item-bottom {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}
.item-price {
  color: #e1251b;
  font-weight: bold;
}
.item-price .currency { font-size: 14px; }
.item-price .price-int { font-size: 20px; }
.item-price .price-decimal { font-size: 14px; }

.item-stepper {
  display: flex;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  overflow: hidden;
}
.step-btn {
  width: 30px;
  height: 26px;
  background: #f5f5f5;
  border: none;
  cursor: pointer;
  font-size: 16px;
}
.step-btn:disabled { color: #ccc; cursor: not-allowed; }
.step-input {
  width: 40px;
  height: 26px;
  text-align: center;
  border: none;
  border-left: 1px solid #e5e5e5;
  border-right: 1px solid #e5e5e5;
  outline: none;
}

.cart-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 70px;
  background: #fff;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  display: flex;
  justify-content: center;
  z-index: 100;
}
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 calc((100vw - 1000px) / 2); /* 保持和主体同宽 */
  min-width: 1000px;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 20px;
}
.select-all-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
}
.delete-selected {
  font-size: 14px;
  color: #666;
  cursor: pointer;
}
.delete-selected:hover { color: #e1251b; }

.footer-right {
  display: flex;
  align-items: center;
  gap: 20px;
}
.summary-info {
  text-align: right;
}
.total-line {
  font-size: 14px;
  color: #333;
}
.total-price {
  font-size: 20px;
  color: #e1251b;
  font-weight: bold;
}
.discount-line {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.checkout-btn {
  background: #e1251b;
  color: #fff;
  border: none;
  height: 50px;
  padding: 0 40px;
  font-size: 18px;
  border-radius: 25px;
  font-weight: bold;
  cursor: pointer;
  transition: opacity 0.2s;
}
.checkout-btn.disabled {
  background: #ccc;
  cursor: not-allowed;
}
.checkout-btn:hover:not(.disabled) {
  opacity: 0.9;
}

/* 空购物车样式 */
.empty-cart {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #999;
}
.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}
.go-shop-btn {
  margin-top: 20px;
  padding: 10px 30px;
  background: #e1251b;
  color: #fff;
  border: none;
  border-radius: 20px;
  font-size: 16px;
  cursor: pointer;
}
.go-shop-btn:hover {
  background: #c81623;
}
</style>