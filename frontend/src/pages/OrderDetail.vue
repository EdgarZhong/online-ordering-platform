<template>
  <div>
    <div v-if="loading">加载中...</div>
    <div v-else-if="error" class="card">{{ error }}</div>
    <div v-else>
      <button class="back-btn" @click="back">返回</button>
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
import { getOrder } from '../api'

const route = useRoute()
const router = useRouter()
const id = Number(route.params.id)
const order = ref(null)
const loading = ref(true)
const error = ref('')

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
</style>