import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const backendTarget = env.VITE_BACKEND_TARGET
  const backendContext = env.VITE_BACKEND_CONTEXT
  return {
    plugins: [vue()],
    server: {
      port: Number(env.VITE_DEV_PORT || 5173),
      proxy: backendTarget && backendContext ? {
        [`${backendContext}/api`]: {
          target: backendTarget,
          changeOrigin: true
        }
      } : {}
    }
  }
})