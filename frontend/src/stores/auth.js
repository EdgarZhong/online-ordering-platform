import { defineStore } from 'pinia'
import { getSession } from '../api'

export const useAuthStore = defineStore('auth', {
  state: () => ({ user: null }),
  actions: {
    async fetchSession() {
      try {
        this.user = await getSession()
      } catch (e) {
        this.user = null
      }
    },
    logout() {
      window.location.href = 'http://localhost:8080/online_ordering_backend_war_exploded/logout'
    }
  }
})