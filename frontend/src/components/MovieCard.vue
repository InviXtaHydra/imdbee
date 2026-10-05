<script setup>
import { Star } from 'lucide-vue-next'
import PosterImage from './PosterImage.vue'
import { score, year } from '../utils/format'

defineProps({
  movie: { type: Object, required: true },
})
</script>

<template>
  <RouterLink
    :to="{ name: 'movie', params: { id: movie.id } }"
    class="group relative block aspect-[2/3] overflow-hidden rounded-md bg-coal ring-honey transition-transform duration-300 hover:z-10 hover:scale-105 focus-visible:scale-105 focus-visible:ring-2 focus-visible:outline-none"
  >
    <PosterImage :movie="movie" />
    <div
      class="absolute inset-x-0 bottom-0 translate-y-2 bg-gradient-to-t from-black via-black/80 to-transparent p-3 pt-10 opacity-0 transition duration-300 group-hover:translate-y-0 group-hover:opacity-100 group-focus-visible:translate-y-0 group-focus-visible:opacity-100"
    >
      <p class="line-clamp-2 text-sm leading-tight font-semibold">{{ movie.title }}</p>
      <p class="mt-1 flex items-center gap-2 text-xs text-smoke">
        <span v-if="movie.releaseDate">{{ year(movie.releaseDate) }}</span>
        <span v-if="movie.voteAverage" class="inline-flex items-center gap-0.5">
          <Star :size="12" class="fill-honey text-honey" aria-hidden="true" /> {{ score(movie.voteAverage) }}
        </span>
      </p>
    </div>
  </RouterLink>
</template>
