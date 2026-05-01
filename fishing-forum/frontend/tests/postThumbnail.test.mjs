import assert from 'node:assert/strict'
import { getPostThumbnail } from '../src/utils/postThumbnail.js'

assert.equal(
  getPostThumbnail({ images: ['/api/uploads/images/a.jpg', '/api/uploads/images/b.jpg'] }),
  '/api/uploads/images/a.jpg'
)

assert.equal(
  getPostThumbnail({ attachments: [{ url: '/api/uploads/images/from-attachment.webp' }] }),
  '/api/uploads/images/from-attachment.webp'
)

assert.equal(
  getPostThumbnail({ content: '<p>正文</p><p><img src="/api/uploads/images/from-html.png" style="max-width:100%"/></p>' }),
  '/api/uploads/images/from-html.png'
)

assert.equal(
  getPostThumbnail({ content: '正文 ![渔获](/api/uploads/images/from-markdown.jpg)' }),
  '/api/uploads/images/from-markdown.jpg'
)

assert.equal(getPostThumbnail({ content: '<p>没有图片</p>' }), '')
assert.equal(getPostThumbnail(null), '')
