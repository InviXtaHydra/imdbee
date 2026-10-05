import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [vue(), tailwindcss()],
  server: {
    // 5174 so IMDBee can run next to other Vite projects on the default 5173
    port: 5174,
    // Forward API calls to Spring Boot in development (no CORS needed)
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
