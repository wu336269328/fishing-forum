<template>
  <div>
    <h1 class="page-title">🌤 天气查询</h1>
    <div class="card" style="display:flex; gap:8px; align-items:center">
      <el-input v-model="city" placeholder="输入城市名" size="default" style="width:200px" @keyup.enter="load" />
      <el-button type="primary" @click="load" :loading="loading">查询</el-button>
      <span class="text-muted" v-if="w?.report_time">更新于 {{ w.report_time }}</span>
    </div>

    <div v-if="errorMsg" class="card" style="color:#e74c3c">{{ errorMsg }}</div>

    <template v-if="w">
      <!-- 天气概况 2列布局 -->
      <div class="weather-top-grid">
        <!-- 实时天气 -->
        <div class="card">
          <div style="display:flex; gap:24px; flex-wrap:wrap; align-items:center">
            <div>
              <div style="font-size:48px; font-weight:700; color:#1a73e8">{{ w.temperature }}°C</div>
              <div style="font-size:16px; margin-top:4px">{{ w.weather }}</div>
              <div class="text-muted" style="margin-top:2px">{{ w.province }} · {{ w.city }}</div>
            </div>
            <div style="display:grid; grid-template-columns:1fr 1fr; gap:8px 24px; font-size:14px; color:#555">
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
            <h3 style="font-size:14px; margin-bottom:8px">🌬️ 空气质量</h3>
            <div style="display:flex; gap:16px; align-items:center; flex-wrap:wrap">
              <div style="text-align:center">
                <div :style="{fontSize:'28px',fontWeight:'700',color:aqiColor}">{{ w.aqi }}</div>
                <div style="font-size:13px; color:#777">{{ w.aqi_category }}</div>
              </div>
              <div>
                <div v-if="w.aqi_primary" class="text-muted">主要污染物: {{ w.aqi_primary }}</div>
                <div v-if="w.air_pollutants" style="display:grid; grid-template-columns:1fr 1fr 1fr; gap:4px 12px; font-size:12px; color:#777; margin-top:4px">
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
          <div v-if="fishIndex" class="card" style="background:#f0f9ff; border-color:#bae6fd">
            <h3 style="font-size:14px; margin-bottom:6px">🎣 钓鱼指数</h3>
            <div style="font-size:16px; font-weight:600; color:#1a73e8">{{ fishIndex.level }} — {{ fishIndex.brief }}</div>
            <p style="font-size:13px; color:#555; margin-top:4px">{{ fishIndex.advice }}</p>
          </div>
        </div>
      </div>



      <!-- 多天预报 -->
      <div v-if="w.forecast?.length" class="card">
        <h3 style="font-size:14px; margin-bottom:10px">📅 未来天气预报</h3>
        <div class="forecast-grid">
          <div v-for="(d, i) in w.forecast" :key="i" class="forecast-day">
            <div style="font-weight:500; font-size:13px">{{ d.date || dayLabel(i) }}</div>
            <div style="font-size:13px; margin:4px 0">{{ d.weather_day || d.weather || '—' }}</div>
            <div style="font-size:15px; font-weight:600">{{ d.temp_min }}° ~ {{ d.temp_max }}°</div>
            <div class="text-muted" style="font-size:11px">{{ d.wind_direction_day || '' }} {{ d.wind_power_day || '' }}</div>
          </div>
        </div>
      </div>

      <!-- 生活指数 -->
      <div v-if="w.life_indices" class="card">
        <h3 style="font-size:14px; margin-bottom:10px">📋 生活指数</h3>
        <div class="indices-grid">
          <div v-for="(item, key) in filteredIndices" :key="key" class="index-item">
            <div style="display:flex; justify-content:space-between; align-items:center">
              <span style="font-weight:500; font-size:13px">{{ indexLabels[key] || key }}</span>
              <el-tag size="small" effect="plain">{{ item.level }}</el-tag>
            </div>
            <p class="text-muted" style="margin-top:2px">{{ item.advice }}</p>
          </div>
        </div>
      </div>
    </template>

    <el-empty v-if="!w && !loading && queried" description="未查到天气数据" />
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
.weather-top-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; align-items: start; }
.forecast-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(110px, 1fr)); gap: 10px; }
.forecast-day { text-align: center; padding: 10px 6px; background: #f9f9f9; border-radius: 6px; }
.indices-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 10px; }
.index-item { padding: 8px 10px; background: #f9f9f9; border-radius: 6px; }
@media (max-width: 900px) { .weather-top-grid { grid-template-columns: 1fr; } }
</style>
