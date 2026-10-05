<script setup>
import { ref } from 'vue'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'
import MovieCard from './MovieCard.vue'

defineProps({
  title: { type: String, required: true },
  movies: { type: Array, required: true },
})

const track = ref(null)

function scroll(dir) {
  const el = track.value
  el.scrollBy({ left: dir * el.clientWidth * 0.85, behavior: 'smooth' })
}
</script>

<template>
  <section class="group/row relative" :aria-label="title">
    <h2 class="page section-title mb-1 text-lg md:text-xl">{{ title }}</h2>
    <div class="relative">
      <button
        type="button"
        class="absolute inset-y-0 left-0 z-20 hidden w-12 items-center justify-center bg-black/50 opacity-0 transition-opacity group-hover/row:opacity-100 focus-visible:opacity-100 md:flex"
        aria-label="Vorige films"
        @click="scroll(-1)"
      >
        <ChevronLeft :size="32" />
      </button>
      <ul
        ref="track"
        class="no-scrollbar page flex snap-x snap-mandatory scroll-px-4 gap-2 overflow-x-auto py-4 md:scroll-px-12"
      >
        <li v-for="m in movies" :key="m.id" class="w-[34vw] shrink-0 snap-start sm:w-[24vw] md:w-[17vw] lg:w-[13.5vw] xl:w-[11.5vw]">
          <MovieCard :movie="m" />
        </li>
      </ul>
      <button
        type="button"
        class="absolute inset-y-0 right-0 z-20 hidden w-12 items-center justify-center bg-black/50 opacity-0 transition-opacity group-hover/row:opacity-100 focus-visible:opacity-100 md:flex"
        aria-label="Volgende films"
        @click="scroll(1)"
      >
        <ChevronRight :size="32" />
      </button>
    </div>
  </section>
</template>
