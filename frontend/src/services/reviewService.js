import api from './api'

export default {
  forMovie: (movieId) => api.get(`/reviews/movie/${movieId}`).then((r) => r.data),
  mine: () => api.get('/reviews/me').then((r) => r.data),
  create: (payload) => api.post('/reviews', payload).then((r) => r.data),
  update: (id, payload) => api.put(`/reviews/${id}`, payload).then((r) => r.data),
  remove: (id) => api.delete(`/reviews/${id}`),
}
