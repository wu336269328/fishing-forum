const publicBrowsingRules = [
  /^\/api\/spots(?:\/all)?$/,
  /^\/api\/spots\/\d+$/,
  /^\/api\/spots\/\d+\/reviews$/,
  /^\/api\/wiki$/,
  /^\/api\/wiki\/categories$/,
  /^\/api\/wiki\/\d+$/,
  /^\/api\/wiki\/\d+\/history$/,
  /^\/api\/wiki\/\d+\/comments$/
]

const normalizePath = (url = '') => {
  try {
    return new URL(url, 'http://local').pathname
  } catch (e) {
    return String(url).split('?')[0]
  }
}

export const isPublicBrowsingRequest = (config = {}) => {
  const method = (config.method || 'get').toLowerCase()
  if (method !== 'get') return false
  const path = normalizePath(config.url)
  return publicBrowsingRules.some((rule) => rule.test(path))
}

export const shouldAttachAuth = (config = {}) => !isPublicBrowsingRequest(config)

export const shouldRedirectToLogin = (status, config = {}) => status === 401 && !isPublicBrowsingRequest(config)
