<template>
  <div class="page-shell spot-page">
    <section class="hero-panel spot-hero">
      <p class="eyebrow">Fishing Spots</p>
      <h1 class="hero-title">找钓点，避开禁钓区。</h1>
      <p class="hero-subtitle">按类型、鱼种和评价发现附近值得尝试的水域。</p>
    </section>
    <div class="page-toolbar spot-toolbar">
      <el-input v-model="keyword" placeholder="搜索钓点" clearable size="small" @keyup.enter="loadSpots" />
      <el-select v-model="spotType" placeholder="类型" clearable size="small">
        <el-option v-for="t in ['水库','河流','湖泊','海钓','黑坑']" :key="t" :label="t" :value="t" />
      </el-select>
      <el-button type="primary" size="small" @click="loadSpots">搜索</el-button>
      <el-button type="primary" size="small" @click="openAddSpot">分享钓点</el-button>
    </div>
    <div class="spot-layout">
      <div class="map-panel content-card">
        <div class="map-watermark">钓点地图</div>
        <button v-for="(s, index) in spots.slice(0, 8)" :key="s.id" type="button" class="map-pin" :class="{ active: selected?.id === s.id }" :style="pinStyle(index)" @click="selected=s; showDetail=true">
          <span></span>
        </button>
        <div class="map-summary">
          <b>{{ selected?.name || '选择一个钓点查看详情' }}</b>
          <p>{{ selected ? `${selected.spotType || '钓点'} · ${selected.fishTypes || '鱼种待补充'}` : `当前收录 ${spots.length} 个钓点` }}</p>
        </div>
      </div>
      <div class="spots-grid">
        <article
          v-for="s in spots"
          :key="s.id"
          class="card spot-card"
          role="button"
          tabindex="0"
          @click="selected=s; showDetail=true"
          @keydown.enter.space.prevent="selected=s; showDetail=true"
        >
          <div class="spot-card-body">
            <img v-if="s.imageUrl" :src="s.imageUrl" class="spot-thumb" loading="lazy" />
            <div class="spot-info">
              <div class="spot-heading">
                <b>{{ s.name }}</b>
                <el-tag size="small">{{ s.spotType }}</el-tag>
              </div>
              <p class="spot-desc">{{ s.description?.substring(0,80) }}</p>
              <div class="spot-meta">
                <span>{{ s.fishTypes || '鱼种待补充' }}</span>
                <span>评分 {{ s.rating || 0 }}</span>
                <span>{{ s.openTime || '开放时间待补充' }}</span>
                <span v-if="s.bestSeason">{{ s.bestSeason }}</span>
                <span v-if="s.feeInfo">{{ s.feeInfo }}</span>
              </div>
              <div v-if="s.noFishingNotice" class="warning-line">禁钓提醒：{{ s.noFishingNotice }}</div>
              <div class="spot-links">
                <a :href="toGoogleMap(s)" target="_blank" rel="noopener noreferrer" @click.stop class="map-link">谷歌地图</a>
                <a :href="toAmapLink(s)" target="_blank" rel="noopener noreferrer" @click.stop class="map-link">高德地图</a>
              </div>
            </div>
          </div>
        </article>
        <div v-if="loadError && !spots.length" class="card desktop-error-card">
          <div>
            <b>钓点加载失败</b>
            <p class="text-muted">后端暂时不可用，刷新或稍后重试。</p>
          </div>
          <el-button size="small" type="primary" @click="loadSpots">重新加载</el-button>
        </div>
        <div v-else-if="!spots.length" class="card empty-state">
          <div class="empty-state-icon">📍</div>
          <div class="empty-state-title">附近还没收录钓点</div>
          <div class="empty-state-text">分享你常去的水域，让钓友们少走弯路。</div>
          <el-button type="primary" size="small" round @click="openAddSpot">分享第一个钓点</el-button>
        </div>
      </div>
    </div>
    <el-dialog v-model="showDetail" :title="selected?.name" width="min(92vw, 620px)" class="responsive-dialog" v-if="selected">
      <div class="dialog-form-scroll spot-detail-dialog">
        <img v-if="selected.imageUrl" :src="selected.imageUrl" class="spot-detail-image" />
        <div class="spot-detail-meta">
          <span><b>类型：</b>{{ selected.spotType }}</span>
          <span><b>鱼种：</b>{{ selected.fishTypes }}</span>
          <span><b>开放：</b>{{ selected.openTime }}</span>
          <span><b>适钓季节：</b>{{ selected.bestSeason || '未填写' }}</span>
          <span><b>收费：</b>{{ selected.feeInfo || '未填写' }}</span>
        </div>
        <el-alert v-if="selected.noFishingNotice" type="warning" :closable="false" class="spot-alert" :title="`禁钓提醒：${selected.noFishingNotice}`" />
        <p class="spot-detail-desc">{{ selected.description }}</p>
        <div class="spot-coordinate-card">
          <div class="text-muted">坐标: {{ selected.latitude }}, {{ selected.longitude }}</div>
          <a :href="toGoogleMap(selected)" target="_blank" rel="noopener noreferrer" class="map-btn">谷歌地图</a>
          <a :href="toAmapLink(selected)" target="_blank" rel="noopener noreferrer" class="map-btn">高德地图</a>
        </div>
        <el-button v-if="userStore.isLoggedIn" size="small" @click="toggleSpotFavorite(selected)">收藏/取消收藏钓点</el-button>
        <hr class="section-divider" />
        <h4 class="review-title">评价</h4>
        <div v-for="r in reviews" :key="r.id" class="review-row">
          <b>{{ r.authorName }}</b><span>评分 {{ r.rating }}</span><p>{{ r.content }}</p>
        </div>
        <el-empty v-if="!reviews.length" description="暂无评价" :image-size="24" />
        <div v-if="userStore.isLoggedIn" class="review-form">
          <el-rate v-model="reviewForm.rating" :max="5" />
          <el-input v-model="reviewForm.content" placeholder="写评价..." size="small" />
          <el-button type="primary" size="small" @click="submitReview">提交</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 添加钓点 -->
    <el-dialog v-model="showAdd" title="分享钓点" width="min(92vw, 560px)" class="responsive-dialog">
      <el-form :model="addForm" label-position="top" size="small" class="dialog-form-scroll">
        <el-form-item label="名称"><el-input v-model="addForm.name" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="addForm.spotType" style="width:100%">
            <el-option v-for="t in ['水库','河流','湖泊','海钓','黑坑']" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="位置">
          <div class="coordinate-inputs">
            <el-input-number v-model="addForm.latitude" :precision="4" placeholder="纬度" />
            <el-input-number v-model="addForm.longitude" :precision="4" placeholder="经度" />
            <el-button size="small" @click="getLocation" :loading="locating">定位</el-button>
          </div>
          <div v-if="locMsg" class="text-muted loc-message">{{ locMsg }}</div>
        </el-form-item>
        <el-form-item label="鱼种"><el-input v-model="addForm.fishTypes" placeholder="如: 鲫鱼,鲤鱼,草鱼" /></el-form-item>
        <el-form-item label="开放时间"><el-input v-model="addForm.openTime" placeholder="如: 全天开放 / 06:00-20:00" /></el-form-item>
        <el-form-item label="适钓季节"><el-input v-model="addForm.bestSeason" placeholder="如: 春秋最佳 / 夏季夜钓" /></el-form-item>
        <el-form-item label="收费信息"><el-input v-model="addForm.feeInfo" placeholder="如: 免费 / 50元每天 / 按竿收费" /></el-form-item>
        <el-form-item label="禁钓提醒"><el-input v-model="addForm.noFishingNotice" placeholder="如: 繁殖期禁钓 / 汛期危险" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="addForm.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="钓点图片">
          <div class="spot-upload-row">
            <img v-if="addForm.imageUrl" :src="addForm.imageUrl" class="spot-upload-preview" />
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
const loadError = ref(false)
const reviewForm = ref({ rating: 5, content: '' })
const locating = ref(false), locMsg = ref(''), spotUploading = ref(false)
const emptySpotForm = () => ({ name: '', spotType: '水库', latitude: null, longitude: null, fishTypes: '', openTime: '全天开放', bestSeason: '', feeInfo: '', noFishingNotice: '', description: '', imageUrl: '' })
const addForm = ref(emptySpotForm())

