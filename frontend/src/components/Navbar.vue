<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChevronDown, LogOut, Menu, Search, User, X } from 'lucide-vue-next'
import AppLogo from './AppLogo.vue'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const scrolled = ref(false)
const mobileOpen = ref(false)
const accountOpen = ref(false)
const searchOpen = ref(false)
const searchInput = ref(null)
const query = ref(route.query.query ?? '')

const links = [
  { to: { name: 'home' }, label: 'Home', name: 'home' },
  { to: { name: 'browse' }, label: 'Films zoeken', name: 'browse' },
]

function onScroll() {
  scrolled.value = window.scrollY > 10
}
function onDocClick(e) {
  if (!e.target.closest('[data-account-menu]')) accountOpen.value = false
}
onMounted(() => {
  onScroll()
  window.addEventListener('scroll', onScroll, { passive: true })
  document.addEventListener('click', onDocClick)
})
onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
  document.removeEventListener('click', onDocClick)
})

// Close menus on navigation
watch(() => route.fullPath, () => {
  mobileOpen.value = false
  accountOpen.value = false
})
watch(() => route.query.query, (q) => (query.value = q ?? ''))
watch(mobileOpen, (open) => (document.body.style.overflow = open ? 'hidden' : ''))

async function openSearch() {
  searchOpen.value = true
  await nextTick()
  searchInput.value?.focus()
}

// Typing searches live: debounce, then show results on the browse page
let timer
function onSearchInput() {
  clearTimeout(timer)
  timer = setTimeout(submitSearch, 350)
}
function submitSearch() {
  clearTimeout(timer)
  const q = query.value.trim()
  const target = { name: 'browse', query: { ...(route.name === 'browse' ? route.query : {}), query: q || undefined } }
  route.name === 'browse' ? router.replace(target) : q && router.push(target)
}
function onSearchBlur() {
  if (!query.value) searchOpen.value = false
}

function logout() {
  auth.logout()
  router.push({ name: 'home' })
}
</script>

