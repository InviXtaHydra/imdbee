<script setup>
import { computed, ref } from 'vue'
import StarRating from './StarRating.vue'
import reviewService from '../services/reviewService'
import { parseError } from '../services/api'

const props = defineProps({
  movieId: { type: Number, required: true },
  review: { type: Object, default: null }, // pass to edit an existing review
})
const emit = defineEmits(['saved', 'cancel'])

const MAX = 2000
const rating = ref(props.review?.rating ?? 0)
const content = ref(props.review?.content ?? '')
const saving = ref(false)
const error = ref('')

const isEdit = computed(() => !!props.review)

async function submit() {
  if (!rating.value) {
    error.value = 'Kies eerst 1 tot 5 sterren.'
    return
  }
  saving.value = true
  error.value = ''
  try {
    const payload = { rating: rating.value, content: content.value }
    const saved = isEdit.value
      ? await reviewService.update(props.review.id, payload)
      : await reviewService.create({ ...payload, movieId: props.movieId })
    emit('saved', saved)
  } catch (e) {
    const { message, fields } = parseError(e)
    error.value = Object.values(fields)[0] ?? message
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <form class="rounded-lg border border-ash bg-coal/60 p-4 sm:p-6" @submit.prevent="submit">
    <h3 class="text-lg font-bold">{{ isEdit ? 'Je review bewerken' : 'Schrijf een review' }}</h3>

    <div class="mt-4">
      <StarRating v-model="rating" :size="28" label="Jouw score" />
    </div>

    <label for="review-content" class="sr-only">Je review</label>
    <textarea
      id="review-content"
      v-model="content"
      rows="4"
      :maxlength="MAX"
      class="field mt-4 resize-y"
      placeholder="Wat vond je ervan? (optioneel)"
    />
    <div class="mt-1 text-right text-xs text-smoke">{{ content.length }} / {{ MAX }}</div>

    <p v-if="error" class="mt-2 text-sm text-[#e87c03]" role="alert">{{ error }}</p>

    <div class="mt-4 flex flex-wrap gap-3">
      <button type="submit" class="btn btn-primary" :disabled="saving">
        {{ saving ? 'Opslaan…' : isEdit ? 'Wijzigingen opslaan' : 'Review plaatsen' }}
      </button>
      <button v-if="isEdit" type="button" class="btn btn-ghost" @click="emit('cancel')">Annuleren</button>
    </div>
  </form>
</template>
