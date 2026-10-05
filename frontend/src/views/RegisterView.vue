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

const form = ref({ username: '', email: '', password: '' })
const fieldErrors = ref({})
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  fieldErrors.value = {}
  loading.value = true
  try {
    await auth.register(form.value)
    router.replace(safeRedirect(route.query.redirect))
  } catch (e) {
    const { message, fields } = parseError(e)
    fieldErrors.value = fields
    if (!Object.keys(fields).length) error.value = message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthLayout title="Account maken">
    <p v-if="error" class="mb-5 rounded bg-[#e87c03] px-4 py-3 text-sm text-black" role="alert">{{ error }}</p>

    <form class="space-y-4" novalidate @submit.prevent="submit">
      <div>
        <label for="username" class="mb-1.5 block text-sm text-smoke">Gebruikersnaam</label>
        <input
          id="username" v-model="form.username" class="field" autocomplete="username" required autofocus
          :aria-invalid="!!fieldErrors.username" aria-describedby="username-hint"
        />
        <p id="username-hint" class="mt-1 text-xs" :class="fieldErrors.username ? 'text-[#e87c03]' : 'text-smoke'">
          {{ fieldErrors.username ?? 'Zichtbaar bij je reviews. 3 tot 40 tekens.' }}
        </p>
      </div>
      <div>
        <label for="email" class="mb-1.5 block text-sm text-smoke">E-mailadres</label>
        <input
          id="email" v-model="form.email" type="email" class="field" autocomplete="email" required
          :aria-invalid="!!fieldErrors.email" aria-describedby="email-error"
        />
        <p v-if="fieldErrors.email" id="email-error" class="mt-1 text-xs text-[#e87c03]">{{ fieldErrors.email }}</p>
      </div>
      <div>
        <label for="password" class="mb-1.5 block text-sm text-smoke">Wachtwoord</label>
        <input
          id="password" v-model="form.password" type="password" class="field" autocomplete="new-password" required
          :aria-invalid="!!fieldErrors.password" aria-describedby="password-hint"
        />
        <p id="password-hint" class="mt-1 text-xs" :class="fieldErrors.password ? 'text-[#e87c03]' : 'text-smoke'">
          {{ fieldErrors.password ?? 'Minstens 8 tekens.' }}
        </p>
      </div>
      <button type="submit" class="btn btn-primary mt-4 w-full !py-3" :disabled="loading">
        {{ loading ? 'Account maken…' : 'Account maken' }}
      </button>
    </form>

    <p class="mt-10 text-smoke">
      Heb je al een account?
      <RouterLink :to="{ name: 'login', query: route.query.redirect ? { redirect: route.query.redirect } : {} }" class="link font-semibold">
        Inloggen
      </RouterLink>
    </p>
  </AuthLayout>
</template>
