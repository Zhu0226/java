<template>
  <div id="app">
    <template v-if="route.name !== 'Login'">
      
      <div class="shortcut">
        <div class="container shortcut-inner">
          
          <div class="shortcut-left">
            <div class="location-dropdown">
              <div class="current-location">
                <span class="icon">📍</span> 中国大陆版 <span class="arrow">∨</span>
              </div>
              <div class="location-list">
                <div class="list-item active"><span>中国大陆版</span></div>
                <div class="list-item"><span>中国港澳版</span></div>
                <div class="list-item"><span>中国台湾版</span></div>
              </div>
            </div>

            <ul class="nav-links">
              <li v-if="username">
                你好，<router-link to="/" class="highlight">{{ username }}</router-link>
                <button class="text-btn" @click="logoutUser">[退出]</button>
              </li>
              <li v-else>
                你好，请<router-link to="/login">登录</router-link>
              </li>
              
              <li class="spacer" v-if="perms.includes('mall:enterprise')">|</li>
              <li v-if="perms.includes('mall:enterprise')">
                <router-link to="/enterprise" class="highlight">🏢 企业大宗采购专区</router-link>
              </li>

              <li class="spacer" v-if="isInternalStaff">|</li>
              <li v-if="isInternalStaff">
                <router-link to="/admin/goods" class="admin-link">💻 进入后台管理系统</router-link>
              </li>
            </ul>
          </div>

          <div class="shortcut-right">
            <ul class="nav-links right-links">
              <template v-if="!isInternalStaff">
                <li>
                  <a href="#" @click.prevent="$router.push('/cart')" class="cart-top-link">
                    <span class="cart-icon-small">🛒</span>购物车
                  </a>
                </li>
                
                <li class="my-jd-dropdown">
                  <div class="my-jd-title">我的双喜 <span class="arrow">∨</span></div>
                  <div class="my-jd-list">
                    <div class="my-jd-grid">
                      <router-link to="/user?tab=orders">我的订单</router-link>
                      <router-link to="/user?tab=drops">降价商品</router-link>
                      <router-link to="/user?tab=favorites">我的关注</router-link>
                    </div>
                    <div class="my-jd-divider"></div>
                    <div class="my-jd-grid">
                      <router-link to="/user?tab=coins">我的喜币</router-link>
                      <router-link to="/user?tab=coupons">我的优惠券</router-link>
                      <router-link to="/user?tab=wallet">我的钱包</router-link>
                    </div>
                  </div>
                </li>
              </template>

              <template v-else>
                <li><router-link to="/admin/orders" class="admin-link">📋 订单管理台</router-link></li>
                <li><router-link to="/admin/customers" class="admin-link">🎧 客服工作台</router-link></li>
              </template>
              
            </ul>
          </div>

        </div>
      </div>

      <header class="header">
        <div class="container header-inner">
          
          <div class="logo-area" @click="$router.push('/')" title="返回首页">
            <img 
              src="/logo.png" 
              alt="双喜" 
              class="logo-img" 
              onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';" 
            />
            <div class="logo-fallback" style="display: none;">
              <span class="icon">⚡</span> 双喜
            </div>
          </div>

          <div class="search-area">
            <div class="search-box">
              <input 
                type="text" 
                v-model="searchKeyword" 
                placeholder="搜索秒杀商品、手机、家电..." 
                @keyup.enter="handleSearch"
              />
              <button class="search-btn" @click="handleSearch">搜索</button>
            </div>
            
            <div class="hot-words" v-if="!isInternalStaff">
              <a href="#" class="highlight" @click.prevent="handleHotWord('每满300减40')">每满300减40</a>
              <a href="#" @click.prevent="handleHotWord('爆款手机')">爆款手机</a>
              <a href="#" @click.prevent="handleHotWord('家电秒杀')">家电秒杀</a>
              <a href="#" @click.prevent="handleHotWord('低至5折')">低至5折</a>
            </div>
          </div>

          <div class="header-right-placeholder"></div>
          
        </div>
      </header>
    </template>

    <main :class="{ 'main-content': route.name !== 'Login' }">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const userId = ref(localStorage.getItem('userId') || '')
const username = ref(localStorage.getItem('username') || '')
const perms = ref(JSON.parse(localStorage.getItem('perms') || '[]'))

// 搜索框双向绑定变量
const searchKeyword = ref('')

const route = useRoute()
const router = useRouter()

const isInternalStaff = computed(() => {
  return perms.value.includes('admin:view') || 
         perms.value.includes('admin:goods:list') || 
         perms.value.includes('kefu:view')
})

watch(() => route.path, () => {
  userId.value = localStorage.getItem('userId') || ''
  username.value = localStorage.getItem('username') || ''
  perms.value = JSON.parse(localStorage.getItem('perms') || '[]')
})

function logoutUser() {
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('username')
  localStorage.removeItem('perms')
  userId.value = ''
  username.value = ''
  perms.value = []
  router.push('/login')
}

// 触发搜索动作
function handleSearch() {
  if (!searchKeyword.value.trim()) {
    alert('请输入您想要搜索的商品名称！')
    return
  }
  // 模拟搜索结果提示
  alert(`正在为您检索与【${searchKeyword.value}】相关的商品...\n（注：全局搜索功能正在开发中）`)
}

