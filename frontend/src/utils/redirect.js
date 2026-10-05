/** Only follow redirects within this app (blocks "//evil.com" and absolute URLs). */
export function safeRedirect(target) {
  return typeof target === 'string' && target.startsWith('/') && !target.startsWith('//') ? target : '/'
}
