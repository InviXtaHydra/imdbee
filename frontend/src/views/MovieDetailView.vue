<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Clock, Play, Star, Users } from 'lucide-vue-next'
import PosterImage from '../components/PosterImage.vue'
import ReviewForm from '../components/ReviewForm.vue'
import ReviewList from '../components/ReviewList.vue'
import ErrorState from '../components/ErrorState.vue'
import TrailerModal from '../components/TrailerModal.vue'
import movieService from '../services/movieService'
import reviewService from '../services/reviewService'
import { parseError } from '../services/api'
import { useAuthStore } from '../stores/auth'
import { longDate, runtime, score, year } from '../utils/format'

const props = defineProps({ id: { type: String, required: true } })
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const movie = ref(null)
const reviews = ref([])
const loading = ref(true)
const error = ref('')
const editing = ref(false)

// Trailer opens via the button, or via ?trailer=1 (used by the home hero)
const trailerOpen = computed(() => route.query.trailer === '1' && !!movie.value?.trailerKey)
function openTrailer() {
  router.replace({ query: { ...route.query, trailer: '1' } })
}
function closeTrailer() {
  const { trailer, ...rest } = route.query
  router.replace({ query: rest })
}

const movieId = computed(() => Number(props.id))
const myReview = computed(() => reviews.value.find((r) => r.userId === auth.user?.id))
const otherReviews = computed(() => reviews.value.filter((r) => r !== myReview.value))

async function load() {
  loading.value = true
  error.value = ''
  editing.value = false
  try {
    const [m, r] = await Promise.all([movieService.details(movieId.value), reviewService.forMovie(movieId.value)])
    movie.value = m
    reviews.value = r
    document.title = `${m.title} · IMDBee`
  } catch (e) {
    error.value = parseError(e).message
  } finally {
    loading.value = false
  }
  // Jump to reviews when linked with #reviews
  if (route.hash === '#reviews') {
    await nextTick()
    document.getElementById('reviews')?.scrollIntoView({ behavior: 'smooth' })
  }
}
watch(movieId, load, { immediate: true })

// Keep the community score in sync after create/update/delete
function recalcCommunity() {
  const n = reviews.value.length
  const avg = n ? reviews.value.reduce((s, r) => s + r.rating, 0) / n : null
  movie.value.community = { average: avg == null ? null : Math.round(avg * 10) / 10, count: n }
}

function onSaved(saved) {
  const i = reviews.value.findIndex((r) => r.id === saved.id)
  if (i >= 0) reviews.value[i] = saved
  else reviews.value.unshift(saved)
  editing.value = false
  recalcCommunity()
}

async function onDelete(review) {
  if (!window.confirm('Weet je zeker dat je je review wilt verwijderen?')) return
  try {
    await reviewService.remove(review.id)
    reviews.value = reviews.value.filter((r) => r.id !== review.id)
    recalcCommunity()
  } catch (e) {
    window.alert(parseError(e).message)
  }
}
</script>

