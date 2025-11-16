<template>
  <div>
    <div v-if="loading">加载中...</div>
    <div v-else-if="error" class="card">{{ error }}</div>
    <div v-else>
      <div class="actions">
        <button class="back-btn" @click="back">返回</button>
        <button v-if="order.status==='PENDING'" class="btn-cancel" :disabled="busyCancel" @click="onCancel">取消订单</button>
        <button class="btn-reorder" :disabled="busyReorder" @click="onReorder">再次购买</button>
      </div>
      <h1 class="title">流水号：{{ order.serialNumber }}</h1>
      <button class="btn-restaurant" @click="gotoRestaurant">{{ order.restaurantName }}</button>
      <p>订单id：{{ order.orderId }}</p>
      <p>状态：{{ order.status }} · 金额：￥{{ Number(order.totalPrice || 0).toFixed(2) }}</p>
      <p>时间：{{ order.createdAt }}</p>
      <h3>明细</h3>
      <div v-if="order.menus && order.menus.length">
        <div v-for="m in order.menus" :key="m.menuId" class="card">
          <div class="menu-header">
            <div class="menu-title text-menu-name">{{ m.menuName || ('菜单 ' + m.menuId) }}<span v-if="m.isPackage" class="badge">套餐</span></div>
            <div v-if="m.isPackage" class="pkg"><span class="pkg-qty">{{ m.menuQuantity || 0 }}</span></div>
          </div>
          <div v-if="m.isPackage" class="menu-unit text-muted">单价：<span class="text-price">￥{{ Number(m.menuUnitPrice || 0).toFixed(2) }}</span></div>
          <div v-for="it in sortItems(m)" :key="`${m.menuId}-${it.dishName}-${it.unitPrice}`" class="item">
            <div class="info">
              <div class="row">
                <div class="name text-dish-name">{{ it.dishName }}</div>
                <div class="price text-price">￥{{ Number(it.unitPrice).toFixed(2) }} × {{ m.isPackage ? ((it.perPackageQuantity || 0) * (m.menuQuantity || 0)) : it.quantity }}</div>
              </div>
              <div v-if="!m.isPackage" class="unit text-muted">单价：<span class="text-price">￥{{ Number(it.unitPrice).toFixed(2) }}</span></div>
            </div>
          </div>
        </div>
      </div>
      <div v-else>
        <div v-for="it in order.items" :key="`${it.menuId}-${it.dishName}-${it.unitPrice}`" class="card">
          <div>{{ it.menuName || '—' }} / {{ it.dishName }}</div>
          <div>数量：{{ it.quantity }} · 单价：￥{{ it.unitPrice }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrder, cancelOrder, getMenuItems, getMenus } from '../api'
import { useCartStore } from '../stores/cart'

const route = useRoute()
const router = useRouter()
const id = Number(route.params.id)
const order = ref(null)
const loading = ref(true)
const error = ref('')
const cart = useCartStore()

onMounted(async () => {
  try {
    order.value = await getOrder(id)
  } catch (e) {
    if (e.response && e.response.status === 401) {
      const ret = encodeURIComponent(window.location.href)
      const BACKEND_BASE = import.meta.env.VITE_BACKEND_BASE
      window.location.href = `${BACKEND_BASE}/login.jsp?redirect=${ret}`
      return
    }
    error.value = '加载失败'
  } finally {
    loading.value = false
  }
})

function back() { router.push('/orders/history') }
function gotoRestaurant() { if (order.value && order.value.restaurantId) router.push(`/restaurants/${order.value.restaurantId}`) }

function sortItems(m) {
  return (m.items || []).slice().sort((a,b) => (a.sortOrder || 0) - (b.sortOrder || 0))
}

const busyCancel = ref(false)
async function onCancel() {
  if (busyCancel.value) return
  try {
    busyCancel.value = true
    await cancelOrder(id)
    order.value = await getOrder(id)
  } catch (e) {
    error.value = '取消失败'
  } finally {
    busyCancel.value = false
  }
}

