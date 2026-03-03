<template>
  <div>
    <h1 class="page-title">钓点</h1>
    <div class="card" style="display:flex; gap:8px; flex-wrap:wrap; align-items:center">
      <el-input v-model="keyword" placeholder="搜索钓点..." clearable size="small" style="width:180px" @keyup.enter="loadSpots" />
      <el-select v-model="spotType" placeholder="类型" clearable size="small" style="width:100px">
        <el-option v-for="t in ['水库','河流','湖泊','海钓','黑坑']" :key="t" :label="t" :value="t" />
      </el-select>
      <el-button type="primary" size="small" @click="showAdd=true">+ 分享钓点</el-button>
    </div>
    <div class="spots-grid">
      <div v-for="s in spots" :key="s.id" class="card spot-card" @click="selected=s; showDetail=true">
        <div style="display:flex; gap:12px">
          <img v-if="s.imageUrl" :src="s.imageUrl" class="spot-thumb" />
          <div style="flex:1; min-width:0">
            <div style="display:flex; justify-content:space-between; align-items:center">
              <b>{{ s.name }}</b>
              <el-tag size="small">{{ s.spotType }}</el-tag>
            </div>
            <p style="font-size:13px; color:#777; margin:4px 0; line-height:1.4">{{ s.description?.substring(0,80) }}</p>
            <div style="font-size:12px; color:#999; display:flex; gap:12px; flex-wrap:wrap">
              <span>🐟 {{ s.fishTypes }}</span>
              <span>⭐ {{ s.rating }}</span>
              <span>🕐 {{ s.openTime }}</span>
            </div>
            <div style="font-size:12px; margin-top:4px; display:flex; gap:8px">
              <a :href="toGoogleMap(s)" target="_blank" @click.stop class="map-link">📍 谷歌地图</a>
              <a :href="toAmapLink(s)" target="_blank" @click.stop class="map-link">🗺️ 高德地图</a>
            </div>
          </div>
        </div>
      </div>
    </div>
    <el-empty v-if="!spots.length" description="暂无钓点，来分享第一个吧" />

    <!-- 钓点详情 -->
    <el-dialog v-model="showDetail" :title="selected?.name" width="550px" v-if="selected">
      <img v-if="selected.imageUrl" :src="selected.imageUrl" style="width:100%; max-height:240px; object-fit:cover; border-radius:6px; margin-bottom:12px" />
      <p style="margin-bottom:8px"><b>类型：</b>{{ selected.spotType }}　<b>鱼种：</b>{{ selected.fishTypes }}　<b>开放：</b>{{ selected.openTime }}</p>
      <p style="margin-bottom:8px; line-height:1.6">{{ selected.description }}</p>
      <div style="display:flex; gap:12px; margin:12px 0; padding:10px; background:#f9f9f9; border-radius:6px">
        <div style="flex:1">
          <div class="text-muted">📍 坐标: {{ selected.latitude }}, {{ selected.longitude }}</div>
        </div>
        <a :href="toGoogleMap(selected)" target="_blank" class="map-btn">谷歌地图 →</a>
        <a :href="toAmapLink(selected)" target="_blank" class="map-btn">高德地图 →</a>
      </div>
      <hr class="section-divider" />
      <h4 style="margin-bottom:8px">评价</h4>
      <div v-for="r in reviews" :key="r.id" style="padding:6px 0; border-bottom:1px solid #f0f0f0; font-size:13px">
        <b>{{ r.authorName }}</b> ⭐{{ r.rating }} <span style="color:#777">{{ r.content }}</span>
      </div>
      <el-empty v-if="!reviews.length" description="暂无评价" :image-size="24" />
      <div v-if="userStore.isLoggedIn" style="margin-top:12px">
        <el-rate v-model="reviewForm.rating" :max="5" />
        <el-input v-model="reviewForm.content" placeholder="写评价..." size="small" style="margin:6px 0" />
        <el-button type="primary" size="small" @click="submitReview">提交</el-button>
      </div>
    </el-dialog>

    <!-- 添加钓点 -->
    <el-dialog v-model="showAdd" title="分享钓点" width="500px">
      <el-form :model="addForm" label-position="top" size="small">
        <el-form-item label="名称"><el-input v-model="addForm.name" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="addForm.spotType" style="width:100%">
            <el-option v-for="t in ['水库','河流','湖泊','海钓','黑坑']" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="位置">
          <div style="display:flex; gap:8px; align-items:center">
            <el-input-number v-model="addForm.latitude" :precision="4" placeholder="纬度" style="flex:1" />
            <el-input-number v-model="addForm.longitude" :precision="4" placeholder="经度" style="flex:1" />
            <el-button size="small" @click="getLocation" :loading="locating">📍 定位</el-button>
          </div>
          <div v-if="locMsg" class="text-muted" style="margin-top:4px">{{ locMsg }}</div>
        </el-form-item>
        <el-form-item label="鱼种"><el-input v-model="addForm.fishTypes" placeholder="如: 鲫鱼,鲤鱼,草鱼" /></el-form-item>
        <el-form-item label="开放时间"><el-input v-model="addForm.openTime" placeholder="如: 全天开放 / 06:00-20:00" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="addForm.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="钓点图片">
          <div style="display:flex; gap:8px; align-items:center">
            <img v-if="addForm.imageUrl" :src="addForm.imageUrl" style="width:80px; height:80px; object-fit:cover; border-radius:6px" />
            <label class="img-upload-btn">
              <input type="file" accept="image/*" style="display:none" @change="uploadSpotImage" />
              {{ spotUploading ? '上传中...' : '+ 添加图片' }}
            </label>
          </div>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="showAdd=false">取消</el-button><el-button type="primary" @click="submitSpot">提交</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const userStore = useUserStore()
