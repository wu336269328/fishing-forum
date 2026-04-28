import assert from 'node:assert/strict'
import { normalizePostType } from '../src/utils/postType.js'

assert.equal(normalizePostType('CATCH'), 'CATCH')
assert.equal(normalizePostType('REVIEW'), 'REVIEW')
assert.equal(normalizePostType('NORMAL'), 'NORMAL')
assert.equal(normalizePostType('catch'), 'NORMAL')
assert.equal(normalizePostType(undefined), 'NORMAL')
assert.equal(normalizePostType('BAD'), 'NORMAL')