const busyReorder = ref(false)
async function onReorder() {
  if (busyReorder.value) return
  if (!order.value) return
  const rid = order.value.restaurantId
  if (!rid) { error.value = '无法识别餐厅'; return }
  try {
    busyReorder.value = true
    cart.clearCart(rid)
    let currentMenus = []
    try { currentMenus = await getMenus(rid) } catch (e) { currentMenus = [] }
    const menuMap = new Map(currentMenus.map(m => [m.menuId, m]))
    const desiredQtyByMenu = {}
    if (order.value.menus && order.value.menus.length) {
      for (const m of order.value.menus) { desiredQtyByMenu[m.menuId] = m.menuQuantity || 0 }
    }
    const itemsByMenu = {}
    if (order.value.items && order.value.items.length) {
      for (const it of order.value.items) {
        if (!it.menuId) continue
        (itemsByMenu[it.menuId] ||= []).push(it)
      }
    }
    const menuIds = Array.from(new Set([...
      Object.keys(desiredQtyByMenu).map(x => Number(x)),
      ...Object.keys(itemsByMenu).map(x => Number(x))
    ]))
    for (const mid of menuIds) {
      if (!menuMap.has(mid)) continue
      const curMenu = menuMap.get(mid)
      const res = await getMenuItems(mid)
      if (curMenu.isPackage) {
        let qty = desiredQtyByMenu[mid] || 0
        if (!qty || qty <= 0) {
          let calc = null
          const byDish = new Map((res.items || []).map(x => [x.dishId, x]))
          const list = itemsByMenu[mid] || []
          for (const it of list) {
            const cur = byDish.get(it.dishId)
            if (!cur) continue
            const dq = cur.defaultQuantity || cur.quantity || 0
            if (!dq) continue
            const v = Math.floor((it.quantity || 0) / dq)
            calc = calc == null ? v : Math.min(calc, v)
          }
          qty = calc && calc > 0 ? calc : 1
        }
        const mapped = (res.items || []).map(x => ({ dishId: x.dishId, sortOrder: x.sortOrder || 0, quantity: x.defaultQuantity || x.quantity || 1, name: x.name, price: x.price }))
        if (mapped.length > 0) cart.addPackage(rid, mid, mapped, qty, curMenu.name || '', { version: res.version, signature: res.signature })
      } else {
        const byDish = new Map((res.items || []).map(x => [x.dishId, x]))
        const byName = new Map((res.items || []).map(x => [x.name, x]))
        const list = itemsByMenu[mid] || []
        for (const it of list) {
          let cur = null
          if (it.dishId !== undefined && it.dishId !== null) cur = byDish.get(it.dishId)
          if (!cur && it.dishName) cur = byName.get(it.dishName)
          if (!cur) continue
          const qty = Math.max(1, parseInt(it.quantity || 0))
          const dishId = cur.dishId !== undefined ? cur.dishId : it.dishId
          cart.addItem(rid, mid, { dishId: dishId, sortOrder: cur.sortOrder || 0, quantity: qty, name: it.dishName, price: cur.price || it.unitPrice }, curMenu.name || '')
        }
      }
    }
    router.push(`/restaurants/${rid}?openCart=1`)
  } catch (e) {
    error.value = '再次购买失败'
  } finally {
    busyReorder.value = false
  }
}
</script>
<style>
.card { border: 1px solid #e5e7eb; border-radius: 8px; padding: 12px; margin-bottom: 12px; }
ul { list-style: none; padding: 0; }
li { padding: 4px 0; }
.back-btn { margin-bottom: 8px; }
.menu-header { display: flex; justify-content: space-between; align-items: center; }
.menu-title { font-weight: 600; margin-bottom: 6px; }
.menu-unit { color: #666; font-size: 12px; margin: 4px 0 8px; }
.badge { margin-left: 6px; font-size: 12px; color: #b00020; }
.item { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px dashed #eee; padding: 8px 0; }
.info { display: flex; flex-direction: column; gap: 4px; width: 100%; }
.row { display: flex; justify-content: space-between; align-items: center; width: 100%; }
.price { color: #333; }
.unit { color: #666; font-size: 12px; }
.pkg { display: flex; gap: 8px; align-items: center; }
.pkg-qty { min-width: 20px; text-align: center; }
.actions { display: flex; gap: 8px; margin-bottom: 8px; }
.btn-cancel { background: #dc3545; color: #fff; border: none; padding: 6px 10px; border-radius: 4px; }
.btn-reorder { background: #0d6efd; color: #fff; border: none; padding: 6px 10px; border-radius: 4px; }
.btn-restaurant { background: #198754; color: #fff; border: none; padding: 6px 10px; border-radius: 4px; margin: 4px 0 8px; }
.btn-cancel:disabled { opacity: .7; }
.btn-reorder:disabled { opacity: .7; }
</style>
const canReorder = computed(() => {
  const o = order.value
  if (!o) return false
  if (o.menus && o.menus.length) return true
  if (o.items && o.items.length) return true
  return false
})