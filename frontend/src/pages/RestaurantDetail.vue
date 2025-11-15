<template>
  <div>
    <div v-if="loading">加载中...</div>
    <div v-else>
      <h2>{{ restaurant?.name }}</h2>
      <p>{{ restaurant?.description }}</p>
      <button class="open-cart" @click="drawerOpen = true">打开购物车</button>
      <div class="tabs">
        <button v-for="m in menus" :key="m.menuId" class="tab"
                :class="{ active: m.menuId === activeMenuId }"
                @click="activeMenuId = m.menuId">
          <span class="text-menu-name">{{ m.name }}</span><span v-if="m.isPackage" class="badge">套餐</span>
        </button>
      </div>
      <div class="menu-content" v-if="activeMenuId">
        <div v-if="isActiveMenuPackage" class="card">
          <h3>总价：￥{{ packageUnitPrice.toFixed(2) }}，包含：</h3>
          <ul>
            <li v-for="(it, idx) in sortedItems" :key="it.dishId" class="item-row">
              <span class="text-dish-name">{{ idx + 1 }}. {{ it.name }}</span>
              <span class="price text-price">￥{{ Number(it.price).toFixed(2) }} × {{ it.defaultQuantity }}</span>
            </li>
          </ul>
          <div class="actions">
            <span>份数：</span>
            <template v-if="pkgQtyOutside > 0">
              <button @click="decPkgOutside">-</button>
              <span>{{ pkgQtyOutside }}</span>
              <button @click="incPkgOutside">+</button>
            </template>
            <template v-else>
              <button @click="incPkgOutside">+</button>
            </template>
          </div>
        </div>
        <div v-else>
          <div v-for="item in items" :key="item.dishId" class="card">
            <h3><span class="text-dish-name">{{ item.name }}</span> <small class="text-price">￥{{ Number(item.price).toFixed(2) }}</small></h3>
            <p>{{ item.description }}</p>
            <div class="actions">
              <template v-if="getItemQty(item) > 0">
                <button @click="decItem(item)">-</button>
                <span>{{ getItemQty(item) }}</span>
                <button @click="incItem(item)">+</button>
              </template>
              <template v-else>
                <button @click="incItem(item)">+</button>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>
    <CartDrawer :open="drawerOpen" :restaurantId="rid" @close="drawerOpen = false">
      <CheckoutForm :restaurantId="rid" />
    </CartDrawer>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getRestaurant, getMenus, getMenuItems } from '../api'
import { useCartStore } from '../stores/cart'
import CartDrawer from '../components/CartDrawer.vue'
import CheckoutForm from '../components/CheckoutForm.vue'

const route = useRoute()
const rid = Number(route.params.id)
const restaurant = ref(null)
const menus = ref([])
const items = ref([])
const activeMenuId = ref(null)
const loading = ref(true)
const drawerOpen = ref(false)
const isActiveMenuPackage = computed(() => {
  const m = menus.value.find(mm => mm.menuId === activeMenuId.value)
  return !!(m && (m.isPackage || m.is_package))
})
const packageQty = ref(1)

const cart = useCartStore()

onMounted(async () => {
  try {
    restaurant.value = await getRestaurant(rid)
    menus.value = await getMenus(rid)
    activeMenuId.value = menus.value[0]?.menuId || null
  } finally {
    loading.value = false
  }
})

watch(activeMenuId, async (mid) => {
  if (!mid) return
  items.value = await getMenuItems(mid)
  packageQty.value = 1
})

function incItem(item) {
  const m = menus.value.find(mm => mm.menuId === activeMenuId.value)
  cart.addItem(rid, activeMenuId.value, { dishId: item.dishId, sortOrder: item.sortOrder, quantity: 1, name: item.name, price: item.price }, m?.name)
}
function decItem(item) {
  const q = getItemQty(item) - 1
  cart.updateItemQty(rid, activeMenuId.value, item.dishId, q)
}
function getItemQty(item) {
  const c = cart.getCart(rid)
  const m = (c.menus || []).find(mm => mm.menuId === activeMenuId.value)
  const it = m && m.items.find(ii => ii.dishId === item.dishId)
  return it ? it.quantity : 0
}
function incPkgOutside() {
  const c = cart.getCart(rid)
  const m = (c.menus || []).find(mm => mm.menuId === activeMenuId.value)
  if (m && m.isPackage) {
    cart.updatePackageQty(rid, activeMenuId.value, (m.quantity || 0) + 1)
  } else {
    const payloadItems = items.value.map(it => ({ dishId: it.dishId, sortOrder: (it.sortOrder ?? it.sort_order ?? 0), quantity: (it.defaultQuantity ?? it.quantity ?? 1), name: it.name, price: it.price }))
    const m = menus.value.find(mm => mm.menuId === activeMenuId.value)
    cart.addPackage(rid, activeMenuId.value, payloadItems, 1, m?.name)
  }
}
function decPkgOutside() {
  const c = cart.getCart(rid)
  const m = (c.menus || []).find(mm => mm.menuId === activeMenuId.value)
  if (m && m.isPackage) {
    cart.updatePackageQty(rid, activeMenuId.value, (m.quantity || 0) - 1)
  }
}

const pkgQtyOutside = computed(() => {
  const c = cart.getCart(rid)
  const m = (c.menus || []).find(mm => mm.menuId === activeMenuId.value)
  return m && m.isPackage ? (m.quantity || 0) : 0
})

const packageUnitPrice = computed(() => {
  return items.value.reduce((s, it) => s + Number(it.price || 0) * Number(it.defaultQuantity || it.quantity || 1), 0)
})
const sortedItems = computed(() => items.value.slice().sort((a,b)=> (a.sortOrder||0)-(b.sortOrder||0)))
</script>

<style>
.tabs { display: flex; gap: 8px; margin-bottom: 12px; }
.tab { padding: 6px 10px; border: 1px solid #ddd; background: #fff; cursor: pointer; }
.tab.active { background: #1a73e8; color: #fff; border-color: #1a73e8; }
button { padding: 6px 10px; }
.open-cart { margin: 8px 0; }
.badge { margin-left: 6px; font-size: 12px; color: #b00020; }
.item-row { display: flex; justify-content: space-between; align-items: center; }
.item-row .price { margin-left: 12px; white-space: nowrap; }
.menu-content { max-width: calc(100% - 380px); }
</style>