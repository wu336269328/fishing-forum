export function createRequestErrorNotifier(showMessage, options = {}) {
    const cooldownMs = options.cooldownMs ?? 1200
    const now = options.now ?? (() => Date.now())
    const lastShownAt = new Map()

    return (message) => {
        const currentTime = now()
        const previousTime = lastShownAt.get(message)
        if (previousTime !== undefined && currentTime - previousTime < cooldownMs) {
            return
        }
        lastShownAt.set(message, currentTime)
        showMessage(message)
    }
}
