import { defineStore } from 'pinia'
import { getSession, changePassword } from '../api'

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
      const BACKEND_BASE = import.meta.env.VITE_BACKEND_BASE
      window.location.href = `${BACKEND_BASE}/logout`
    },
    async changePassword(oldPassword, newPassword) {
      const res = await changePassword({ oldPassword, newPassword })
      return res
    }
  }
})