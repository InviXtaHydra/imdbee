import axios from 'axios'

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

api.interceptors.response.use(
  (res) => res,
  (error) => {
    const isAuthCall = error.config?.url?.startsWith('/auth/login') || error.config?.url?.startsWith('/auth/register')
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
  if (error?.code === 'ECONNABORTED' || !error?.response) {
    return { message: 'Kan de server niet bereiken. Draait de backend op poort 8080?', fields: {} }
  }
  const status = error.response.status
  if (status === 403) {
    return { message: 'De server weigert dit verzoek (403). Herstart de backend en probeer opnieuw.', fields: {} }
  }
  return { message: `Er ging iets mis (fout ${status}). Probeer het opnieuw.`, fields: {} }
}

export default api
