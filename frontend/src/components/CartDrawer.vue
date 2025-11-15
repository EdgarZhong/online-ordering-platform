<template>
  <div class="drawer" v-if="open">
    <div class="drawer-header">
      <div>购物车</div>
      <button @click="$emit('close')">关闭</button>
    </div>
    <div v-if="cart.menus.length === 0" class="empty">购物车为空</div>
    <div v-else>
      <div v-for="m in (cart.menus || []).filter(x => (x.items || []).length > 0)" :key="m.menuId" class="menu-block">
        <div class="menu-header">
          <div class="menu-title text-menu-name">{{ m.menuName || ('菜单 ' + m.menuId) }}<span v-if="m.isPackage" class="badge">套餐</span></div>
          <div v-if="m.isPackage" class="pkg">
            <button @click="decPkg(m)">-</button>
            <span class="pkg-qty">{{ m.quantity || 0 }}</span>
            <button @click="incPkg(m)">+</button>
          </div>
        </div>
        <div v-if="m.isPackage" class="menu-unit text-muted">单价：<span class="text-price">￥{{ menuUnitPrice(m).toFixed(2) }}</span></div>
        <div v-for="it in m.items.slice().sort((a,b)=> (a.sortOrder||0)-(b.sortOrder||0))" :key="`${m.menuId}-${it.dishId}`" class="item">
          <div class="info">
            <div class="row">
              <div class="name text-dish-name">{{ it.name }}</div>
              <div v-if="!m.isPackage" class="actions-inline">
                <button @click="dec(m, it)">-</button>
                <span class="qty">{{ it.quantity }}</span>
                <button @click="inc(m, it)">+</button>
              </div>
              <div v-else class="price text-price">￥{{ Number(it.price).toFixed(2) }} × {{ qty(m, it) }}</div>
            </div>
            <div v-if="!m.isPackage" class="unit text-muted">单价：<span class="text-price">￥{{ Number(it.price).toFixed(2) }}</span></div>
          </div>
        </div>
      </div>
      <div class="total">合计：￥{{ cart.total.toFixed(2) }}</div>
      <div class="footer">
        <button @click="clear">清空</button>
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, watch } from 'vue'
import { useCartStore } from '../stores/cart'

const props = defineProps({ open: { type: Boolean, default: false }, restaurantId: { type: Number, required: true } })
const cartStore = useCartStore()
const cart = computed(() => cartStore.getCart(props.restaurantId))
const pkgQty = reactive({})
watch(cart, (c) => {
  const menus = (c.menus || [])
  // 清理已移除菜单的缓存
  Object.keys(pkgQty).forEach(k => { if (!menus.find(m => String(m.menuId) === String(k))) delete pkgQty[k] })
  menus.forEach(m => { pkgQty[m.menuId] = m.quantity || 0 })
}, { immediate: true, deep: true })

function inc(m, it) {
  cartStore.updateItemQty(props.restaurantId, m.menuId, it.dishId, it.quantity + 1)
}
function dec(m, it) {
  const q = it.quantity - 1
  cartStore.updateItemQty(props.restaurantId, m.menuId, it.dishId, q)
}
function remove(m, it) {
  cartStore.removeItem(props.restaurantId, m.menuId, it.dishId)
}
function clear() {
  cartStore.clearCart(props.restaurantId)
}
function updatePkg(m) {
  const q = Math.max(0, pkgQty[m.menuId] || 0)
  cartStore.updatePackageQty(props.restaurantId, m.menuId, q)
}
function menuUnitPrice(m) {
  return (m.items || []).reduce((s, it) => s + Number(it.price || 0) * Number(it.quantity || 0), 0)
}
function qty(m, it) {
  return m.isPackage ? (it.quantity * (m.quantity || 0)) : it.quantity
}
function incPkg(m) {
  const newQty = (m.quantity || 0) + 1
  pkgQty[m.menuId] = newQty
  updatePkg(m)
}
function decPkg(m) {
  const newQty = (m.quantity || 0) - 1
  pkgQty[m.menuId] = newQty
  updatePkg(m)
}
</script>

<style>
.drawer { position: fixed; right: 0; top: 0; width: 360px; height: 100vh; background: #fff; border-left: 1px solid #eee; box-shadow: -2px 0 8px rgba(0,0,0,0.05); padding: 12px; overflow-y: auto; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.empty { color: #666; padding: 12px; }
.menu-block { border-bottom: 1px solid #f0f0f0; margin-bottom: 8px; }
.menu-header { display: flex; justify-content: space-between; align-items: center; }
.menu-title { font-weight: 600; margin-bottom: 6px; }
.menu-unit { color: #666; font-size: 12px; margin: 4px 0 8px; }
.badge { margin-left: 6px; font-size: 12px; color: #b00020; }
.item { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px dashed #eee; padding: 8px 0; }
.info { display: flex; flex-direction: column; gap: 4px; width: 100%; }
.row { display: flex; justify-content: space-between; align-items: center; width: 100%; }
.actions-inline { display: flex; gap: 8px; align-items: center; }
.actions-inline .qty { min-width: 20px; text-align: center; }
.unit { color: #666; font-size: 12px; }
.name { font-weight: 600; }
.price { color: #333; }
.actions { display: flex; gap: 8px; align-items: center; }
.pkg { display: flex; gap: 8px; align-items: center; }
.pkg-qty { min-width: 20px; text-align: center; }
.remove { color: #b00020; }
.total { text-align: right; font-weight: 600; margin-top: 8px; }
.footer { display: flex; justify-content: space-between; align-items: center; margin-top: 10px; }
button { padding: 4px 8px; }
</style>