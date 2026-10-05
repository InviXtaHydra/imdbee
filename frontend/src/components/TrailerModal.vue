<script setup>
import { nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { X } from 'lucide-vue-next'

const props = defineProps({
  open: { type: Boolean, default: false },
  videoKey: { type: String, default: null },
  title: { type: String, default: '' },
})
const emit = defineEmits(['close'])

const closeBtn = ref(null)
let lastFocus = null

function onKey(e) {
  if (e.key === 'Escape') emit('close')
}

watch(
  () => props.open,
  async (open) => {
    if (open) {
      lastFocus = document.activeElement
      document.body.style.overflow = 'hidden'
      window.addEventListener('keydown', onKey)
      await nextTick()
      closeBtn.value?.focus()
    } else {
      document.body.style.overflow = ''
      window.removeEventListener('keydown', onKey)
      lastFocus?.focus?.()
    }
  },
  { immediate: true },
)
onBeforeUnmount(() => {
  document.body.style.overflow = ''
  window.removeEventListener('keydown', onKey)
})
</script>

<template>
  <Teleport to="body">
    <Transition enter-from-class="opacity-0" leave-to-class="opacity-0" enter-active-class="transition duration-200" leave-active-class="transition duration-150">
      <div
        v-if="open && videoKey"
        class="fixed inset-0 z-[70] flex items-center justify-center bg-black/90 p-4 backdrop-blur-sm"
        role="dialog"
        aria-modal="true"
        :aria-label="`Trailer van ${title}`"
        @click.self="emit('close')"
      >
        <div class="w-full max-w-5xl">
          <div class="mb-3 flex items-center justify-between gap-4">
            <p class="truncate font-semibold">Trailer: {{ title }}</p>
            <button ref="closeBtn" type="button" class="grid h-10 w-10 shrink-0 place-items-center rounded-full bg-ash hover:bg-coal" aria-label="Trailer sluiten" @click="emit('close')">
              <X :size="22" />
            </button>
          </div>
          <div class="aspect-video w-full overflow-hidden rounded-lg bg-black shadow-2xl">
            <!-- youtube-nocookie: no tracking cookies until the viewer presses play -->
            <iframe
              class="h-full w-full"
              :src="`https://www.youtube-nocookie.com/embed/${videoKey}?autoplay=1&rel=0&modestbranding=1`"
              :title="`Trailer van ${title}`"
              allow="autoplay; encrypted-media; picture-in-picture; fullscreen"
              allowfullscreen
            />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
