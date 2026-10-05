<script setup>
import { computed, ref } from 'vue'
import { Star } from 'lucide-vue-next'

const props = defineProps({
  modelValue: { type: Number, default: 0 },
  readonly: { type: Boolean, default: false },
  size: { type: Number, default: 18 },
  label: { type: String, default: 'Beoordeling' },
})
const emit = defineEmits(['update:modelValue'])

const labels = ['Slecht', 'Matig', 'Oké', 'Goed', 'Uitstekend']
const hover = ref(0)
const shown = computed(() => hover.value || props.modelValue)

function set(v) {
  emit('update:modelValue', v)
}
// Radio-group keyboard behaviour: arrows change the value
function onKey(e) {
  const delta = { ArrowRight: 1, ArrowUp: 1, ArrowLeft: -1, ArrowDown: -1 }[e.key]
  if (!delta) return
  e.preventDefault()
  set(Math.min(5, Math.max(1, (props.modelValue || 0) + delta)))
}
</script>

<template>
  <span v-if="readonly" class="inline-flex items-center gap-0.5" role="img" :aria-label="`${modelValue} van 5 sterren`">
    <Star
      v-for="n in 5"
      :key="n"
      :size="size"
      :class="n <= Math.round(modelValue) ? 'fill-honey text-honey' : 'text-ash'"
      aria-hidden="true"
    />
  </span>

  <div v-else class="flex items-center gap-3">
    <div role="radiogroup" :aria-label="label" class="inline-flex" @mouseleave="hover = 0" @keydown="onKey">
      <button
        v-for="n in 5"
        :key="n"
        type="button"
        role="radio"
        :aria-checked="modelValue === n"
        :aria-label="`${n} ${n === 1 ? 'ster' : 'sterren'}: ${labels[n - 1]}`"
        :tabindex="modelValue === n || (!modelValue && n === 1) ? 0 : -1"
        class="p-0.5 transition-transform hover:scale-110"
        @click="set(n)"
        @mouseenter="hover = n"
      >
        <Star :size="size" :class="n <= shown ? 'fill-honey text-honey' : 'text-smoke/50'" aria-hidden="true" />
      </button>
    </div>
    <span class="min-w-20 text-sm text-smoke" aria-hidden="true">{{ shown ? labels[shown - 1] : 'Kies een score' }}</span>
  </div>
</template>
