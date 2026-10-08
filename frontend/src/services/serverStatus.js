import { ref } from 'vue'

/** True while the backend is being woken up (the free Render plan sleeps after 15 minutes without traffic). */
export const serverWaking = ref(false)