// 地图链接
const toGoogleMap = (s) => `https://www.google.com/maps?q=${s.latitude},${s.longitude}`
const toAmapLink = (s) => `https://uri.amap.com/marker?position=${s.longitude},${s.latitude}&name=${encodeURIComponent(s.name)}`
const pinStyle = (index) => ({
  left: `${18 + ((index * 23) % 62)}%`,
  top: `${22 + ((index * 17) % 50)}%`
})

const requireLogin = () => {
  if (userStore.isLoggedIn) return true
  ElMessage.warning('请先登录后再执行此操作')
  return false
}

const openAddSpot = () => {
  if (!requireLogin()) return
  showAdd.value = true
}

const loadSpots = async () => {
  loadError.value = false
  try {
    const r = await request.get('/api/spots', { params: { keyword: keyword.value, spotType: spotType.value, page: 1, size: 50 } })
    if (r.code === 200) spots.value = r.data.records || []
  } catch (e) {
    loadError.value = true
  }
}
const loadReviews = async (id) => { const r = await request.get(`/api/spots/${id}/reviews`); if (r.code === 200) reviews.value = r.data || [] }
const submitReview = async () => {
  if (!requireLogin()) return
  if (!reviewForm.value.content.trim()) return
  await request.post(`/api/spots/${selected.value.id}/reviews`, reviewForm.value)
  ElMessage.success('已评价')
  reviewForm.value = { rating: 5, content: '' }
  loadReviews(selected.value.id)
  loadSpots()
}

