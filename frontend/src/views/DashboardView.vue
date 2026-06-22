<template>
  <v-app-bar color="primary" elevation="2">
    <v-app-bar-title>MiniPay Dashboard</v-app-bar-title>
    <v-spacer />
    <span class="mr-4">{{ authStore.username }}</span>
    <v-btn variant="text" to="/payments">History</v-btn>
    <v-btn variant="text" @click="handleLogout">Logout</v-btn>
  </v-app-bar>

  <v-main>
    <v-container fluid>
      <v-row>
        <v-col cols="12" md="5">
          <PaymentForm @payment-completed="loadRecentPayments" />
        </v-col>
        <v-col cols="12" md="7">
          <TransactionTable :payments="recentPayments" :loading="loading" :show-view-all="true" @refresh="loadRecentPayments" />        </v-col>
      </v-row>
    </v-container>
  </v-main>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.js'
import api from '@/services/api.js'
import PaymentForm from '@/components/PaymentForm.vue'
import TransactionTable from '@/components/TransactionTable.vue'

const router = useRouter()
const authStore = useAuthStore()
const recentPayments = ref([])
const loading = ref(false)

async function loadRecentPayments() {
  loading.value = true
  try {
    const response = await api.get('/payments?page=0&size=5&sort=createdAt,desc')
    recentPayments.value = response.data.data.content
  } catch (err) {
    console.error('Failed to load payments', err)
  } finally {
    loading.value = false
  }
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}

onMounted(loadRecentPayments)
</script>