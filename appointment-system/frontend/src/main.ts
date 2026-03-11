import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 👇👇👇 新增：引入 Vant 及其样式 👇👇👇
import Vant from 'vant'
import 'vant/lib/index.css'
// 👆👆👆 ======================== 👆👆👆

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// 👇👇👇 新增：注册 Vant 👇👇👇
app.use(Vant)
// 👆👆👆 ================= 👆👆👆

app.mount('#app')