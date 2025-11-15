import axios from 'axios'

const instance = axios.create({
  baseURL: '/online_ordering_backend_war_exploded/api',
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' }
})

instance.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response && err.response.status === 401) {
      const ret = encodeURIComponent(window.location.href)
      window.location.href = `http://localhost:8080/online_ordering_backend_war_exploded/login.jsp?redirect=${ret}`
      return Promise.reject(err)
    }
    return Promise.reject(err)
  }
)

export const getRestaurants = () => instance.get('/restaurants').then(r => r.data)
export const getRestaurant = (id) => instance.get(`/restaurants/${id}?t=${Date.now()}`).then(r => r.data)
export const getMenus = async (restaurantId) => {
  const data = await instance.get(`/restaurants/${restaurantId}/menus?t=${Date.now()}`).then(r => r.data)
  return (data || []).map(m => ({
    ...m,
    isPackage: m.isPackage !== undefined ? m.isPackage : (m.is_package !== undefined ? m.is_package : false)
  }))
}
export const getMenuItems = async (menuId) => {
  const data = await instance.get(`/menus/${menuId}/items?t=${Date.now()}`).then(r => r.data)
  return (data || []).map(it => ({
    ...it,
    sortOrder: it.sortOrder !== undefined ? it.sortOrder : (it.sort_order !== undefined ? it.sort_order : 0),
    defaultQuantity: it.defaultQuantity !== undefined ? it.defaultQuantity : (it.quantity !== undefined ? it.quantity : 1)
  }))
}
export const createOrder = (payload) => instance.post('/orders', payload).then(r => r.data)
export const getOrder = (orderId) => instance.get(`/orders/${orderId}`).then(r => r.data)
export const getSession = () => instance.get('/session').then(r => r.data)
export const getOrders = () => instance.get('/orders').then(r => r.data)

export default instance