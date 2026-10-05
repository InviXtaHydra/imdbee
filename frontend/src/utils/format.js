export const year = (date) => (date ? new Date(date).getFullYear() : '')

export const longDate = (date) =>
  date ? new Intl.DateTimeFormat('nl-BE', { day: 'numeric', month: 'long', year: 'numeric' }).format(new Date(date)) : ''

export const runtime = (min) => (min ? `${Math.floor(min / 60)}u ${String(min % 60).padStart(2, '0')}m` : '')

export const score = (v) => (v == null ? null : v.toFixed(1))
