import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import authService from '../services/authService'
import { setUnauthorizedHandler, tokenStorage } from '../services/api'
import router from '../router'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(tokenStorage.get())
  const user = ref(null)
  const ready = ref(false)

  const isLoggedIn = computed(() => !!token.value && !!user.value)

  function setSession({ token: t, user: u }) {
    token.value = t
    user.value = u
    tokenStorage.set(t)
  }

  async function register(payload) {
    setSession(await authService.register(payload))
  }

  async function login(payload) {
    setSession(await authService.login(payload))
  }

  function logout({ expired = false } = {}) {
    token.value = null
    user.value = null
    tokenStorage.clear()
    const current = router.currentRoute.value
    if (current.meta.requiresAuth || expired) {
      router.push({ name: 'login', query: { redirect: current.fullPath, ...(expired && { expired: '1' }) } })
    }
  }

  /** On app start: restore the user from a stored token, if it is still valid. */
  async function init() {
    if (ready.value) return
    if (token.value) {
      try {
        user.value = await authService.me()
      } catch {
        token.value = null
        tokenStorage.clear()
      }
    }
    ready.value = true
  }

  setUnauthorizedHandler(() => logout({ expired: true }))

  return { token, user, ready, isLoggedIn, register, login, logout, init }
})
