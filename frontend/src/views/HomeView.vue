<script setup>
import { onMounted, ref } from 'vue'
import HeroBanner from '../components/HeroBanner.vue'
import MovieRow from '../components/MovieRow.vue'
import ErrorState from '../components/ErrorState.vue'
import { useMovieStore } from '../stores/movies'
import { parseError } from '../services/api'

const store = useMovieStore()
const loading = ref(!store.home)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    await store.loadHome()
  } catch (e) {
    error.value = parseError(e).message
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>

<template>
  <div>
    <!-- Loading skeleton -->
    <div v-if="loading" aria-busy="true" aria-label="Films laden">
      <div class="h-[78vh] min-h-[28rem] animate-pulse bg-coal/60 md:h-[88vh]" />
    </div>

    <div v-else-if="error" class="page pt-28">
      <ErrorState :message="error" @retry="load" />
    </div>

    <template v-else-if="store.home">
      <HeroBanner v-if="store.home.hero" :movie="store.home.hero" :show-trailer="!store.home.demoMode" />

      <div class="relative z-10 space-y-6 md:-mt-24 md:space-y-8">
        <p v-if="store.home.demoMode" class="page">
          <span class="inline-block rounded border border-honey/40 bg-honey/10 px-3 py-2 text-sm text-honey">
            Demomodus: een kleine ingebouwde catalogus zonder posters. Stel <code class="font-semibold">TMDB_API_KEY</code> in voor echte films en afbeeldingen.
          </span>
        </p>
        <MovieRow v-for="row in store.home.rows" :key="row.key" :title="row.title" :movies="row.movies" />
      </div>
    </template>
  </div>
</template>
