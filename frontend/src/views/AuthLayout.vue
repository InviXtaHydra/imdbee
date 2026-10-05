<script setup>
import AppLogo from '../components/AppLogo.vue'

defineProps({ title: { type: String, required: true } })
</script>

<template>
  <div class="relative min-h-dvh overflow-hidden bg-black">
    <!-- Soft yellow light from the logo instead of a stock photo -->
    <div
      class="pointer-events-none absolute inset-0 opacity-60"
      style="background: radial-gradient(60rem 30rem at 85% -10%, rgb(251 197 11 / 0.2), transparent 60%), radial-gradient(40rem 30rem at -10% 110%, rgb(224 174 0 / 0.12), transparent 60%)"
      aria-hidden="true"
    />
    <header class="page relative flex h-20 items-center">
      <AppLogo />
    </header>
    <div class="relative flex justify-center px-4 pb-16 sm:pt-6">
      <div class="w-full max-w-md rounded-lg bg-black/75 p-6 sm:border sm:border-ash/60 sm:p-12">
        <h1 class="mb-7 text-3xl font-bold">{{ title }}</h1>
        <slot />
      </div>
    </div>
  </div>
</template>
