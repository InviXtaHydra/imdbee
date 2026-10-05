<script setup>
import MovieCard from './MovieCard.vue'

defineProps({
  movies: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  skeletons: { type: Number, default: 12 },
})
</script>

<template>
  <ul class="grid grid-cols-3 gap-2 sm:grid-cols-4 sm:gap-3 md:grid-cols-5 lg:grid-cols-6 xl:grid-cols-7">
    <li v-for="m in movies" :key="m.id">
      <MovieCard :movie="m" />
    </li>
    <template v-if="loading">
      <li v-for="n in skeletons" :key="`s${n}`" class="aspect-[2/3] animate-pulse rounded-md bg-coal" aria-hidden="true" />
    </template>
  </ul>
</template>
