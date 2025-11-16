import { defineStore } from 'pinia'

export const useCartStore = defineStore('cart', {
  state: () => ({
    carts: {}
  }),
  getters: {
    getCart: (state) => (restaurantId) => state.carts[restaurantId] || { menus: [], total: 0 }
  },
  actions: {
    initFromStorage() {
      try {
        const raw = localStorage.getItem('consumer_cart')
        if (raw) {
          const obj = JSON.parse(raw)
          if (obj && typeof obj === 'object') this.carts = obj
        }
      } catch (e) {}
    },
    persist() {
      try { localStorage.setItem('consumer_cart', JSON.stringify(this.carts)) } catch (e) {}
    },
    addItem(restaurantId, menuId, item, menuName) {
      const cart = this.carts[restaurantId] || { menus: [], total: 0 }
      let menu = cart.menus.find(m => m.menuId === menuId)
      if (!menu) { menu = { menuId, isPackage: false, quantity: 0, items: [], menuName: menuName }; cart.menus.push(menu) }
      if (menuName) menu.menuName = menuName
      const idx = menu.items.findIndex(i => i.dishId === item.dishId)
      if (idx >= 0) {
        menu.items[idx].quantity += item.quantity
      } else {
        menu.items.push({ dishId: item.dishId, sortOrder: item.sortOrder, quantity: item.quantity, name: item.name, price: item.price })
      }
      cart.total = cart.menus.reduce((s, m) => s + m.items.reduce((ss, it) => ss + (it.price || 0) * (m.isPackage ? (it.quantity * (m.quantity || 1)) : it.quantity), 0), 0)
      this.carts[restaurantId] = cart
      this.persist()
    },
    addPackage(restaurantId, menuId, items, quantity, menuName, meta) {
      const cart = this.carts[restaurantId] || { menus: [], total: 0 }
      let menu = cart.menus.find(m => m.menuId === menuId)
      if (!menu) { menu = { menuId, isPackage: true, quantity: 0, items: [], menuName: menuName, menuVersion: '', menuSignature: '' }; cart.menus.push(menu) }
      menu.isPackage = true
      if (menuName) menu.menuName = menuName
      menu.items = items.map(i => ({ dishId: i.dishId, sortOrder: i.sortOrder, quantity: i.quantity, name: i.name, price: i.price }))
      menu.quantity = quantity
      if (meta) { menu.menuVersion = meta.version || ''; menu.menuSignature = meta.signature || '' }
      cart.total = cart.menus.reduce((s, m) => s + m.items.reduce((ss, it) => ss + (it.price || 0) * (m.isPackage ? (it.quantity * (m.quantity || 1)) : it.quantity), 0), 0)
      this.carts[restaurantId] = cart
      this.persist()
    },
    updateItemQty(restaurantId, menuId, dishId, qty) {
      const cart = this.carts[restaurantId]
      if (!cart) return
      const menu = cart.menus.find(m => m.menuId === menuId)
      if (!menu) return
      const it = menu.items.find(i => i.dishId === dishId)
      if (it) {
        it.quantity = qty
        if (it.quantity <= 0) {
          menu.items = menu.items.filter(i => i.dishId !== dishId)
          if (!menu.isPackage && menu.items.length === 0) {
            cart.menus = cart.menus.filter(m => m.menuId !== menuId)
          }
        }
        cart.total = cart.menus.reduce((s, m) => s + m.items.reduce((ss, it2) => ss + (it2.price || 0) * (m.isPackage ? (it2.quantity * (m.quantity || 1)) : it2.quantity), 0), 0)
      }
      this.persist()
    },
    updatePackageQty(restaurantId, menuId, qty) {
      const cart = this.carts[restaurantId]
      if (!cart) return
      const menu = cart.menus.find(m => m.menuId === menuId)
      if (!menu) return
      menu.quantity = qty
      if ((menu.quantity || 0) <= 0) {
        cart.menus = cart.menus.filter(m => m.menuId !== menuId)
      }
      cart.total = cart.menus.reduce((s, m) => s + m.items.reduce((ss, it2) => ss + (it2.price || 0) * (m.isPackage ? (it2.quantity * (m.quantity || 1)) : it2.quantity), 0), 0)
      this.persist()
    },
    removeItem(restaurantId, menuId, dishId) {
      const cart = this.carts[restaurantId]
      if (!cart) return
      const menu = cart.menus.find(m => m.menuId === menuId)
      if (!menu) return
      menu.items = menu.items.filter(i => i.dishId !== dishId)
      if (!menu.isPackage && menu.items.length === 0) {
        cart.menus = cart.menus.filter(m => m.menuId !== menuId)
      }
      cart.total = cart.menus.reduce((s, m) => s + m.items.reduce((ss, it) => ss + (it.price || 0) * (m.isPackage ? (it.quantity * (m.quantity || 1)) : it.quantity), 0), 0)
      this.persist()
    },
    clearCart(restaurantId) {
      this.carts[restaurantId] = { menus: [], total: 0 }
      this.persist()
    },
    toOrderPayload(restaurantId) {
      const cart = this.carts[restaurantId]
      if (!cart || !cart.menus.length) return { restaurantId, menus: [] }
      return {
        restaurantId,
        menus: cart.menus.map(m => ({
          menuId: m.menuId,
          quantity: m.isPackage ? (m.quantity || 0) : 0,
          items: m.items.map(it => ({ dishId: it.dishId, sortOrder: it.sortOrder, quantity: it.quantity })),
          menuVersion: m.menuVersion || '',
          menuSignature: m.menuSignature || ''
        }))
      }
    }
  }
})