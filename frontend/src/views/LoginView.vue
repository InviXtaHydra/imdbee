<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AuthLayout from './AuthLayout.vue'
import { useAuthStore } from '../stores/auth'
import { parseError } from '../services/api'
import { safeRedirect } from '../utils/redirect'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const form = ref({ login: '', password: '' })
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  loading.value = true
  try {
    await auth.login(form.value)
    router.replace(safeRedirect(route.query.redirect))
  } catch (e) {
    error.value = parseError(e).message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthLayout title="Inloggen">
    <p v-if="route.query.expired" class="mb-5 rounded bg-[#e87c03] px-4 py-3 text-sm text-black">
      Je sessie is verlopen. Log opnieuw in om verder te gaan.
    </p>
    <p v-if="error" class="mb-5 rounded bg-[#e87c03] px-4 py-3 text-sm text-black" role="alert">{{ error }}</p>

    <form class="space-y-4" novalidate @submit.prevent="submit">
      <div>
        <label for="login" class="mb-1.5 block text-sm text-smoke">Gebruikersnaam of e-mailadres</label>
        <input id="login" v-model="form.login" class="field" autocomplete="username" required autofocus />
      </div>
      <div>
        <label for="password" class="mb-1.5 block text-sm text-smoke">Wachtwoord</label>
        <input id="password" v-model="form.password" type="password" class="field" autocomplete="current-password" required />
      </div>
      <button type="submit" class="btn btn-primary mt-4 w-full !py-3" :disabled="loading || !form.login || !form.password">
        {{ loading ? 'Bezig met inloggen…' : 'Inloggen' }}
      </button>
    </form>

    <p class="mt-10 text-smoke">
      Nieuw bij IMDBee?
      <RouterLink :to="{ name: 'register', query: route.query.redirect ? { redirect: route.query.redirect } : {} }" class="link font-semibold">
        Maak een account
      </RouterLink>
    </p>
  </AuthLayout>
</template>
