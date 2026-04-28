import assert from 'node:assert/strict'
import { isPublicBrowsingRequest, shouldAttachAuth, shouldRedirectToLogin } from '../src/api/requestPolicy.js'

const get = (url) => ({ method: 'get', url })
const post = (url) => ({ method: 'post', url })

for (const url of [
  '/api/spots',
  '/api/spots?keyword=lake',
  '/api/spots/all',
  '/api/spots/1',
  '/api/spots/1/reviews',
  '/api/wiki',
  '/api/wiki?category=fish',
  '/api/wiki/categories',
  '/api/wiki/1',
  '/api/wiki/1/history',
  '/api/wiki/1/comments'
]) {
  assert.equal(isPublicBrowsingRequest(get(url)), true, `${url} should be public browsing`)
  assert.equal(shouldAttachAuth(get(url)), false, `${url} should not attach auth`)
  assert.equal(shouldRedirectToLogin(401, get(url)), false, `${url} should not redirect to login on 401`)
}

for (const config of [
  post('/api/spots'),
  post('/api/spots/1/reviews'),
  post('/api/wiki'),
  post('/api/wiki/1/comments'),
  get('/api/users/me'),
  get('/api/messages'),
  get('/api/notifications')
]) {
  assert.equal(isPublicBrowsingRequest(config), false, `${config.method} ${config.url} should not be public browsing`)
  assert.equal(shouldAttachAuth(config), true, `${config.method} ${config.url} should attach auth`)
  assert.equal(shouldRedirectToLogin(401, config), true, `${config.method} ${config.url} should redirect to login on 401`)
}
