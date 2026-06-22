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
        hide-default-footer
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
    </v-data-table>

    <v-card-actions v-if="showViewAll" class="justify-center">
      <v-btn variant="text" color="primary" to="/payments">
        View Full History
        <v-icon end icon="mdi-arrow-right" />
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script setup>
defineProps({
  payments: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  showViewAll: { type: Boolean, default: false },
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
]

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