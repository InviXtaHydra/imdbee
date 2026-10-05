<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { SlidersHorizontal } from 'lucide-vue-next'
import MovieGrid from '../components/MovieGrid.vue'
import ErrorState from '../components/ErrorState.vue'
import movieService from '../services/movieService'
import { parseError } from '../services/api'
import { useMovieStore } from '../stores/movies'

const route = useRoute()
const router = useRouter()
const store = useMovieStore()

// Filters live in the URL, so results can be shared and survive a refresh
const query = computed(() => route.query.query ?? '')
const genre = computed(() => (route.query.genre ? Number(route.query.genre) : ''))
const minRating = computed(() => (route.query.minRating ? Number(route.query.minRating) : ''))

const movies = ref([])
const page = ref(1)
const totalPages = ref(1)
const totalResults = ref(0)
const loading = ref(false)
const error = ref('')

const ratingOptions = [
  { value: '', label: 'Alle scores' },
  { value: 6, label: '6+ op TMDB' },
  { value: 7, label: '7+ op TMDB' },
  { value: 8, label: '8+ op TMDB' },
]

const heading = computed(() => {
  if (query.value) return `Resultaten voor “${query.value}”`
  if (genre.value) return store.genreName(genre.value) ?? 'Films'
  return 'Alle films'
})

function setFilter(key, value) {
  router.replace({ query: { ...route.query, [key]: value === '' ? undefined : value } })
}

let requestId = 0
async function load(reset = true) {
  const id = ++requestId
  loading.value = true
  error.value = ''
  if (reset) {
    page.value = 1
    movies.value = []
  }
  try {
    const res = await movieService.search({ query: query.value, genre: genre.value, minRating: minRating.value, page: page.value })
    if (id !== requestId) return // a newer search started meanwhile
    // Skip duplicates that can appear between TMDB pages
    const seen = new Set(movies.value.map((m) => m.id))
    movies.value = [...movies.value, ...res.results.filter((m) => !seen.has(m.id))]
    totalPages.value = res.totalPages
    totalResults.value = res.totalResults
  } catch (e) {
    if (id === requestId) error.value = parseError(e).message
  } finally {
    if (id === requestId) loading.value = false
  }
}

function loadMore() {
  page.value++
  load(false)
}

watch(() => [route.query.query, route.query.genre, route.query.minRating], () => load())
onMounted(() => {
  store.loadGenres().catch(() => {})
  load()
})
</script>

<template>
  <div class="page pt-24 md:pt-28">
    <div class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
      <h1 class="display text-4xl md:text-5xl">{{ heading }}</h1>

      <div class="flex flex-wrap items-center gap-2">
        <SlidersHorizontal :size="18" class="text-smoke" aria-hidden="true" />
        <label class="sr-only" for="filter-genre">Genre</label>
        <select id="filter-genre" :value="genre" class="field !w-auto !py-2 text-sm" @change="setFilter('genre', $event.target.value)">
          <option value="">Alle genres</option>
          <option v-for="g in store.genres" :key="g.id" :value="g.id">{{ g.name }}</option>
        </select>
        <label class="sr-only" for="filter-rating">Minimale score</label>
        <select id="filter-rating" :value="minRating" class="field !w-auto !py-2 text-sm" @change="setFilter('minRating', $event.target.value)">
          <option v-for="o in ratingOptions" :key="o.label" :value="o.value">{{ o.label }}</option>
        </select>
      </div>
    </div>

    <!-- Mobile search field (the navbar search is compact on small screens) -->
    <form role="search" class="mt-4 md:hidden" @submit.prevent>
      <label for="browse-search" class="sr-only">Zoek op titel</label>
      <input
        id="browse-search"
        type="search"
        class="field"
        placeholder="Zoek op titel"
        :value="query"
        @input="setFilter('query', $event.target.value.trim())"
      />
    </form>

    <p class="mt-3 text-sm text-smoke" aria-live="polite">
      <template v-if="(!loading || movies.length) && totalResults >= 0">{{ totalResults }} {{ totalResults === 1 ? 'film' : 'films' }} gevonden</template>
    </p>

    <div class="mt-6">
      <ErrorState v-if="error" :message="error" @retry="load()" />
      <template v-else>
        <MovieGrid :movies="movies" :loading="loading" />
        <div v-if="!loading && !movies.length" class="py-16 text-center">
          <p class="text-lg font-semibold">Geen films gevonden</p>
          <p class="mt-1 text-smoke">Probeer een andere titel, of zet de filters op ‘Alle genres’ en ‘Alle scores’.</p>
        </div>
        <div v-if="!loading && page < totalPages" class="mt-8 flex justify-center">
          <button type="button" class="btn btn-ghost" @click="loadMore">Meer films laden</button>
        </div>
      </template>
    </div>
  </div>
</template>
