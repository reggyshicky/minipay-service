<template>
  <v-card elevation="4" rounded="lg">
    <v-card-title>New Payment</v-card-title>
    <v-card-text>
      <v-alert v-if="resultMessage" :type="resultType" variant="tonal" class="mb-4" closable @click:close="resultMessage = ''">
        {{ resultMessage }}
      </v-alert>

      <v-form @submit.prevent="submitPayment">
        <v-text-field
            v-model="amount"
            label="Amount (KES)"
            type="number"
            prepend-inner-icon="mdi-cash"
            variant="outlined"
            class="mb-2"
            required
        />
        <v-text-field
            v-model="phoneNumber"
            label="Phone Number"
            placeholder="+254712345678 / 071234568"
            prepend-inner-icon="mdi-phone"
            variant="outlined"
            class="mb-2"
            required
        />
        <v-select
            v-model="paymentMethod"
            :items="['MPESA', 'CARD']"
            label="Payment Method"
            prepend-inner-icon="mdi-credit-card"
            variant="outlined"
            class="mb-2"
            required
        />
        <v-btn type="submit" color="primary" block size="large" :loading="loading">
          Send Payment
        </v-btn>
      </v-form>
    </v-card-text>
  </v-card>
</template>

<script setup>
import { ref } from 'vue'
import api from '@/services/api'

const emit = defineEmits(['payment-completed'])

const amount = ref('')
const phoneNumber = ref('')
const paymentMethod = ref('MPESA')
const loading = ref(false)
const resultMessage = ref('')
const resultType = ref('success')

async function submitPayment() {
  resultMessage.value = ''
  loading.value = true
  try {
    const response = await api.post('/payments', {
      amount: parseFloat(amount.value),
      phoneNumber: phoneNumber.value,
      paymentMethod: paymentMethod.value,
    })

    const payment = response.data.data
    if (payment.status === 'SUCCESS') {
      resultType.value = 'success'
      resultMessage.value = `Payment successful! Reference: ${payment.reference}`
    } else {
      resultType.value = 'error'
      resultMessage.value = `Payment failed: ${payment.failureReason}`
    }

    amount.value = ''
    phoneNumber.value = ''
    emit('payment-completed')
  } catch (err) {
    resultType.value = 'error'
    resultMessage.value = err.response?.data?.message || 'Something went wrong.'
  } finally {
    loading.value = false
  }
}
</script>