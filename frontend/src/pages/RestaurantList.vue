<template>
  <div>
    <h2>餐厅列表</h2>
    <div v-if="loading">加载中...</div>
    <div v-else>
      <div v-if="error" class="card">{{ error }}</div>
      <div v-else>
        <div v-for="r in restaurants" :key="r.restaurantId" class="card">
          <h3>{{ r.name }}</h3>
          <p>{{ r.description }}</p>
          <p>{{ r.address }} · {{ r.phone }}</p>
          <router-link :to="`/restaurants/${r.restaurantId}`">查看详情</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getRestaurants } from '../api'

const restaurants = ref([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    restaurants.value = await getRestaurants()
  } catch (e) {
    error.value = '加载失败'
  } finally {
    loading.value = false
  }
})
</script>