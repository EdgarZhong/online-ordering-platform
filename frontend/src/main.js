import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import { useCartStore } from './stores/cart'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)
app.mount('#app')

// 初始化购物车持久化数据
try {
  const cart = useCartStore()
  cart.initFromStorage()
} catch (e) {}