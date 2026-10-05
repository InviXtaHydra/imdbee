<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Navbar from './components/Navbar.vue'

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
  <footer v-if="!bare" class="page mt-16 border-t border-ash bg-bar py-10 text-sm text-smoke">
    <p>IMDBee, een filmcatalogus met reviews. Filmdata en afbeeldingen via TMDB.</p>
    <p class="mt-1">Dit product gebruikt de TMDB API maar is niet goedgekeurd of gecertificeerd door TMDB.</p>
  </footer>
</template>
