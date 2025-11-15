import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const backendTarget = 'http://localhost:8080'
const context = '/online_ordering_backend_war_exploded'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      [`${context}/api`]: {
        target: backendTarget,
        changeOrigin: true
      }
    }
  }
})