const toggleSpotFavorite = async (spot) => {
  if (!requireLogin()) return
  const r = await request.post(`/api/spots/${spot.id}/favorite`)
  if (r.code === 200) ElMessage.success(r.data || r.message)
}

const getLocation = () => {
  if (!navigator.geolocation) return ElMessage.warning('浏览器不支持定位')
  locating.value = true; locMsg.value = '正在获取位置...'
  navigator.geolocation.getCurrentPosition(
    (pos) => { addForm.value.latitude = parseFloat(pos.coords.latitude.toFixed(4)); addForm.value.longitude = parseFloat(pos.coords.longitude.toFixed(4)); locMsg.value = `已定位: ${addForm.value.latitude}, ${addForm.value.longitude}`; locating.value = false },
    (err) => { locMsg.value = '定位失败: ' + err.message; locating.value = false; ElMessage.error('定位失败，请手动输入坐标') },
    { enableHighAccuracy: true, timeout: 10000 }
  )
}

const uploadSpotImage = async (e) => {
  if (!requireLogin()) { e.target.value = ''; return }
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
  if (!requireLogin()) return
  if (!addForm.value.name) return ElMessage.warning('请输入名称')
  if (!addForm.value.latitude || !addForm.value.longitude) return ElMessage.warning('请输入位置坐标或使用定位')
  await request.post('/api/spots', addForm.value)
  ElMessage.success('钓点已分享'); showAdd.value = false
  addForm.value = emptySpotForm()
  loadSpots()
}

watch(showDetail, v => { if (v && selected.value) loadReviews(selected.value.id) })
watch(spotType, loadSpots)
onMounted(loadSpots)
</script>

