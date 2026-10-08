import axios from 'axios'
import { serverWaking } from './serverStatus'

const TOKEN_KEY = 'imdbee.token'

export const tokenStorage = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (t) => localStorage.setItem(TOKEN_KEY, t),
  clear: () => localStorage.removeItem(TOKEN_KEY),
}

// In dev, Vite proxies /api to Spring Boot. Set VITE_API_URL for other setups.
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '/api',
  timeout: 15000,
})

api.interceptors.request.use((config) => {
  const token = tokenStorage.get()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Called when the server says the token is no longer valid; set by the auth store
let onUnauthorized = () => {}
export function setUnauthorizedHandler(fn) {
  onUnauthorized = fn
}

// A sleeping backend needs up to a minute to start. Until then requests time out or the proxy
// answers 502/503/504; keep retrying for a while instead of showing an error straight away.
const WAKE_BUDGET_MS = 90_000
const RETRY_DELAY_MS = 3_000
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

function isWakingError(error) {
  const status = error.response?.status
  if (status === 502 || status === 503 || status === 504) return true // the request never reached the app
  // No answer at all: only retry reads, a write might have arrived after all
  return !error.response && (error.config?.method ?? 'get').toLowerCase() === 'get'
}

api.interceptors.response.use(
  (res) => {
    serverWaking.value = false
    return res
  },
  async (error) => {
    const config = error.config
    if (config && isWakingError(error)) {
      config.wakeStartedAt ??= Date.now()
      if (Date.now() - config.wakeStartedAt < WAKE_BUDGET_MS) {
        serverWaking.value = true
        await sleep(RETRY_DELAY_MS)
        return api(config)
      }
    }
    serverWaking.value = false
    const isAuthCall = config?.url?.startsWith('/auth/login') || config?.url?.startsWith('/auth/register')
    if (error.response?.status === 401 && !isAuthCall && tokenStorage.get()) {
      onUnauthorized()
    }
    return Promise.reject(error)
  },
)

/** Turns an axios error into a message and optional per-field errors for the UI. */
export function parseError(error) {
  const data = error?.response?.data
  if (data?.message) return { message: data.message, fields: data.fieldErrors ?? {} }
  if (error?.code === 'ECONNABORTED' || !error?.response || [502, 503, 504].includes(error.response.status)) {
    const message = import.meta.env.DEV
      ? 'Kan de server niet bereiken. Draait de backend op poort 8080?'
      : 'De server reageert niet. Probeer het over een minuut opnieuw.'
    return { message, fields: {} }
  }
  const status = error.response.status
  if (status === 403) {
    return { message: 'De server weigert dit verzoek (403). Herstart de backend en probeer opnieuw.', fields: {} }
  }
  return { message: `Er ging iets mis (fout ${status}). Probeer het opnieuw.`, fields: {} }
}

export default api
