<template>
  <div class="app">
    <header class="app-header">
      <h1>在线点餐</h1>
      <nav class="nav">
        <router-link to="/">餐厅列表</router-link>
        <router-link to="/orders/history">历史订单</router-link>
      </nav>
      <div v-if="user" class="user" ref="userEl">
        <button class="user-btn" @click="toggleMenu">欢迎，{{ user.username }} ▾</button>
        <div v-if="menuOpen" class="menu">
          <a href="#" @click.prevent="openChangePwd">修改密码</a>
          <a href="#" @click.prevent="logout">退出登录</a>
        </div>
      </div>
    </header>
    <main>
      <router-view />
    </main>
    <div v-if="showChangePwd" class="modal-mask" @click.self="showChangePwd=false">
      <div class="modal">
        <h3>修改密码</h3>
        <div class="form">
          <label>当前密码</label>
          <input type="password" v-model="oldPwd" />
          <label>新密码</label>
          <input type="password" v-model="newPwd" />
          <label>确认新密码</label>
          <input type="password" v-model="confirmPwd" />
        </div>
        <div class="actions">
          <button @click="showChangePwd=false">取消</button>
          <button :disabled="loading" @click="submitChangePwd">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount, ref } from 'vue'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()
const user = computed(() => auth.user)
const menuOpen = ref(false)
const userEl = ref(null)
  function toggleMenu() { menuOpen.value = !menuOpen.value }
  function logout() { auth.logout() }
  const showChangePwd = ref(false)
  const oldPwd = ref('')
  const newPwd = ref('')
  const confirmPwd = ref('')
  const loading = ref(false)
  function openChangePwd() { showChangePwd.value = true; menuOpen.value = false }
  async function submitChangePwd() {
    if (!oldPwd.value || !newPwd.value || !confirmPwd.value) { alert('请填写所有字段'); return }
    if (newPwd.value !== confirmPwd.value) { alert('两次输入的新密码不一致'); return }
    try {
      loading.value = true
      await auth.changePassword(oldPwd.value, newPwd.value)
      alert('密码修改成功')
      showChangePwd.value = false
      oldPwd.value = newPwd.value = confirmPwd.value = ''
    } catch (e) {
      alert('修改失败：' + (e?.response?.data?.error || e.message || '未知错误'))
    } finally {
      loading.value = false
    }
  }
function onDocClick(e) {
  if (!userEl.value) return
  if (!userEl.value.contains(e.target)) menuOpen.value = false
}
onMounted(() => { auth.fetchSession(); document.addEventListener('click', onDocClick) })
onBeforeUnmount(() => { document.removeEventListener('click', onDocClick) })
</script>

<style>
.app { font-family: system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif; }
.app-header { padding: 12px 16px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
main { padding: 16px; }
a { color: #1a73e8; text-decoration: none; }
ul { list-style: none; padding: 0; }
.card { border: 1px solid #e5e7eb; border-radius: 8px; padding: 12px; margin-bottom: 12px; }
.user { position: relative; }
.user-btn { background: #fff; border: 1px solid #e5e7eb; border-radius: 6px; padding: 6px 10px; cursor: pointer; }
.menu { position: absolute; right: 0; top: 36px; background: #fff; border: 1px solid #eee; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); padding: 8px 12px; min-width: 160px; }
.menu a, .menu .router-link-active, .menu .router-link { display: block; padding: 6px 0; color: #1a73e8; }
.nav { display: flex; gap: 16px; align-items: center; }
.nav .router-link-active { font-weight: 600; }

/* Typography (reusable) */
.text-menu-name { font-size: 20px; font-weight: 700; color: #111; }
.text-dish-name { font-size: 16px; font-weight: 600; color: #222; }
.text-price { font-size: 14px; font-weight: 600; color: #111; }
.text-qty { font-size: 14px; font-weight: 500; color: #444; }
.text-muted { color: #666; }
</style>

 

<style>
.modal-mask { position: fixed; left:0; top:0; right:0; bottom:0; background: rgba(0,0,0,0.3); display:flex; align-items:center; justify-content:center; }
.modal { background:#fff; border-radius:8px; padding:16px; width:320px; box-shadow:0 6px 20px rgba(0,0,0,0.12); }
.modal h3 { margin:0 0 8px; }
.form { display:flex; flex-direction:column; gap:8px; }
.form input { border:1px solid #e5e7eb; border-radius:6px; padding:6px 8px; }
.modal .actions { display:flex; justify-content:flex-end; gap:8px; margin-top:12px; }
.modal .actions button { padding:6px 12px; border:1px solid #e5e7eb; border-radius:6px; background:#fff; cursor:pointer; }
.modal .actions button[disabled] { opacity:.6; cursor:not-allowed; }
</style>