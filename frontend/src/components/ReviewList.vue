<script setup>
import { Pencil, Trash2 } from 'lucide-vue-next'
import StarRating from './StarRating.vue'
import PosterImage from './PosterImage.vue'
import { longDate } from '../utils/format'

defineProps({
  reviews: { type: Array, required: true },
  currentUserId: { type: Number, default: null },
  showMovie: { type: Boolean, default: false }, // profile page: show which movie each review is about
})
const emit = defineEmits(['edit', 'delete'])
</script>

<template>
  <ul class="divide-y divide-ash">
    <li v-for="r in reviews" :key="r.id" class="flex gap-4 py-5">
      <RouterLink
        v-if="showMovie"
        :to="{ name: 'movie', params: { id: r.movieId } }"
        class="block aspect-[2/3] w-16 shrink-0 overflow-hidden rounded sm:w-20"
        :aria-label="r.movieTitle"
      >
        <PosterImage :movie="{ id: r.movieId, title: r.movieTitle, posterUrl: r.moviePosterUrl }" />
      </RouterLink>
      <span v-else class="grid h-10 w-10 shrink-0 place-items-center rounded bg-ash font-bold uppercase" aria-hidden="true">
        {{ r.username.charAt(0) }}
      </span>

      <div class="min-w-0 flex-1">
        <div class="flex flex-wrap items-baseline gap-x-3 gap-y-1">
          <RouterLink v-if="showMovie" :to="{ name: 'movie', params: { id: r.movieId } }" class="font-bold hover:underline">
            {{ r.movieTitle }}
          </RouterLink>
          <span v-else class="font-bold">
            {{ r.username }}
            <span v-if="r.userId === currentUserId" class="ml-1 rounded bg-honey px-1.5 py-0.5 text-xs font-semibold text-black">Jij</span>
          </span>
          <time :datetime="r.createdAt" class="text-sm text-smoke">
            {{ longDate(r.createdAt) }}<span v-if="r.updatedAt"> · bewerkt</span>
          </time>
        </div>
        <StarRating :model-value="r.rating" readonly :size="16" class="mt-1" />
        <p v-if="r.content" class="mt-2 whitespace-pre-line text-snow/90">{{ r.content }}</p>

        <div v-if="r.userId === currentUserId" class="mt-3 flex gap-4 text-sm">
          <button type="button" class="inline-flex items-center gap-1.5 text-smoke hover:text-snow" @click="emit('edit', r)">
            <Pencil :size="14" aria-hidden="true" /> Bewerken
          </button>
          <button type="button" class="inline-flex items-center gap-1.5 text-smoke hover:text-red-400" @click="emit('delete', r)">
            <Trash2 :size="14" aria-hidden="true" /> Verwijderen
          </button>
        </div>
      </div>
    </li>
  </ul>
</template>
