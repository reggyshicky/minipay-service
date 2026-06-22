import axios from 'axios'

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:3271/api',
})

// Attach JWT token to every outgoing request automatically
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('minipay_token')
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

// Auto-redirect to login if token expires/invalid (401 response)
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('minipay_token')
            window.location.href = '/login'
        }
        return Promise.reject(error)
    }
)

export default api