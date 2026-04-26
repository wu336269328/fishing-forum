import { defineStore } from 'pinia'
import { computed, ref, watch } from 'vue'

const STORAGE_KEY = 'fishforum:view-mode'
const VALID_MODES = new Set(['auto', 'desktop', 'mobile'])

export const useViewModeStore = defineStore('viewMode', () => {
    const readInitialMode = () => {
        try {
            const saved = localStorage.getItem(STORAGE_KEY)
            return VALID_MODES.has(saved) ? saved : 'auto'
        } catch (e) {
            return 'auto'
        }
    }

    const mode = ref(readInitialMode())
    const modeLabel = computed(() => ({ auto: '自动', desktop: 'PC版', mobile: '手机版' }[mode.value]))
    const appClass = computed(() => ({
        'view-auto': mode.value === 'auto',
        'view-desktop': mode.value === 'desktop',
        'view-mobile': mode.value === 'mobile',
    }))

    const setMode = (nextMode) => {
        mode.value = VALID_MODES.has(nextMode) ? nextMode : 'auto'
    }

    watch(mode, (value) => {
        try {
            localStorage.setItem(STORAGE_KEY, value)
        } catch (e) {
            // localStorage may be unavailable in private/restricted contexts.
        }
    }, { immediate: true })

    return { mode, modeLabel, appClass, setMode }
})
