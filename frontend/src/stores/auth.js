import { defineStore } from 'pinia'
import api from '@/services/api'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: localStorage.getItem('minipay_token') || null,
        username: localStorage.getItem('minipay_username') || null,
    }),

    getters: {
        isAuthenticated: (state) => !!state.token,
    },

    actions: {
        async login(username, password) {
            const response = await api.post('/auth/login', { username, password })
            const { token, username: returnedUsername } = response.data.data
            this.setSession(token, returnedUsername)
        },

        async register(username, email, password) {
            const response = await api.post('/auth/register', { username, email, password })
            const { token, username: returnedUsername } = response.data.data
            this.setSession(token, returnedUsername)
        },

        setSession(token, username) {
            this.token = token
            this.username = username
            localStorage.setItem('minipay_token', token)
            localStorage.setItem('minipay_username', username)
        },

        logout() {
            this.token = null
            this.username = null
            localStorage.removeItem('minipay_token')
            localStorage.removeItem('minipay_username')
        },
    },
})