// 点击热词动作
function handleHotWord(word) {
  // 1. 将词语填入搜索框
  searchKeyword.value = word
  // 2. 自动触发搜索
  handleSearch()
}
</script>

<style scoped>
/* 上部快捷导航 */
.shortcut {
  background: #e3e4e5;
  height: 30px;
  line-height: 30px;
  color: #999;
  font-size: 12px;
  border-bottom: 1px solid #ddd;
}
.shortcut-inner {
  display: flex;
  justify-content: space-between; 
  align-items: center;
}

.shortcut-left, .shortcut-right {
  display: flex;
  align-items: center;
}

/* 中国大陆版 下拉菜单 */
.location-dropdown {
  position: relative;
  height: 30px;
  padding: 0 10px;
  cursor: pointer;
  border: 1px solid transparent; 
  border-bottom: none;
  z-index: 999; 
  margin-right: 15px; 
}
.location-dropdown:hover {
  background: #fff;
  border-color: #eee;
}
.location-dropdown:hover .location-list {
  display: block; 
}
.arrow {
  font-size: 10px;
  margin-left: 4px;
  transition: transform 0.2s;
  display: inline-block;
}
.location-dropdown:hover .arrow, .my-jd-dropdown:hover .arrow {
  transform: rotate(180deg);
}

.location-list {
  display: none; 
  position: absolute;
  top: 29px; 
  left: -1px;
  width: 120px; 
  background: #fff;
  border: 1px solid #eee;
  border-top: none;
  padding: 10px 0; 
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.list-item {
  display: block;
  padding: 8px 15px; 
  text-align: left;
  color: #999;
  line-height: 1.2;
}
.list-item:hover {
  background: #f4f4f4;
  color: #e1251b;
}
.list-item.active {
  background: transparent; 
}
.list-item.active:hover {
  background: #f4f4f4;
}
.list-item.active span {
  background: #e1251b;
  color: #fff;
  padding: 3px 6px;
  border-radius: 4px;
}

/* 我的双喜 下拉菜单 */
.my-jd-dropdown {
  position: relative;
  z-index: 999;
}
.my-jd-title {
  height: 30px;
  padding: 0 10px;
  cursor: pointer;
  border: 1px solid transparent;
  border-bottom: none;
}
.my-jd-dropdown:hover .my-jd-title {
  background: #fff;
  border-color: #eee;
}
.my-jd-list {
  display: none;
  position: absolute;
  top: 29px;
  left: 50%; 
  transform: translateX(-50%); 
  width: 260px; 
  background: #fff;
  border: 1px solid #eee;
  border-top: none;
  padding: 15px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.my-jd-dropdown:hover .my-jd-list {
  display: block;
}
.my-jd-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px 8px; 
}
.my-jd-grid a {
  color: #999;
  text-decoration: none;
  text-align: left;
}
.my-jd-grid a:hover {
  color: #e1251b;
}
.my-jd-divider {
  height: 1px;
  background: #eee;
  margin: 12px 0;
}

/* 导航链接公用样式 */
.nav-links {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}
.right-links {
  gap: 18px; 
}
.nav-links a { color: #999; text-decoration: none; }
.nav-links a:hover { color: #e1251b; }
.spacer { color: #ccc; }
.highlight { color: #e1251b !important; text-decoration: none; }
.highlight:hover { text-decoration: underline; }

.cart-top-link { display: flex; align-items: center; }
.cart-icon-small { color: #e1251b; font-size: 14px; margin-right: 4px; }

.text-btn {
  background: none; border: none; color: #999; cursor: pointer; padding: 0; font-size: 12px; margin-left: 4px;
}
.text-btn:hover { color: #e1251b; }
.admin-link { color: #333 !important; }

/* 中部 Header 布局 */
.header {
  background: #fff;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
  position: relative;
  z-index: 10; 
}
.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 80px;
}

/* 1. Logo */
.logo-area {
  width: 190px;
  height: 100%;
  cursor: pointer;
  display: flex;
  align-items: center;
}
.logo-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}
.logo-fallback {
  font-size: 36px;
  font-weight: 900;
  color: #e1251b;
  letter-spacing: 2px;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 2. 搜索区 */
.search-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.search-box {
  display: flex;
  border: 2px solid #e1251b;
  width: 550px; 
  height: 36px;
  background: #fff;
}
.search-box input {
  flex: 1;
  border: none;
  padding: 0 16px;
  outline: none;
  font-size: 14px;
}
.search-btn {
  width: 80px;
  background: #e1251b;
  color: #fff;
  border: none;
  font-weight: bold;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.2s;
}
.search-btn:hover {
  background: #c81623;
}
.hot-words {
  width: 550px;
  margin-top: 6px;
  font-size: 12px;
  display: flex;
  gap: 12px;
}
.hot-words a {
  color: #999;
  text-decoration: none;
}
.hot-words a:hover {
  color: #e1251b;
}

/* 右侧透明占位，替代购物车，保证中心搜索框对齐 */
.header-right-placeholder {
  width: 190px; 
}

.main-content {
  padding-top: 20px;
}
</style>