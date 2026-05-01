const pickStringUrl = (value) => {
  if (!value || typeof value !== 'string') return ''
  return value.trim()
}

const pickUrlFromItem = (item) => {
  if (!item) return ''
  if (typeof item === 'string') return pickStringUrl(item)
  return pickStringUrl(item.url || item.imageUrl || item.src || item.path)
}

const firstFromCollection = (value) => {
  if (!Array.isArray(value)) return ''
  for (const item of value) {
    const url = pickUrlFromItem(item)
    if (url) return url
  }
  return ''
}

const firstImageFromContent = (content = '') => {
  if (typeof content !== 'string' || !content) return ''

  const htmlMatch = content.match(/<img\b[^>]*\bsrc=(["'])(.*?)\1/i)
  if (htmlMatch?.[2]) return htmlMatch[2].trim()

  const markdownMatch = content.match(/!\[[^\]]*]\(([^)\s]+)(?:\s+["'][^"']*["'])?\)/)
  return markdownMatch?.[1]?.trim() || ''
}

export const getPostThumbnail = (post) => {
  if (!post) return ''
  return (
    firstFromCollection(post.images) ||
    firstFromCollection(post.attachments) ||
    pickStringUrl(post.imageUrl) ||
    pickStringUrl(post.photoUrl) ||
    firstImageFromContent(post.content)
  )
}

export const hideBrokenThumbnail = (event) => {
  if (event?.target?.style) event.target.style.display = 'none'
}