const spots = ref([]), keyword = ref(''), spotType = ref(''), selected = ref(null), showDetail = ref(false), showAdd = ref(false), reviews = ref([])
const reviewForm = ref({ rating: 5, content: '' })
const locating = ref(false), locMsg = ref(''), spotUploading = ref(false)
const addForm = ref({ name: '', spotType: '水库', latitude: null, longitude: null, fishTypes: '', openTime: '全天开放', description: '', imageUrl: '' })

// 地图链接
const toGoogleMap = (s) => `https://www.google.com/maps?q=${s.latitude},${s.longitude}`
const toAmapLink = (s) => `https://uri.amap.com/marker?position=${s.longitude},${s.latitude}&name=${encodeURIComponent(s.name)}`

const loadSpots = async () => { const r = await request.get('/api/spots', { params: { keyword: keyword.value, spotType: spotType.value, page: 1, size: 50 } }); if (r.code === 200) spots.value = r.data.records || [] }
const loadReviews = async (id) => { const r = await request.get(`/api/spots/${id}/reviews`); if (r.code === 200) reviews.value = r.data || [] }
const submitReview = async () => { if (!reviewForm.value.content.trim()) return; await request.post(`/api/spots/${selected.value.id}/reviews`, reviewForm.value); ElMessage.success('已评价'); reviewForm.value = { rating: 5, content: '' }; loadReviews(selected.value.id); loadSpots() }

const getLocation = () => {
  if (!navigator.geolocation) return ElMessage.warning('浏览器不支持定位')
  locating.value = true; locMsg.value = '正在获取位置...'
  navigator.geolocation.getCurrentPosition(
    (pos) => { addForm.value.latitude = parseFloat(pos.coords.latitude.toFixed(4)); addForm.value.longitude = parseFloat(pos.coords.longitude.toFixed(4)); locMsg.value = `✅ 已定位: ${addForm.value.latitude}, ${addForm.value.longitude}`; locating.value = false },
    (err) => { locMsg.value = '❌ 定位失败: ' + err.message; locating.value = false; ElMessage.error('定位失败，请手动输入坐标') },
    { enableHighAccuracy: true, timeout: 10000 }
  )
}

const uploadSpotImage = async (e) => {
  const file = e.target.files[0]; if (!file) return
  spotUploading.value = true
  const fd = new FormData(); fd.append('file', file)
  try {
    const r = await request.post('/api/upload/image', fd)
    if (r.code === 200) { addForm.value.imageUrl = r.data; ElMessage.success('图片已上传') }
  } catch (err) { ElMessage.error('上传失败') }
  spotUploading.value = false; e.target.value = ''
}

const submitSpot = async () => {
  if (!addForm.value.name) return ElMessage.warning('请输入名称')
  if (!addForm.value.latitude || !addForm.value.longitude) return ElMessage.warning('请输入位置坐标或使用定位')
  await request.post('/api/spots', addForm.value)
  ElMessage.success('钓点已分享'); showAdd.value = false
  addForm.value = { name: '', spotType: '水库', latitude: null, longitude: null, fishTypes: '', openTime: '全天开放', description: '', imageUrl: '' }
  loadSpots()
}

watch(showDetail, v => { if (v && selected.value) loadReviews(selected.value.id) })
watch(spotType, loadSpots)
onMounted(loadSpots)
</script>

<style scoped>
.spot-card { cursor: pointer; transition: box-shadow 0.15s; margin-bottom: 0; }
.spot-card:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.spots-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.spot-thumb { width: 100px; height: 75px; border-radius: 6px; object-fit: cover; flex-shrink: 0; }
.map-link { color: #1a73e8; font-size: 12px; text-decoration: none; }
.map-link:hover { text-decoration: underline; }
.map-btn { background: #1a73e8; color: #fff; padding: 4px 10px; border-radius: 4px; font-size: 12px; text-decoration: none; white-space: nowrap; }
.map-btn:hover { background: #1558b0; }
.img-upload-btn { width: 80px; height: 80px; border: 1px dashed #ccc; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-size: 12px; color: #999; cursor: pointer; text-align: center; padding: 4px; }
.img-upload-btn:hover { border-color: #1a73e8; color: #1a73e8; }
@media (max-width: 900px) { .spots-grid { grid-template-columns: 1fr; } }
</style>
