import { defineStore } from 'pinia'
import { ref } from 'vue'
import movieService from '../services/movieService'

/** Caches data that rarely changes during a session: the home rows and the genre list. */
export const useMovieStore = defineStore('movies', () => {
  const home = ref(null)
  const genres = ref([])
  const demoMode = ref(false)

  async function loadHome() {
    if (home.value) return home.value
    home.value = await movieService.home()
    demoMode.value = home.value.demoMode
    return home.value
  }

  async function loadGenres() {
    if (genres.value.length) return genres.value
    genres.value = await movieService.genres()
    return genres.value
  }

  function genreName(id) {
    return genres.value.find((g) => g.id === id)?.name
  }

  return { home, genres, demoMode, loadHome, loadGenres, genreName }
})
