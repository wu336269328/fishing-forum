<template>
  <div class="page-shell">
    <section class="hero-panel">
      <p class="eyebrow">Weather</p>
      <h1 class="hero-title">查天气、看钓鱼指数、规划下次出钓。</h1>
      <p class="hero-subtitle">实时天气、空气质量与未来 7 天预报，专为钓友提供的钓鱼建议。</p>
    </section>
    <div class="page-toolbar weather-toolbar">
      <el-input v-model="city" placeholder="输入城市名，如：北京、上海" size="default" class="weather-city-input" @keyup.enter="load" />
      <el-button type="primary" @click="load" :loading="loading">查询</el-button>
      <span class="text-muted weather-update-time" v-if="w?.report_time">更新于 {{ w.report_time }}</span>
    </div>

    <div v-if="errorMsg" class="card weather-error">
      <div>
        <b>天气查询失败</b>
        <p class="text-muted">{{ errorMsg }}</p>
      </div>
      <el-button size="small" type="primary" @click="load">重新加载</el-button>
    </div>

    <template v-if="w">
      <!-- 天气概况 2列布局 -->
      <div class="weather-top-grid">
        <!-- 实时天气 -->
        <div class="card">
          <div class="weather-now-row">
            <div>
              <div class="weather-temp">{{ w.temperature }}°C</div>
              <div class="weather-label">{{ w.weather }}</div>
              <div class="text-muted weather-loc">{{ w.province }} · {{ w.city }}</div>
            </div>
            <div class="weather-meta-grid">
              <span>💧 湿度: {{ w.humidity }}%</span>
              <span>🌬️ {{ w.wind_direction }} {{ w.wind_power }}</span>
              <span v-if="w.feels_like!=null">🌡️ 体感: {{ w.feels_like }}°C</span>
              <span v-if="w.pressure">📊 气压: {{ w.pressure }} hPa</span>
              <span v-if="w.visibility">👁 能见度: {{ w.visibility }} km</span>
              <span v-if="w.uv!=null">☀️ 紫外线: {{ w.uv }}</span>
              <span v-if="w.cloud!=null">☁️ 云量: {{ w.cloud }}%</span>
              <span v-if="w.precipitation!=null">🌧️ 降水: {{ w.precipitation }} mm</span>
            </div>
          </div>
        </div>

        <!-- 空气质量 + 钓鱼指数 -->
        <div>
          <div v-if="w.aqi!=null" class="card">
            <h3 class="weather-card-title">空气质量</h3>
            <div class="weather-aqi-row">
              <div class="weather-aqi-num">
                <div :style="{color:aqiColor}" class="weather-aqi-value">{{ w.aqi }}</div>
                <div class="weather-aqi-cat">{{ w.aqi_category }}</div>
              </div>
              <div>
                <div v-if="w.aqi_primary" class="text-muted">主要污染物: {{ w.aqi_primary }}</div>
                <div v-if="w.air_pollutants" class="weather-pollutants">
                  <span v-if="w.air_pollutants.pm25">PM2.5: {{ w.air_pollutants.pm25 }}</span>
                  <span v-if="w.air_pollutants.pm10">PM10: {{ w.air_pollutants.pm10 }}</span>
                  <span v-if="w.air_pollutants.o3">O₃: {{ w.air_pollutants.o3 }}</span>
                  <span v-if="w.air_pollutants.no2">NO₂: {{ w.air_pollutants.no2 }}</span>
                  <span v-if="w.air_pollutants.so2">SO₂: {{ w.air_pollutants.so2 }}</span>
                  <span v-if="w.air_pollutants.co">CO: {{ w.air_pollutants.co }}</span>
                </div>
              </div>
            </div>
          </div>
          <!-- 钓鱼指数 -->
          <div v-if="fishIndex" class="card weather-fishcard">
            <h3 class="weather-card-title">钓鱼指数</h3>
            <div class="weather-fish-level">{{ fishIndex.level }} — {{ fishIndex.brief }}</div>
            <p class="weather-fish-advice">{{ fishIndex.advice }}</p>
          </div>
        </div>
      </div>

      <!-- 多天预报 -->
      <div v-if="w.forecast?.length" class="card">
        <h3 class="weather-card-title">未来天气预报</h3>
        <div class="forecast-grid">
          <div v-for="(d, i) in w.forecast" :key="i" class="forecast-day">
            <div class="forecast-date">{{ d.date || dayLabel(i) }}</div>
            <div class="forecast-weather">{{ d.weather_day || d.weather || '—' }}</div>
            <div class="forecast-temp">{{ d.temp_min }}° ~ {{ d.temp_max }}°</div>
            <div class="text-muted forecast-wind">{{ d.wind_direction_day || '' }} {{ d.wind_power_day || '' }}</div>
          </div>
        </div>
      </div>

      <!-- 生活指数 -->
      <div v-if="w.life_indices" class="card">
        <h3 class="weather-card-title">生活指数</h3>
        <div class="indices-grid">
          <div v-for="(item, key) in filteredIndices" :key="key" class="index-item">
            <div class="index-head">
              <span class="index-label">{{ indexLabels[key] || key }}</span>
              <el-tag size="small" effect="plain">{{ item.level }}</el-tag>
            </div>
            <p class="text-muted index-advice">{{ item.advice }}</p>
          </div>
        </div>
      </div>
    </template>

    <div v-if="!w && !loading && queried && !errorMsg" class="card empty-state">
      <div class="empty-state-icon">🌤️</div>
      <div class="empty-state-title">没找到这座城市的天气</div>
      <div class="empty-state-text">检查城市名是否正确，例如「北京」「上海」「杭州」。</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../api/request'

