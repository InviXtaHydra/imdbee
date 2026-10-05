<script setup>
import { Info, MessageSquareText, Play, Star } from 'lucide-vue-next'
import PosterImage from './PosterImage.vue'
import { score, year } from '../utils/format'

defineProps({
  movie: { type: Object, required: true },
  // Trailers come from TMDB, so the demo catalog has none
  showTrailer: { type: Boolean, default: false },
})
</script>

<template>
  <section class="relative h-[78vh] min-h-[28rem] w-full overflow-hidden md:h-[88vh]">
    <div class="absolute inset-0">
      <PosterImage :movie="movie" variant="backdrop" eager />
    </div>
    <!-- Fade into the page -->
    <div class="absolute inset-0 bg-gradient-to-r from-black/85 via-black/40 to-transparent" aria-hidden="true" />
    <div class="absolute inset-x-0 bottom-0 h-40 bg-gradient-to-t from-night to-transparent" aria-hidden="true" />

    <div class="page absolute inset-x-0 bottom-[14%] max-w-3xl">
      <h1 class="display text-5xl drop-shadow-lg sm:text-6xl md:text-7xl lg:text-8xl">{{ movie.title }}</h1>
      <p class="mt-4 flex items-center gap-3 text-sm font-semibold text-smoke">
        <span v-if="movie.voteAverage" class="inline-flex items-center gap-1 text-snow">
          <Star :size="16" class="fill-honey text-honey" aria-hidden="true" />{{ score(movie.voteAverage) }}<span class="font-normal text-smoke">/10</span>
        </span>
        <span v-if="movie.releaseDate">{{ year(movie.releaseDate) }}</span>
      </p>
      <p class="mt-3 line-clamp-3 max-w-xl text-base text-snow/90 drop-shadow md:text-lg">{{ movie.overview }}</p>
      <div class="mt-6 flex flex-wrap gap-3">
        <RouterLink v-if="showTrailer" :to="{ name: 'movie', params: { id: movie.id }, query: { trailer: '1' } }" class="btn btn-light">
          <Play :size="20" class="fill-current" aria-hidden="true" /> Trailer
        </RouterLink>
        <RouterLink :to="{ name: 'movie', params: { id: movie.id } }" class="btn" :class="showTrailer ? 'btn-ghost' : 'btn-light'">
          <Info :size="20" aria-hidden="true" /> Meer info
        </RouterLink>
        <RouterLink :to="{ name: 'movie', params: { id: movie.id }, hash: '#reviews' }" class="btn btn-ghost" :class="showTrailer && 'max-sm:hidden'">
          <MessageSquareText :size="20" aria-hidden="true" /> Reviews
        </RouterLink>
      </div>
    </div>
  </section>
</template>
