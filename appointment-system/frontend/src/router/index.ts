import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'
import DoctorLayout from '../layout/DoctorLayout.vue' // 引入新建的 Layout 壳子
import AdminLayout from '../layout/AdminLayout.vue' // 在文件顶部引入
import PatientLayout from '../layout/PatientLayout.vue'// 在文件顶部引入

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  // B端：管理员控制台 (暂未做 Layout，直接显示)
  {
    path: '/admin',
    name: 'AdminLayout',
    component: AdminLayout,
    redirect: '/admin/schedule-engine', // 暂时默认重定向到排班引擎
    children: [
      {
        path: 'schedule-engine',
        name: 'ScheduleEngine',
        component: () => import('../views/admin/ScheduleEngine.vue') // 你刚才重命名的页面
      },
     {
        path: 'dashboard',
        name: 'AdminDashboard',
        // 修改这里，指向刚才新建的 Dashboard.vue
        component: () => import('../views/admin/Dashboard.vue') 
      },
     {
        path: 'orders',
        name: 'AdminOrders',
        // 修改这里，不再用 ScheduleEngine 占位了！
        component: () => import('../views/admin/OrderMonitor.vue')
      }
    ]
  },
  // B端：医生专属后台 (使用嵌套路由，套用 DoctorLayout)
  {
    path: '/doctor',
    name: 'DoctorLayout',
    component: DoctorLayout,
    redirect: '/doctor/workbench', // 访问 /doctor 时默认重定向到工作台
    children: [
      {
        path: 'workbench',
        name: 'Workbench',
        component: () => import('../views/doctor/Workbench.vue')
      },
      // 👇 追加下面这段 👇
      {
        path: 'schedule',
        name: 'DoctorSchedule',
        component: () => import('../views/doctor/Schedule.vue')
      },
      {
        path: 'history',
        name: 'DoctorHistory',
        component: () => import('../views/doctor/History.vue')
      }
    ]
  },
  // C端：患者挂号大厅
  {
    path: '/patient',
    component: PatientLayout,
    redirect: '/patient/home',
    children: [
      {
        path: 'home',
        name: 'PatientHome',
        component: () => import('../views/patient/Home.vue') // 现有的挂号页面
      },
      {
        path: 'orders',
        name: 'PatientOrders',
        component: () => import('../views/patient/OrderList.vue') // 占位，下一步开发
      },
      {
          path: 'profile',
          name: 'PatientProfile',
          component: () => import('../views/patient/Profile.vue') // 【修改这里】指向真实的个人中心
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局路由前置守卫：除了登录页，其余全盘拦截校验 Token
router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  if (to.path !== '/login' && !userStore.accessToken) {
    next('/login')
  } else {
    next()
  }
})

export default router