<template>
  <header
    class="fixed inset-x-0 top-0 z-50 bg-bar transition-shadow duration-300"
    :class="(scrolled || mobileOpen) && 'shadow-lg shadow-black/60'"
  >
    <nav class="page flex h-16 items-center gap-6 md:h-[4.5rem]" aria-label="Hoofdmenu">
      <AppLogo />

      <ul class="hidden items-center gap-5 text-sm md:flex">
        <li v-for="l in links" :key="l.name">
          <RouterLink
            :to="l.to"
            class="transition-colors hover:text-smoke"
            :class="route.name === l.name ? 'font-bold text-honey' : 'text-snow/80'"
            :aria-current="route.name === l.name ? 'page' : undefined"
          >
            {{ l.label }}
          </RouterLink>
        </li>
      </ul>

      <div class="ml-auto flex items-center gap-3 md:gap-5">
        <!-- Search: icon that expands into an input -->
        <form role="search" class="flex items-center" @submit.prevent="submitSearch">
          <div
            class="flex items-center overflow-hidden border transition-all duration-300"
            :class="searchOpen ? 'w-44 border-snow/80 bg-black/80 sm:w-64' : 'w-9 border-transparent'"
          >
            <button type="button" class="grid h-9 w-9 shrink-0 place-items-center" aria-label="Zoeken" @click="openSearch">
              <Search :size="20" />
            </button>
            <input
              ref="searchInput"
              v-model="query"
              type="search"
              placeholder="Titels zoeken"
              aria-label="Zoek een film op titel"
              class="w-full bg-transparent pr-2 text-sm outline-none placeholder:text-smoke"
              :tabindex="searchOpen ? 0 : -1"
              @input="onSearchInput"
              @blur="onSearchBlur"
              @keydown.esc="(query = ''), (searchOpen = false)"
            />
          </div>
        </form>

        <!-- Account (desktop) -->
        <div v-if="auth.isLoggedIn" class="relative hidden md:block" data-account-menu>
          <button
            type="button"
            class="flex items-center gap-2"
            :aria-expanded="accountOpen"
            aria-haspopup="menu"
            @click="accountOpen = !accountOpen"
          >
            <span class="grid h-8 w-8 place-items-center rounded bg-honey text-sm font-bold text-black uppercase">
              {{ auth.user.username.charAt(0) }}
            </span>
            <ChevronDown :size="16" class="transition-transform" :class="accountOpen && 'rotate-180'" aria-hidden="true" />
            <span class="sr-only">Accountmenu</span>
          </button>
          <Transition
            enter-from-class="opacity-0 -translate-y-1"
            leave-to-class="opacity-0 -translate-y-1"
            enter-active-class="transition duration-150"
            leave-active-class="transition duration-100"
          >
            <div v-if="accountOpen" role="menu" class="absolute right-0 mt-3 w-56 rounded-md border border-ash bg-black/95 py-2 text-sm shadow-xl">
              <p class="truncate px-4 pt-1 pb-3 text-smoke">Ingelogd als <strong class="text-snow">{{ auth.user.username }}</strong></p>
              <RouterLink :to="{ name: 'profile' }" role="menuitem" class="flex items-center gap-3 px-4 py-2 hover:bg-coal">
                <User :size="16" aria-hidden="true" /> Profiel & mijn reviews
              </RouterLink>
              <button type="button" role="menuitem" class="flex w-full items-center gap-3 border-t border-ash px-4 py-2 pt-3 text-left hover:bg-coal" @click="logout">
                <LogOut :size="16" aria-hidden="true" /> Uitloggen
              </button>
            </div>
          </Transition>
        </div>
        <RouterLink v-else :to="{ name: 'login', query: { redirect: route.fullPath } }" class="btn btn-primary hidden !py-1.5 text-sm md:inline-flex">
          Inloggen
        </RouterLink>

        <!-- Mobile menu toggle -->
        <button
          type="button"
          class="grid h-9 w-9 place-items-center md:hidden"
          :aria-expanded="mobileOpen"
          aria-controls="mobile-menu"
          :aria-label="mobileOpen ? 'Menu sluiten' : 'Menu openen'"
          @click="mobileOpen = !mobileOpen"
        >
          <X v-if="mobileOpen" :size="24" />
          <Menu v-else :size="24" />
        </button>
      </div>
    </nav>

    <!-- Mobile menu -->
    <Transition
      enter-from-class="opacity-0 -translate-y-2"
      leave-to-class="opacity-0 -translate-y-2"
      enter-active-class="transition duration-200"
      leave-active-class="transition duration-150"
    >
      <div v-if="mobileOpen" id="mobile-menu" class="h-[calc(100dvh-4rem)] overflow-y-auto border-t border-ash bg-bar md:hidden">
        <ul class="page flex flex-col py-4">
          <li v-for="l in links" :key="l.name">
            <RouterLink :to="l.to" class="block border-b border-ash py-4 text-lg font-semibold" :class="route.name === l.name ? 'text-honey' : ''">
              {{ l.label }}
            </RouterLink>
          </li>
          <template v-if="auth.isLoggedIn">
            <li>
              <RouterLink :to="{ name: 'profile' }" class="block border-b border-ash py-4 text-lg font-semibold" :class="route.name === 'profile' ? 'text-honey' : ''">
                Profiel & mijn reviews
              </RouterLink>
            </li>
            <li class="mt-6 flex items-center justify-between">
              <span class="text-smoke">Ingelogd als <strong class="text-snow">{{ auth.user.username }}</strong></span>
              <button type="button" class="btn btn-ghost !py-2 text-sm" @click="logout"><LogOut :size="16" /> Uitloggen</button>
            </li>
          </template>
          <li v-else class="mt-6 grid grid-cols-2 gap-3">
            <RouterLink :to="{ name: 'login' }" class="btn btn-primary">Inloggen</RouterLink>
            <RouterLink :to="{ name: 'register' }" class="btn btn-ghost">Registreren</RouterLink>
          </li>
        </ul>
      </div>
    </Transition>
  </header>
</template>
