import { createRouter, createWebHistory } from 'vue-router'

const RestaurantList = () => import('../pages/RestaurantList.vue')
const RestaurantDetail = () => import('../pages/RestaurantDetail.vue')
const OrderDetail = () => import('../pages/OrderDetail.vue')
import { getSession } from '../api'
import { useAuthStore } from '../stores/auth'
const OrderHistory = () => import('../pages/OrderHistory.vue')

const router = createRouter({
  history: createWebHistory('/'),
  routes: [
    { path: '/', component: RestaurantList },
    { path: '/restaurants/:id', component: RestaurantDetail },
    { path: '/orders/:id', component: OrderDetail },
    { path: '/orders/history', component: OrderHistory }
  ]
})

const BACKEND_BASE = import.meta.env.VITE_BACKEND_BASE

router.beforeEach(async () => {
  try {
    const s = await getSession()
    const auth = useAuthStore()
    auth.user = s
    if (!s || s.role !== 'customer') {
      const ret = encodeURIComponent(window.location.href)
      window.location.href = `${BACKEND_BASE}/login.jsp?redirect=${ret}`
      return false
    }
  } catch (e) {
    const ret = encodeURIComponent(window.location.href)
    window.location.href = `${BACKEND_BASE}/login.jsp?redirect=${ret}`
    return false
  }
})

export default router