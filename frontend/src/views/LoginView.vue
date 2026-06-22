<template>
  <v-container class="fill-height" fluid>
    <v-row justify="center" align="center">
      <v-col cols="12" sm="8" md="4">
        <v-card elevation="8" rounded="lg">
          <v-card-title class="text-center pt-6">
            <span class="text-h5 font-weight-bold">MiniPay</span>
          </v-card-title>
          <v-card-subtitle class="text-center pb-4">Sign in to your account</v-card-subtitle>

          <v-card-text>
            <v-alert v-if="errorMessage" type="error" variant="tonal" class="mb-4" closable>
              {{ errorMessage }}
            </v-alert>

            <v-form @submit.prevent="handleLogin">
              <v-text-field
                  v-model="username"
                  label="Username"
                  prepend-inner-icon="mdi-account"
                  variant="outlined"
                  class="mb-2"
                  required
              />
              <v-text-field
                  v-model="password"
                  label="Password"
                  type="password"
                  prepend-inner-icon="mdi-lock"
                  variant="outlined"
                  class="mb-2"
                  required
              />
              <v-btn
                  type="submit"
                  color="primary"
                  block
                  size="large"
                  :loading="loading"
              >
                Sign In
              </v-btn>
            </v-form>
          </v-card-text>

          <v-card-actions class="justify-center pb-6">
            <span class="text-body-2">Don't have an account?</span>
            <v-btn variant="text" color="primary" to="/register">Register</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const username = ref('')
const password = ref('')
const loading = ref(false)
const errorMessage = ref('')

const router = useRouter()
const authStore = useAuthStore()

async function handleLogin() {
  errorMessage.value = ''
  loading.value = true
  try {
    await authStore.login(username.value, password.value)
    router.push('/')
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Login failed. Please try again.'
  } finally {
    loading.value = false
  }
}
</script>