const city = ref('北京'), w = ref(null), loading = ref(false), queried = ref(false), errorMsg = ref('')

const indexLabels = { clothing:'👔 穿衣', uv:'☀️ 紫外线', car_wash:'🚗 洗车', drying:'👕 晾晒', cold_risk:'🤧 感冒', exercise:'🏃 运动', comfort:'😊 舒适度', travel:'✈️ 出行', fishing:'🎣 钓鱼', allergy:'🌸 过敏', sunscreen:'🧴 防晒', umbrella:'☂️ 雨伞', beer:'🍺 啤酒', mood:'😄 心情', traffic:'🚦 交通', pollen:'🌼 花粉', air_conditioner:'❄️ 空调', air_purifier:'🌬️ 净化器' }
const fishIndex = computed(() => w.value?.life_indices?.fishing)
const filteredIndices = computed(() => {
  if (!w.value?.life_indices) return {}
  const r = {}; for (const [k, v] of Object.entries(w.value.life_indices)) { if (v && v.level) r[k] = v }; return r
})
const aqiColor = computed(() => { const a = w.value?.aqi; if (!a) return '#999'; if (a <= 50) return '#52c41a'; if (a <= 100) return '#faad14'; if (a <= 150) return '#fa8c16'; if (a <= 200) return '#f5222d'; return '#722ed1' })
const dayLabel = (i) => ['今天','明天','后天','第4天','第5天','第6天','第7天'][i] || ''

const load = async () => {
  loading.value = true; queried.value = true; errorMsg.value = ''
  try {
    const res = await request.get('/api/weather', { params: { city: city.value } })
    if (res.code === 200 && res.data?.city) {
      w.value = res.data
    } else {
      errorMsg.value = res.message || '查询失败'
      w.value = null
    }
  } catch (e) {
    errorMsg.value = '天气查询失败: ' + (e.response?.data?.message || e.message)
    w.value = null
  }
  loading.value = false
}
onMounted(load)
</script>

<style scoped>
.weather-toolbar { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.weather-city-input { width: 220px; }
.weather-update-time { margin-left: auto; }
.weather-top-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; align-items: start; }
.weather-now-row { display: flex; gap: 24px; flex-wrap: wrap; align-items: center; }
.weather-temp { font-size: 48px; font-weight: 800; color: var(--color-primary-dark); letter-spacing: -0.02em; }
.weather-label { font-size: 16px; margin-top: 4px; color: var(--ink); }
.weather-loc { margin-top: 2px; }
.weather-meta-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px 24px; font-size: 14px; color: var(--ink); }
.weather-card-title { font-size: 14px; font-weight: 700; margin-bottom: 8px; color: var(--ink); }
.weather-aqi-row { display: flex; gap: 16px; align-items: center; flex-wrap: wrap; }
.weather-aqi-num { text-align: center; }
.weather-aqi-value { font-size: 28px; font-weight: 800; }
.weather-aqi-cat { font-size: 13px; color: var(--muted); }
.weather-pollutants { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 4px 12px; font-size: 12px; color: var(--muted); margin-top: 4px; }
.weather-fishcard { background: var(--color-primary-soft); border-color: #cfe1f5; }
.weather-fish-level { font-size: 16px; font-weight: 700; color: var(--color-primary-dark); }
.weather-fish-advice { font-size: 13px; color: var(--ink); margin-top: 4px; line-height: 1.6; }
.weather-error { display: flex; align-items: center; gap: 12px; justify-content: space-between; border-color: #fecaca; background: rgba(254, 242, 242, .8); color: #7f1d1d; }
.weather-error b { color: #7f1d1d; }
.forecast-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(110px, 1fr)); gap: 10px; }
.forecast-day { text-align: center; padding: 12px 8px; background: var(--surface-muted); border-radius: 12px; border: 1px solid var(--border-subtle); }
.forecast-date { font-weight: 600; font-size: 13px; color: var(--ink); }
.forecast-weather { font-size: 13px; margin: 4px 0; color: var(--muted); }
.forecast-temp { font-size: 15px; font-weight: 700; color: var(--ink); }
.forecast-wind { font-size: 11px; }
.indices-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 10px; }
.index-item { padding: 10px 12px; background: var(--surface-muted); border-radius: 12px; border: 1px solid var(--border-subtle); }
.index-head { display: flex; justify-content: space-between; align-items: center; }
.index-label { font-weight: 600; font-size: 13px; color: var(--ink); }
.index-advice { margin-top: 4px; font-size: 12px; line-height: 1.5; }
@media (max-width: 900px) { .weather-top-grid { grid-template-columns: 1fr; } .weather-meta-grid { grid-template-columns: 1fr 1fr; } }
</style>
