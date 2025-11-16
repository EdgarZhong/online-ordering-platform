<template>
  <div class="checkout">
    <div class="sum">合计：￥{{ total.toFixed(2) }}</div>
    <button :disabled="submitting || total === 0" @click="submit">提交订单</button>
    <div v-if="error" class="error">{{ error }}</div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { createOrder } from '../api'

const props = defineProps({ restaurantId: { type: Number, required: true } })
const router = useRouter()
const cart = useCartStore()
const total = computed(() => cart.getCart(props.restaurantId).total || 0)
const submitting = ref(false)
const error = ref('')

async function submit() {
  error.value = ''
  const payload = cart.toOrderPayload(props.restaurantId)
  if (!payload.menus.length) { error.value = '购物车为空'; return }
  submitting.value = true
  try {
    const res = await createOrder(payload)
    cart.clearCart(props.restaurantId)
    router.push(`/orders/${res.orderId}`)
  } catch (e) {
    if (e.response && e.response.status === 401) {
      const ret = encodeURIComponent(window.location.href)
      window.location.href = `http://localhost:8080/online_ordering_backend_war_exploded/login.jsp?redirect=${ret}`
      return
    }
    if (e.response && e.response.status === 409) {
      window.alert('下单失败：菜单已变更，请刷新页面')
      return
    }
    error.value = '提交失败'
  } finally {
    submitting.value = false
  }
}
</script>

<style>
.checkout { display: flex; gap: 12px; align-items: center; }
.sum { font-weight: 600; }
.error { color: #b00020; }
</style>