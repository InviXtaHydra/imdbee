import api from './api'

export default {
  home: () => api.get('/movies/home').then((r) => r.data),
  genres: () => api.get('/movies/genres').then((r) => r.data),
  details: (id) => api.get(`/movies/${id}`).then((r) => r.data),
  search: ({ query, genre, minRating, page = 1 }) =>
    api
      .get('/movies/search', {
        params: { query: query || undefined, genre: genre || undefined, minRating: minRating || undefined, page },
      })
      .then((r) => r.data),
}
