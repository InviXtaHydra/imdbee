<script setup>
import { computed, onMounted, ref } from 'vue'
import ReviewList from '../components/ReviewList.vue'
import ReviewForm from '../components/ReviewForm.vue'
import ErrorState from '../components/ErrorState.vue'
import reviewService from '../services/reviewService'
import { parseError } from '../services/api'
import { useAuthStore } from '../stores/auth'
import { longDate } from '../utils/format'

const auth = useAuthStore()
const reviews = ref([])
const loading = ref(true)
const error = ref('')
const editingId = ref(null)

const average = computed(() =>
  reviews.value.length ? (reviews.value.reduce((s, r) => s + r.rating, 0) / reviews.value.length).toFixed(1) : '–',
)
const editingReview = computed(() => reviews.value.find((r) => r.id === editingId.value))

async function load() {
  loading.value = true
  error.value = ''
  try {
    reviews.value = await reviewService.mine()
  } catch (e) {
    error.value = parseError(e).message
  } finally {
    loading.value = false
  }
}
onMounted(load)

function onSaved(saved) {
  const i = reviews.value.findIndex((r) => r.id === saved.id)
  if (i >= 0) reviews.value[i] = saved
  editingId.value = null
}

async function onDelete(review) {
  if (!window.confirm(`Je review van “${review.movieTitle}” verwijderen?`)) return
  try {
    await reviewService.remove(review.id)
    reviews.value = reviews.value.filter((r) => r.id !== review.id)
  } catch (e) {
    window.alert(parseError(e).message)
  }
}
</script>

<template>
  <div class="page max-w-5xl pt-24 md:pt-28">
    <section class="flex flex-col gap-6 border-b border-ash pb-8 sm:flex-row sm:items-center">
      <span class="grid h-20 w-20 shrink-0 place-items-center rounded-md bg-honey text-4xl font-bold text-black uppercase" aria-hidden="true">
        {{ auth.user.username.charAt(0) }}
      </span>
      <div class="flex-1">
        <h1 class="display text-4xl md:text-5xl">{{ auth.user.username }}</h1>
        <p class="mt-1 text-smoke">{{ auth.user.email }} · lid sinds {{ longDate(auth.user.createdAt) }}</p>
      </div>
      <dl class="flex gap-8">
        <div>
          <dt class="text-sm text-smoke">Reviews</dt>
          <dd class="text-3xl font-bold">{{ reviews.length }}</dd>
        </div>
        <div>
          <dt class="text-sm text-smoke">Gem. score</dt>
          <dd class="text-3xl font-bold">{{ average }}</dd>
        </div>
      </dl>
    </section>

    <section class="mt-8" aria-labelledby="my-reviews">
      <h2 id="my-reviews" class="section-title text-2xl">Mijn reviews</h2>

      <div v-if="loading" class="mt-6 space-y-4" aria-busy="true">
        <div v-for="n in 3" :key="n" class="h-28 animate-pulse rounded bg-coal/60" />
      </div>
      <ErrorState v-else-if="error" class="mt-6" :message="error" @retry="load" />
      <div v-else-if="!reviews.length" class="mt-6 rounded-lg border border-dashed border-ash p-8 text-center">
        <p class="font-semibold">Je hebt nog geen reviews geschreven</p>
        <p class="mt-1 text-smoke">Open een film en geef hem 1 tot 5 sterren.</p>
        <RouterLink :to="{ name: 'browse' }" class="btn btn-primary mt-5">Films zoeken</RouterLink>
      </div>
      <template v-else>
        <div v-if="editingReview" class="mt-6">
          <p class="mb-3 text-smoke">Je bewerkt je review van <strong class="text-snow">{{ editingReview.movieTitle }}</strong></p>
          <ReviewForm :movie-id="editingReview.movieId" :review="editingReview" @saved="onSaved" @cancel="editingId = null" />
        </div>
        <ReviewList
          class="mt-2"
          :reviews="reviews"
          :current-user-id="auth.user.id"
          show-movie
          @edit="(r) => (editingId = r.id)"
          @delete="onDelete"
        />
      </template>
    </section>
  </div>
</template>
