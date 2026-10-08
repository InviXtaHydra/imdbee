<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Navbar from './components/Navbar.vue'
import { serverWaking } from './services/serverStatus'

const route = useRoute()
const bare = computed(() => route.meta.bare)
</script>

<template>
  <a href="#main" class="sr-only z-[60] rounded bg-snow px-4 py-2 text-black focus:not-sr-only focus:fixed focus:top-3 focus:left-3">
    Naar inhoud
  </a>
  <Navbar v-if="!bare" />
  <main id="main">
    <RouterView />
  </main>
  <!-- Shown while the free backend wakes up, so a slow first visit doesn't look broken -->
  <Transition
    enter-from-class="translate-y-4 opacity-0"
    enter-active-class="transition duration-300"
    leave-to-class="translate-y-4 opacity-0"
    leave-active-class="transition duration-300"
  >
    <div
      v-if="serverWaking"
      role="status"
      class="fixed inset-x-4 bottom-4 z-50 mx-auto flex max-w-md items-start gap-3 rounded-lg border border-ash bg-coal/95 p-4 text-sm shadow-2xl backdrop-blur sm:inset-x-auto sm:right-6 sm:bottom-6"
    >
      <span class="mt-0.5 h-4 w-4 shrink-0 animate-spin rounded-full border-2 border-smoke border-t-transparent motion-reduce:animate-none" aria-hidden="true" />
      <p>
        <span class="font-semibold">De server wordt wakker…</span>
        <span class="block text-smoke">De gratis hosting slaapt als er even niemand is. Dit duurt hooguit een minuut.</span>
      </p>
    </div>
  </Transition>
  <footer v-if="!bare" class="page mt-16 border-t border-ash bg-bar py-10 text-sm text-smoke">
    <p>IMDBee, een filmcatalogus met reviews. Filmdata en afbeeldingen via TMDB.</p>
    <p class="mt-1">Dit product gebruikt de TMDB API maar is niet goedgekeurd of gecertificeerd door TMDB.</p>
  </footer>
</template>