<template>
  <div>
    <div v-if="loading" class="page pt-28" aria-busy="true">
      <div class="h-[50vh] animate-pulse rounded-lg bg-coal/60" />
    </div>

    <div v-else-if="error" class="page pt-28">
      <ErrorState :message="error" @retry="load" />
      <RouterLink to="/" class="mt-6 inline-flex items-center gap-2 text-smoke hover:text-snow">
        <ArrowLeft :size="16" /> Terug naar home
      </RouterLink>
    </div>

    <article v-else-if="movie">
      <!-- Backdrop header -->
      <div class="relative h-[42vh] min-h-64 overflow-hidden md:h-[60vh]">
        <PosterImage :movie="movie" variant="backdrop" eager />
        <div class="absolute inset-0 bg-gradient-to-t from-night via-night/50 to-black/30" aria-hidden="true" />
      </div>

      <div class="page relative z-10 -mt-40 grid gap-8 md:-mt-64 md:grid-cols-[minmax(0,16rem)_1fr] lg:gap-12">
        <div class="mx-auto aspect-[2/3] w-44 overflow-hidden rounded-lg shadow-2xl shadow-black md:mx-0 md:w-full">
          <PosterImage :movie="movie" eager />
        </div>

        <div class="md:pt-24">
          <h1 class="display text-4xl sm:text-5xl lg:text-6xl">
            {{ movie.title }}
            <span v-if="movie.releaseDate" class="font-normal text-smoke [font-variation-settings:'wdth'_70]">({{ year(movie.releaseDate) }})</span>
          </h1>
          <p v-if="movie.tagline" class="mt-3 text-lg text-smoke italic">{{ movie.tagline }}</p>

          <dl class="mt-5 flex flex-wrap items-center gap-x-6 gap-y-3 text-sm">
            <div v-if="movie.voteAverage" class="flex items-center gap-1.5">
              <dt class="sr-only">TMDB-score</dt>
              <Star :size="16" class="fill-honey text-honey" aria-hidden="true" />
              <dd><strong>{{ score(movie.voteAverage) }}</strong><span class="text-smoke">/10 TMDB</span></dd>
            </div>
            <div class="flex items-center gap-1.5">
              <dt class="sr-only">Score van IMDBee-gebruikers</dt>
              <Users :size="16" class="text-honey" aria-hidden="true" />
              <dd v-if="movie.community.count">
                <strong>{{ movie.community.average }}</strong><span class="text-smoke">/5 uit {{ movie.community.count }} {{ movie.community.count === 1 ? 'review' : 'reviews' }}</span>
              </dd>
              <dd v-else class="text-smoke">Nog geen reviews</dd>
            </div>
            <div v-if="movie.runtime" class="flex items-center gap-1.5">
              <dt class="sr-only">Speelduur</dt>
              <Clock :size="16" class="text-smoke" aria-hidden="true" />
              <dd>{{ runtime(movie.runtime) }}</dd>
            </div>
            <div v-if="movie.releaseDate">
              <dt class="sr-only">Releasedatum</dt>
              <dd class="text-smoke">{{ longDate(movie.releaseDate) }}</dd>
            </div>
          </dl>

          <button v-if="movie.trailerKey" type="button" class="btn btn-light mt-6" @click="openTrailer">
            <Play :size="20" class="fill-current" aria-hidden="true" /> Trailer afspelen
          </button>

          <ul v-if="movie.genres.length" class="mt-6 flex flex-wrap gap-2" aria-label="Genres">
            <li v-for="g in movie.genres" :key="g.id">
              <RouterLink :to="{ name: 'browse', query: { genre: g.id } }" class="block rounded-full border border-ash px-3 py-1 text-sm hover:border-snow">
                {{ g.name }}
              </RouterLink>
            </li>
          </ul>

          <h2 class="section-title mt-8 text-lg">Verhaal</h2>
          <p class="mt-2 max-w-3xl text-snow/90">{{ movie.overview || 'Er is nog geen beschrijving voor deze film.' }}</p>
        </div>
      </div>

      <!-- Cast -->
      <section v-if="movie.cast.length" class="mt-12" aria-labelledby="cast-title">
        <h2 id="cast-title" class="page section-title text-lg">Cast</h2>
        <ul class="no-scrollbar page mt-4 flex gap-4 overflow-x-auto pb-2">
          <li v-for="c in movie.cast" :key="c.name + c.character" class="w-24 shrink-0 text-center sm:w-28">
            <div class="mx-auto aspect-square w-full overflow-hidden rounded-full bg-coal">
              <img v-if="c.profileUrl" :src="c.profileUrl" :alt="c.name" loading="lazy" class="h-full w-full object-cover" />
              <span v-else class="grid h-full w-full place-items-center text-2xl font-bold text-smoke" aria-hidden="true">
                {{ c.name.split(' ').map((p) => p[0]).slice(0, 2).join('') }}
              </span>
            </div>
            <p class="mt-2 text-sm leading-tight font-semibold">{{ c.name }}</p>
            <p class="mt-0.5 text-xs leading-tight text-smoke">{{ c.character }}</p>
          </li>
        </ul>
      </section>

      <!-- Reviews -->
      <section id="reviews" class="page mt-12 max-w-4xl scroll-mt-24" aria-labelledby="reviews-title">
        <h2 id="reviews-title" class="section-title text-2xl">Reviews</h2>

        <div class="mt-6">
          <template v-if="auth.isLoggedIn">
            <ReviewForm v-if="!myReview" :movie-id="movie.id" @saved="onSaved" />
            <ReviewForm v-else-if="editing" :movie-id="movie.id" :review="myReview" @saved="onSaved" @cancel="editing = false" />
            <div v-else class="rounded-lg border border-ash bg-coal/60 px-4 sm:px-6">
              <ReviewList :reviews="[myReview]" :current-user-id="auth.user.id" @edit="editing = true" @delete="onDelete" />
            </div>
          </template>
          <div v-else class="flex flex-col gap-3 rounded-lg border border-ash bg-coal/60 p-5 sm:flex-row sm:items-center sm:justify-between">
            <p>Log in om deze film te beoordelen.</p>
            <RouterLink :to="{ name: 'login', query: { redirect: route.fullPath + '#reviews' } }" class="btn btn-primary">Inloggen</RouterLink>
          </div>
        </div>

        <h3 class="mt-10 text-sm font-semibold text-smoke">
          {{ otherReviews.length ? `Reviews van anderen (${otherReviews.length})` : 'Nog geen reviews van anderen' }}
        </h3>
        <ReviewList :reviews="otherReviews" :current-user-id="auth.user?.id" />
      </section>
    </article>

    <TrailerModal :open="trailerOpen" :video-key="movie?.trailerKey" :title="movie?.title" @close="closeTrailer" />
  </div>
</template>
