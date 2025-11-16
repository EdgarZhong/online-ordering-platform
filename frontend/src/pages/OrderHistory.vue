<template>
  <div>
    <h2>历史订单</h2>
    <div v-if="loading">加载中...</div>
    <div v-else>
      <div v-if="error" class="card">{{ error }}</div>
      <div v-else>
        <div v-if="orders.length === 0" class="card">暂无订单</div>
        <div v-else>
          <div v-for="o in sortedOrders" :key="o.orderId" class="card order-card" @click="go(o.orderId)">
            <div class="row">
              <div class="restaurant text-menu-name">{{ o.restaurantName }}：{{ o.serialNumber }}</div>
              <div class="price text-price">￥{{ Number(o.totalPrice).toFixed(2) }}</div>
            </div>
            <div class="row">
              <div class="status badge">{{ o.status }}</div>
              <div class="time text-muted">{{ o.createdAt }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  </template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getOrders } from '../api'

const orders = ref([])
const loading = ref(true)
const error = ref('')
const router = useRouter()

onMounted(async () => {
  try {
    orders.value = await getOrders()
  } catch (e) {
    error.value = '加载失败'
  } finally {
    loading.value = false
  }
})

const sortedOrders = computed(() => orders.value.slice().sort((a,b) => new Date(b.createdAt) - new Date(a.createdAt)))
function go(id) { router.push(`/orders/${id}`) }
</script>
<style>
.card { border: 1px solid #e5e7eb; border-radius: 8px; padding: 12px; margin-bottom: 12px; }
.order-card .row { display: flex; justify-content: space-between; align-items: center; }
.order-card { cursor: pointer; }
ul { list-style: none; padding: 0; }
li { padding: 4px 0; }
</style>