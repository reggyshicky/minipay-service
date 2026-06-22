<template>
  <v-card elevation="4" rounded="lg">
    <v-card-title class="d-flex justify-space-between align-center">
      <span>Transaction History</span>
      <v-btn icon="mdi-refresh" size="small" variant="text" @click="$emit('refresh')" />
    </v-card-title>

    <v-data-table
        :headers="headers"
        :items="payments"
        :loading="loading"
        item-value="id"
        :items-per-page="-1"
        :hide-default-footer="hideDefaultFooter"
    >
      <template #item.amount="{ item }">
        KES {{ Number(item.amount).toLocaleString() }}
      </template>

      <template #item.status="{ item }">
        <v-chip :color="statusColor(item.status)" size="small" variant="flat">
          {{ item.status }}
        </v-chip>
      </template>

      <template #item.failureReason="{ item }">
        <v-tooltip v-if="item.failureReason" location="top" max-width="400">
          <template #activator="{ props }">
            <span v-bind="props" class="text-error text-body-2 text-truncate d-inline-block" style="max-width: 150px;">
              {{ item.failureReason }}
            </span>
          </template>
          <span>{{ item.failureReason }}</span>
        </v-tooltip>
        <span v-else class="text-grey">—</span>
      </template>

      <template #item.createdAt="{ item }">
        {{ formatDate(item.createdAt) }}
      </template>

      <template #item.actions="{ item }">
        <v-btn icon="mdi-eye" size="small" variant="text" @click="viewDetails(item.id)" />
      </template>
    </v-data-table>

    <v-card-actions v-if="showViewAll" class="justify-center">
      <v-btn variant="text" color="primary" to="/payments">
        View Full History
        <v-icon end icon="mdi-arrow-right" />
      </v-btn>
    </v-card-actions>
  </v-card>

  <v-dialog v-model="dialogOpen" max-width="500">
    <v-card rounded="lg">
      <v-card-title class="d-flex justify-space-between align-center">
        <span>Payment Details</span>
        <v-btn icon="mdi-close" size="small" variant="text" @click="dialogOpen = false" />
      </v-card-title>

      <v-card-text v-if="detailsLoading" class="text-center py-8">
        <v-progress-circular indeterminate color="primary" />
      </v-card-text>

      <v-card-text v-else-if="selectedPayment">
        <v-list density="comfortable">
          <v-list-item title="Payment ID" :subtitle="selectedPayment.id" />
          <v-list-item title="Reference" :subtitle="selectedPayment.reference || '—'" />
          <v-list-item title="Amount" :subtitle="`KES ${Number(selectedPayment.amount).toLocaleString()}`" />
          <v-list-item title="Phone Number" :subtitle="selectedPayment.phoneNumber" />
          <v-list-item title="Payment Method" :subtitle="selectedPayment.paymentMethod" />
          <v-list-item title="Status">
            <template #subtitle>
              <v-chip :color="statusColor(selectedPayment.status)" size="small" variant="flat">
                {{ selectedPayment.status }}
              </v-chip>
            </template>
          </v-list-item>
          <v-list-item v-if="selectedPayment.failureReason" title="Failure Reason" :subtitle="selectedPayment.failureReason" />
          <v-list-item title="Created At" :subtitle="formatDate(selectedPayment.createdAt)" />
          <v-list-item title="Updated At" :subtitle="formatDate(selectedPayment.updatedAt)" />
        </v-list>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script setup>
import { ref } from 'vue'
import api from '@/services/api'

const props = defineProps({
  payments: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  showViewAll: { type: Boolean, default: false },
  hideDefaultFooter: { type: Boolean, default: true },
})
defineEmits(['refresh'])

const headers = [
  { title: 'Reference', key: 'reference' },
  { title: 'Amount', key: 'amount' },
  { title: 'Phone', key: 'phoneNumber' },
  { title: 'Method', key: 'paymentMethod' },
  { title: 'Status', key: 'status' },
  { title: 'Failure Reason', key: 'failureReason' },
  { title: 'Date', key: 'createdAt' },
  { title: '', key: 'actions', sortable: false, align: 'center' },
]

const dialogOpen = ref(false)
const detailsLoading = ref(false)
const selectedPayment = ref(null)

async function viewDetails(paymentId) {
  dialogOpen.value = true
  detailsLoading.value = true
  selectedPayment.value = null
  try {
    const response = await api.get(`/payments/${paymentId}`)
    selectedPayment.value = response.data.data
  } catch (err) {
    console.error('Failed to load payment details', err)
  } finally {
    detailsLoading.value = false
  }
}

function statusColor(status) {
  switch (status) {
    case 'SUCCESS': return 'success'
    case 'FAILED': return 'error'
    case 'PENDING': return 'warning'
    default: return 'grey'
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}
</script>