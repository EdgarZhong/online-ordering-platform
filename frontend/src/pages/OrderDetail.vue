<template>
  <div>
    <div v-if="loading">加载中...</div>
    <div v-else-if="error" class="card">{{ error }}</div>
    <div v-else>
      <div class="actions">
        <button class="back-btn" @click="back">返回</button>
        <button v-if="order.status==='PENDING'" class="btn-cancel" @click="onCancel">取消订单</button>
        <button class="btn-reorder" @click="onReorder">再次购买</button>
      </div>
      <h2>订单 {{ order.orderId }}</h2>
      <p>状态：{{ order.status }} · 金额：￥{{ order.totalPrice }}</p>
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
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrder, cancelOrder, getMenuItems } from '../api'
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
      window.location.href = `http://localhost:8080/online_ordering_backend_war_exploded/login.jsp?redirect=${ret}`
      return
    }
    error.value = '加载失败'
  } finally {
    loading.value = false
  }
})

function back() { router.push('/orders/history') }

function sortItems(m) {
  return (m.items || []).slice().sort((a,b) => (a.sortOrder || 0) - (b.sortOrder || 0))
}

async function onCancel() {
  try {
    await cancelOrder(id)
    order.value = await getOrder(id)
  } catch (e) {
    error.value = '取消失败'
  }
}

async function onReorder() {
  if (!order.value) return
  const rid = order.value.restaurantId
  if (!rid) { error.value = '无法识别餐厅'; return }
  try {
    if (order.value.menus && order.value.menus.length) {
      for (const m of order.value.menus) {
        if (m.isPackage) {
          const res = await getMenuItems(m.menuId)
          const mapped = (res.items || []).map(it => ({
            dishId: it.dishId,
            sortOrder: it.sortOrder || 0,
            quantity: it.defaultQuantity || it.quantity || 1,
            name: it.name,
            price: it.price
          }))
          cart.addPackage(rid, m.menuId, mapped, m.menuQuantity || 1, m.menuName, { version: res.version, signature: res.signature })
        } else {
          for (const it of (m.items || [])) {
            cart.addItem(rid, m.menuId, { dishId: it.dishId, sortOrder: it.sortOrder || 0, quantity: it.quantity, name: it.dishName, price: it.unitPrice }, m.menuName)
          }
        }
      }
    } else if (order.value.items && order.value.items.length) {
      for (const it of order.value.items) {
        cart.addItem(rid, it.menuId, { dishId: it.dishId, sortOrder: 0, quantity: it.quantity, name: it.dishName, price: it.unitPrice }, it.menuName)
      }
    }
    router.push(`/restaurants/${rid}`)
  } catch (e) {
    error.value = '再次购买失败'
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
</style>