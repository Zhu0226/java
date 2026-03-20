import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  // === C 端用户界面 ===
  // 在这里加上了 requireAuth: true，强制首页需要登录
  { path: '/', name: 'Home', component: () => import('../views/GoodsList.vue'), meta: { title: '双喜秒杀', requireAuth: true } },
  // 详情页也加上需要登录校验
  { path: '/goods/:id', name: 'GoodsDetail', component: () => import('../views/GoodsDetail.vue'), meta: { requireAuth: true } },
  { path: '/cart', name: 'Cart', component: () => import('../views/Cart.vue'), meta: { requireAuth: true, title: '购物车' } },
  { path: '/user', name: 'UserCenter', component: () => import('../views/UserCenter.vue'), meta: { requireAuth: true, title: '个人中心' } },
  { path: '/enterprise', name: 'Enterprise', component: () => import('../views/Enterprise.vue'), meta: { requireAuth: true, title: '企业采购专区' } },
  { path: '/orders', name: 'MyOrders', component: () => import('../views/MyOrders.vue'), meta: { requireAuth: true } },
  // 登录页本身不需要登录校验
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },

  // === B 端管理员/客服独立界面 ===
  {
    path: '/admin',
    name: 'AdminContainer',
    component: () => import('../views/admin/AdminLayout.vue'), // 后台布局容器
    redirect: '/admin/dashboard',
    meta: { requireAuth: true, isAdmin: true },
    children: [
      { 
        path: 'dashboard', 
        name: 'AdminDashboard', 
        component: () => import('../views/admin/Dashboard.vue'), 
        meta: { title: '管理概览' } 
      },
      { 
        path: 'goods', 
        name: 'AdminGoods', 
        component: () => import('../views/AdminGoods.vue'), 
        meta: { title: '商品中心' } 
      },
      { 
        path: 'orders', 
        name: 'AdminOrders', 
        component: () => import('../views/AdminOrders.vue'), 
        meta: { title: '订单调度' } 
      },
      { 
        path: 'customers', 
        name: 'AdminCustomers', 
        component: () => import('../views/AdminCustomers.vue'), 
        meta: { title: '客服工作台' } 
      },
      { 
        path: 'metrics', 
        name: 'AdminMetrics', 
        component: () => import('../views/Metrics.vue'), 
        meta: { title: '实时监控' } 
      }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const perms = JSON.parse(localStorage.getItem('perms') || '[]')
  
  // 1. 基础登录校验：如果目标页面需要登录，且本地没有token，踢回登录页
  if (to.meta.requireAuth && !token) {
    return next({ name: 'Login', query: { redirect: to.fullPath } })
  }

  // 2. 管理员界面物理隔离校验
  if (to.path.startsWith('/admin')) {
    const isAdmin = perms.includes('admin:view') || perms.includes('admin:goods:list')
    const isKefu = perms.includes('kefu:view')
    
    // 如果既不是管理员也不是客服，踢回首页
    if (!isAdmin && !isKefu) {
      alert('非法访问：您不是系统内部人员！')
      return next('/')
    }

    // 3. 细粒度权限隔离：拦截客服越权访问管理员专属页面
    const adminOnlyPaths = ['/admin/goods', '/admin/metrics', '/admin/dashboard']
    if (adminOnlyPaths.includes(to.path) && !isAdmin) {
      alert('越权操作：您的客服账号无权访问核心经营模块！')
      // 强制重定向回客服工作台
      return next('/admin/customers')
    }
  }

  next()
})

export default router