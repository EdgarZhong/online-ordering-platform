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
      const BACKEND_BASE = (
        import.meta.env.VITE_BACKEND_BASE
        || (window.location.origin + (import.meta.env.VITE_BACKEND_CONTEXT || ''))
      )
      window.location.href = `${BACKEND_BASE}/logout`
    }
  }
})