<style scoped>
.spot-page { min-width: 0; }
.spot-hero { margin-bottom: 14px; }
.spot-toolbar .el-input { width: 220px; }
.spot-toolbar .el-select { width: 120px; }
.spot-layout { display: grid; grid-template-columns: minmax(360px, .72fr) minmax(0, 1fr); gap: 16px; align-items: start; }
.map-panel { position: sticky; top: 82px; min-height: 430px; overflow: hidden; background:
  linear-gradient(90deg, rgba(79,127,191,.08) 1px, transparent 1px),
  linear-gradient(0deg, rgba(79,127,191,.08) 1px, transparent 1px),
  radial-gradient(circle at 30% 24%, rgba(47,143,107,.18), transparent 18%),
  radial-gradient(circle at 72% 68%, rgba(79,127,191,.18), transparent 24%),
  linear-gradient(135deg, #f8fbff, #edf6ff);
  background-size: 42px 42px, 42px 42px, auto, auto, auto;
  border-radius: var(--radius-card);
  padding: 18px;
}
.map-watermark { color: rgba(54,95,147,.14); font-size: 46px; font-weight: 900; letter-spacing: -.04em; }
.map-pin { position: absolute; width: 30px; height: 30px; border: 0; border-radius: 50% 50% 50% 8px; transform: rotate(-45deg); background: var(--color-primary); box-shadow: 0 10px 22px rgba(54,95,147,.26); cursor: pointer; }
.map-pin span { position: absolute; inset: 8px; border-radius: 50%; background: #fff; }
.map-pin.active { background: var(--color-warning); }
.map-summary { position: absolute; left: 18px; right: 18px; bottom: 18px; padding: 14px; border: 1px solid rgba(255,255,255,.72); border-radius: 16px; background: rgba(255,255,255,.86); backdrop-filter: blur(10px); }
.map-summary b { color: var(--ink); }
.map-summary p { margin-top: 4px; color: var(--muted); font-size: 13px; }
.spots-grid { display: grid; grid-template-columns: 1fr; gap: 12px; }
.spot-card { cursor: pointer; transition: box-shadow 0.15s; margin-bottom: 0; border-color: var(--border-subtle); }
.spot-card:hover { box-shadow: var(--shadow-hover); }
.spot-card-body { display: flex; gap: 12px; min-width: 0; }
.spot-thumb { width: 112px; height: 84px; border-radius: 12px; object-fit: cover; flex-shrink: 0; aspect-ratio: 4 / 3; background: var(--surface-muted); }
.spot-info { flex: 1; min-width: 0; }
.spot-heading { display: flex; justify-content: space-between; align-items: center; gap: 10px; }
.spot-heading b { color: var(--ink); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-weight: 700; }
.spot-desc { font-size: 13px; color: var(--muted); margin: 5px 0; line-height: 1.5; }
.spot-meta { font-size: 12px; color: var(--muted); display: flex; gap: 6px; flex-wrap: wrap; }
.spot-meta span { padding: 4px 10px; border-radius: 999px; background: var(--surface-muted); border: 1px solid var(--border-subtle); min-height: 22px; display: inline-flex; align-items: center; }
.map-link { color: var(--color-primary); font-size: 12px; text-decoration: none; }
.map-link:hover { color: var(--color-primary-dark); }
.spot-links { font-size: 12px; margin-top: 6px; display: flex; gap: 10px; }
.map-btn { background: var(--color-primary); color: #fff; padding: 5px 10px; border-radius: 999px; font-size: 12px; text-decoration: none; white-space: nowrap; }
.map-btn:hover { background: var(--color-primary-dark); color: #fff; }
.warning-line { font-size: 12px; color: var(--color-warning); margin-top: 6px; line-height: 1.4; }
.spot-detail-image { width: 100%; max-height: 260px; object-fit: cover; border-radius: 14px; margin-bottom: 12px; }
.spot-detail-meta { display: flex; flex-wrap: wrap; gap: 8px 14px; margin-bottom: 10px; color: #4b5563; font-size: 13px; }
.spot-alert { margin-bottom: 10px; }
.spot-detail-desc { margin-bottom: 10px; line-height: 1.7; color: #374151; }
.spot-coordinate-card { display: flex; align-items: center; gap: 10px; margin: 12px 0; padding: 12px; background: var(--surface-muted); border-radius: 14px; }
.spot-coordinate-card .text-muted { flex: 1; }
.review-title { margin-bottom: 8px; color: var(--ink); }
.review-row { padding: 8px 0; border-bottom: 1px solid #edf2f7; font-size: 13px; display: grid; gap: 2px; }
.review-row span { color: var(--color-warning); font-size: 12px; }
.review-row p { color: #4b5563; }
.review-form { margin-top: 12px; display: grid; gap: 8px; }
.coordinate-inputs { display: grid; grid-template-columns: 1fr 1fr auto; gap: 8px; align-items: center; }
.coordinate-inputs .el-input-number { width: 100%; }
.loc-message { margin-top: 4px; }
.spot-upload-row { display: flex; gap: 8px; align-items: center; }
.spot-upload-preview { width: 80px; height: 80px; object-fit: cover; border-radius: 10px; }
.img-upload-btn { width: 80px; height: 80px; border: 1px dashed #cbd5e1; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 12px; color: var(--muted); cursor: pointer; text-align: center; padding: 4px; }
.img-upload-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
@media (max-width: 900px) {
  .spot-toolbar { align-items: stretch; }
  .spot-toolbar .el-input, .spot-toolbar .el-select { width: 100%; }
  .spot-layout { grid-template-columns: 1fr; }
  .map-panel { position: relative; top: auto; min-height: 280px; }
}
@media (max-width: 520px) {
  .spot-card-body { flex-direction: column; }
  .spot-thumb { width: 100%; height: 150px; }
  .spot-coordinate-card, .coordinate-inputs { grid-template-columns: 1fr; display: grid; }
}

</style>
