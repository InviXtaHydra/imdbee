<script setup>
import { computed, ref, watch } from 'vue'
import { Film } from 'lucide-vue-next'

const props = defineProps({
  movie: { type: Object, required: true },
  variant: { type: String, default: 'poster' }, // 'poster' | 'backdrop'
  eager: { type: Boolean, default: false },
})

const failed = ref(false)
const src = computed(() => (props.variant === 'backdrop' ? props.movie.backdropUrl : props.movie.posterUrl))
watch(src, () => (failed.value = false))

// Stable colors per movie, so a placeholder always looks the same
const hue = computed(() => (Number(props.movie.id) * 47) % 360)
const placeholderStyle = computed(() => ({
  background: `linear-gradient(160deg, hsl(${hue.value} 45% 22%), hsl(${(hue.value + 40) % 360} 55% 8%) 70%)`,
}))
</script>

<template>
  <img
    v-if="src && !failed"
    :src="src"
    :alt="variant === 'poster' ? `Poster van ${movie.title}` : ''"
    :loading="eager ? 'eager' : 'lazy'"
    class="h-full w-full object-cover"
    @error="failed = true"
  />
  <div
    v-else
    class="@container relative flex h-full w-full items-end overflow-hidden"
    :style="placeholderStyle"
    role="img"
    :aria-label="variant === 'poster' ? `Poster van ${movie.title}` : undefined"
    :aria-hidden="variant === 'backdrop' ? 'true' : undefined"
  >
    <template v-if="variant === 'poster'">
      <Film class="absolute top-3 left-3 h-4 w-4 text-white/25 @[10rem]:h-5 @[10rem]:w-5" aria-hidden="true" />
      <span class="display relative p-2 text-sm text-white/90 [overflow-wrap:break-word] @[7rem]:p-3 @[7rem]:text-lg @[12rem]:text-2xl">
        {{ movie.title }}
      </span>
    </template>
  </div>
</template>
