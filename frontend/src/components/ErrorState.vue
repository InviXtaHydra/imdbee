<script setup>
defineProps({ message: { type: String, required: true } })
const emit = defineEmits(['retry'])
</script>

<template>
  <div class="flex flex-col items-start gap-4 rounded-lg border border-ash bg-coal/60 p-6" role="alert">
    <p>{{ message }}</p>
    <button type="button" class="btn btn-ghost" @click="emit('retry')">Opnieuw proberen</button>
  </div>
</template>
