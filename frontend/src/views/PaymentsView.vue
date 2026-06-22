<template>
  <v-app-bar color="primary" elevation="2">
    <v-btn icon="mdi-arrow-left" to="/" />
    <v-app-bar-title>Transaction History</v-app-bar-title>
  </v-app-bar>

  <v-main>
    <v-container fluid>
      <v-card elevation="2" rounded="lg" class="mb-4">
        <v-card-text>
          <v-text-field
              v-model="searchQuery"
              label="Search by reference or phone number"
              prepend-inner-icon="mdi-magnify"
              variant="outlined"
              density="comfortable"
              clearable
              hide-details
              @update:model-value="onSearchChange"
          />
        </v-card-text>
      </v-card>

      <TransactionTable :payments="filteredPayments" :loading="loading" @refresh="loadPayments" />

      <v-pagination
          v-if="totalPages > 1 && !searchQuery"
          v-model="currentPage"
          :length="totalPages"
          class="mt-4"
          @update:model-value="loadPayments"
      />
    </v-container>
  </v-main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '@/services/api'
import TransactionTable from '@/components/TransactionTable.vue'

const payments = ref([])
const loading = ref(false)
const currentPage = ref(1)
const totalPages = ref(1)
const searchQuery = ref('')

async function loadPayments() {
  loading.value = true
  try {
    const response = await api.get(`/payments?page=${currentPage.value - 1}&size=10&sort=createdAt,desc`)
    payments.value = response.data.data.content
    totalPages.value = response.data.data.totalPages
  } catch (err) {
    console.error('Failed to load payments', err)
  } finally {
    loading.value = false
  }
}

function onSearchChange() {
}

const filteredPayments = computed(() => {
  if (!searchQuery.value) return payments.value
  const query = searchQuery.value.toLowerCase()
  return payments.value.filter((p) =>
      p.reference?.toLowerCase().includes(query) ||
      p.phoneNumber?.toLowerCase().includes(query)
  )
})

onMounted(loadPayments)
</script>