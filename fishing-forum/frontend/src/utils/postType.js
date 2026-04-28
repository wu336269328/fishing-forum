const ALLOWED_POST_TYPES = new Set(['NORMAL', 'CATCH', 'REVIEW'])

export const normalizePostType = (value) => ALLOWED_POST_TYPES.has(value) ? value : 'NORMAL'
