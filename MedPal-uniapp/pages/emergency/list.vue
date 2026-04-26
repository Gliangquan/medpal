<template>
  <view class="page">
    <view class="hero-card">
      <text class="hero-title">紧急联动工作台</text>
      <text class="hero-sub">查看求助、快速响应、定位患者位置</text>
      <view class="hero-stats">
        <view class="stat-item danger">
          <text class="stat-value">{{ stats.pending }}</text>
          <text class="stat-label">待响应</text>
        </view>
        <view class="stat-item primary">
          <text class="stat-value">{{ stats.responded }}</text>
          <text class="stat-label">处理中</text>
        </view>
        <view class="stat-item muted">
          <text class="stat-value">{{ stats.resolved }}</text>
          <text class="stat-label">已处理</text>
        </view>
      </view>
    </view>

    <view class="filter-row">
      <view
        v-for="item in tabs"
        :key="item.value"
        class="filter-chip"
        :class="{ active: currentStatus === item.value }"
        @tap="switchStatus(item.value)"
      >
        {{ item.label }}
      </view>
    </view>

    <view v-if="filteredList.length" class="list-panel">
      <view v-for="item in filteredList" :key="item.id" class="list-card">
        <view class="list-card-head">
          <view>
            <text class="list-card-title">求助 #{{ item.id }}</text>
            <text class="list-card-time">{{ item.createTimeText }}</text>
          </view>
          <text class="status-tag" :class="item.status">{{ item.statusText }}</text>
        </view>

        <view class="summary-row">
          <text class="severity-chip" :class="item.severityClass">{{ item.severityText }}</text>
          <text class="patient-chip">{{ item.patientName }}</text>
        </view>

        <text class="list-card-content">{{ item.contentPreview }}</text>

        <view class="summary-meta">
          <text class="summary-meta-text">{{ item.locationShort }}</text>
          <text v-if="item.responderName" class="summary-meta-text">{{ item.responderName }}</text>
        </view>

        <view class="list-actions">
          <button
            v-if="item.status === 'pending'"
            class="btn-mini danger"
            @tap.stop="respondHelp(item)"
          >响应</button>
          <button
            v-if="item.status === 'responded' && isCurrentResponder(item)"
            class="btn-mini primary"
            @tap.stop="resolveHelp(item)"
          >完成</button>
          <button class="btn-mini plain" @tap.stop="openDetail(item)">详情</button>
        </view>
      </view>
    </view>

    <view v-else class="empty-box">
      <text class="empty-title">暂无相关求助</text>
      <text class="empty-sub">当前筛选条件下没有需要处理的紧急求助</text>
    </view>

    <uni-popup ref="detailPopup" type="center" @change="handlePopupChange">
      <view class="detail-popup">
        <view class="popup-head">
          <view>
            <text class="popup-title">{{ activeDetail ? `求助 #${activeDetail.id}` : '求助详情' }}</text>
            <text v-if="activeDetail" class="popup-sub">{{ activeDetail.createTimeText }}</text>
          </view>
          <view class="popup-close" @tap="closeDetail">×</view>
        </view>

        <scroll-view v-if="activeDetail" scroll-y class="popup-scroll">
          <view class="compact-card summary-card">
            <view class="summary-top">
              <text class="patient-avatar">{{ patientInitial }}</text>
              <view class="summary-main">
                <text class="summary-name">{{ activeDetail.patientName }}</text>
                <text class="summary-desc">{{ activeDetail.patientBaseText }}</text>
              </view>
              <text class="status-tag" :class="activeDetail.status">{{ activeDetail.statusText }}</text>
            </view>
            <text class="help-text">{{ activeDetail.content || '未填写求助内容' }}</text>
          </view>

          <view class="compact-card">
            <view class="compact-row">
              <text class="compact-label">电话</text>
              <text class="compact-value">{{ activeDetail.patientPhoneMasked || '未提供' }}</text>
            </view>
            <view class="compact-row">
              <text class="compact-label">位置</text>
              <text class="compact-value multiline">{{ activeDetail.location || '未获取位置' }}</text>
            </view>
            <view class="compact-row">
              <text class="compact-label">坐标</text>
              <text class="compact-value">{{ activeDetail.coordinateText }}</text>
            </view>
            <view class="compact-row">
              <text class="compact-label">响应</text>
              <text class="compact-value">{{ activeDetail.responderName || '暂无' }}</text>
            </view>
            <view v-if="activeDetail.resolveNote" class="compact-row">
              <text class="compact-label">处理</text>
              <text class="compact-value multiline">{{ activeDetail.resolveNote }}</text>
            </view>
          </view>

          <view class="compact-card map-card">
            <!-- #ifdef H5 -->
            <view class="map-panel">
              <view id="emergency-dispatch-map" class="amap-container"></view>
              <view v-if="mapInitializing" class="map-mask">地图加载中...</view>
              <view v-else-if="mapUnavailable" class="map-mask error-mask">地图加载失败</view>
            </view>
            <!-- #endif -->

            <!-- #ifndef H5 -->
            <map
              class="mini-map"
              :latitude="detailMapLatitude"
              :longitude="detailMapLongitude"
              :scale="detailMapScale"
              :markers="detailMapMarkers"
              :show-location="true"
            />
            <!-- #endif -->

            <view class="map-actions">
              <button class="btn-secondary" :disabled="!activeDetail.hasValidCoords" @tap="openNavigation(activeDetail)">导航</button>
              <button class="btn-secondary" :disabled="!activeDetail.hasValidCoords" @tap="copyLocation(activeDetail)">复制坐标</button>
            </view>
          </view>
        </scroll-view>

        <view v-if="activeDetail" class="popup-actions">
          <button
            v-if="activeDetail.status === 'pending'"
            class="btn-primary"
            @tap="respondHelp(activeDetail)"
          >立即响应</button>
          <button
            v-if="activeDetail.status === 'responded' && isCurrentResponder(activeDetail)"
            class="btn-primary"
            @tap="resolveHelp(activeDetail)"
          >标记已处理</button>
          <button class="btn-secondary" @tap="openPatientChat(activeDetail)">联系患者</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import { emergencyApi, userApi } from '@/utils/api.js';
import { formatDateTime } from '@/utils/format.js';
import { isCompanionRole } from '@/utils/permission.js';
import { DEFAULT_CENTER, formatCoords, loadAmapSdk } from '@/utils/amap.js';

const STATUS_TEXT = {
  pending: '待响应',
  responded: '处理中',
  resolved: '已处理'
};

export default {
  data() {
    return {
      list: [],
      currentStatus: 'all',
      currentUser: null,
      activeDetail: null,
      detailMapLatitude: DEFAULT_CENTER.latitude,
      detailMapLongitude: DEFAULT_CENTER.longitude,
      detailMapScale: 15,
      detailMapMarkers: [],
      mapInitializing: false,
      mapUnavailable: false,
      h5Map: null,
      h5Marker: null,
      tabs: [
        { label: '全部', value: 'all' },
        { label: '待响应', value: 'pending' },
        { label: '处理中', value: 'responded' },
        { label: '已处理', value: 'resolved' }
      ]
    };
  },
  computed: {
    filteredList() {
      if (this.currentStatus === 'all') return this.list;
      return this.list.filter((item) => item.status === this.currentStatus);
    },
    stats() {
      return this.list.reduce(
        (acc, item) => {
          if (item.status === 'pending') acc.pending += 1;
          if (item.status === 'responded') acc.responded += 1;
          if (item.status === 'resolved') acc.resolved += 1;
          return acc;
        },
        { pending: 0, responded: 0, resolved: 0 }
      );
    },
    patientInitial() {
      return (this.activeDetail?.patientName || '患').slice(0, 1);
    }
  },
  async onShow() {
    const user = uni.getStorageSync('userInfo');
    if (!isCompanionRole(user)) {
      uni.showToast({ title: '仅陪诊员可使用紧急联动', icon: 'none' });
      uni.switchTab({ url: '/pages/index/index' });
      return;
    }
    this.currentUser = user;
    try {
      const fresh = await userApi.fetchCurrentUser();
      if (fresh?.id) {
        this.currentUser = fresh;
        uni.setStorageSync('userInfo', fresh);
      }
    } catch (error) {
      // ignore
    }
    await this.loadList();
  },
  onUnload() {
    this.destroyMap();
  },
  methods: {
    async loadList() {
      try {
        const page = await emergencyApi.list({ current: 1, size: 50 });
        const records = page?.records || [];
        const rawList = records.map((item) => this.normalizeEmergencyItem(item));
        const userIds = Array.from(new Set(rawList.map((item) => item.userId).filter(Boolean)));
        const patientMap = await this.loadPatientMap(userIds);
        this.list = rawList.map((item) => this.attachPatientInfo(item, patientMap.get(Number(item.userId))));
        if (this.activeDetail?.id) {
          const refreshed = this.list.find((item) => item.id === this.activeDetail.id);
          if (refreshed) {
            this.activeDetail = refreshed;
            this.syncDetailMap(refreshed);
          }
        }
      } catch (error) {
        uni.showToast({ title: error.message || '加载失败', icon: 'none' });
      }
    },
    normalizeEmergencyItem(item) {
      const latitude = this.toNumber(item.latitude);
      const longitude = this.toNumber(item.longitude);
      return {
        ...item,
        latitude,
        longitude,
        hasValidCoords: Number.isFinite(latitude) && Number.isFinite(longitude),
        statusText: STATUS_TEXT[item.status] || '未知',
        createTimeText: formatDateTime(item.createTime) || '刚刚',
        resolveTimeText: formatDateTime(item.resolveTime) || '',
        responderName: item.responderId
          ? Number(item.responderId) === Number(this.currentUser?.id)
            ? '我'
            : `陪诊员 #${item.responderId}`
          : '',
        locationShort: item.location ? this.truncate(item.location, 18) : '未获取位置',
        coordinateText: Number.isFinite(latitude) && Number.isFinite(longitude)
          ? formatCoords(latitude, longitude)
          : '无有效坐标',
        contentPreview: item.content ? this.truncate(item.content, 34) : '未填写求助内容',
        severityText: this.getSeverityText(item.content),
        severityClass: this.getSeverityClass(item.content)
      };
    },
    async loadPatientMap(userIds) {
      const map = new Map();
      await Promise.all(userIds.map(async (id) => {
        try {
          const data = await userApi.getUserVOById(id);
          if (data?.id) {
            map.set(Number(id), data);
          }
        } catch (error) {
          map.set(Number(id), null);
        }
      }));
      return map;
    },
    attachPatientInfo(item, patient) {
      const patientName = patient?.userName || `患者 #${item.userId}`;
      const patientPhone = patient?.userPhone || '';
      const patientBaseText = [patient?.gender, patient?.age ? `${patient.age}岁` : ''].filter(Boolean).join(' · ') || '基础信息未完善';
      return {
        ...item,
        patientName,
        patientPhone,
        patientPhoneMasked: this.maskPhone(patientPhone),
        patientBaseText,
        patientMedicalHistory: patient?.medicalHistory || '',
        patientRaw: patient || null
      };
    },
    switchStatus(status) {
      this.currentStatus = status;
    },
    openDetail(item) {
      this.activeDetail = item;
      this.$refs.detailPopup.open();
      this.$nextTick(() => {
        this.syncDetailMap(item);
        this.initializeMap();
      });
    },
    closeDetail() {
      this.$refs.detailPopup.close();
    },
    handlePopupChange(event) {
      if (!event.show) {
        this.destroyMap();
      }
    },
    resetDetailMap() {
      this.detailMapLatitude = DEFAULT_CENTER.latitude;
      this.detailMapLongitude = DEFAULT_CENTER.longitude;
      this.detailMapMarkers = [];
    },
    syncDetailMap(item) {
      if (!item?.hasValidCoords) {
        this.resetDetailMap();
        return;
      }
      this.detailMapLatitude = item.latitude;
      this.detailMapLongitude = item.longitude;
      this.detailMapMarkers = [
        {
          id: 1,
          latitude: item.latitude,
          longitude: item.longitude,
          width: 26,
          height: 34
        }
      ];
      // #ifdef H5
      if (this.h5Marker) {
        this.h5Marker.setPosition([item.longitude, item.latitude]);
      }
      if (this.h5Map) {
        this.h5Map.setZoomAndCenter(this.detailMapScale, [item.longitude, item.latitude]);
      }
      // #endif
    },
    async initializeMap() {
      // #ifdef H5
      if (!this.activeDetail) return;
      if (this.h5Map || this.mapInitializing) {
        this.syncDetailMap(this.activeDetail);
        return;
      }
      this.mapInitializing = true;
      this.mapUnavailable = false;
      try {
        const AMap = await loadAmapSdk();
        if (!AMap) {
          throw new Error('高德地图未就绪');
        }
        this.h5Map = new AMap.Map('emergency-dispatch-map', {
          zoom: this.detailMapScale,
          center: [this.detailMapLongitude, this.detailMapLatitude],
          viewMode: '2D'
        });
        this.h5Marker = new AMap.Marker({
          position: [this.detailMapLongitude, this.detailMapLatitude],
          anchor: 'bottom-center'
        });
        this.h5Map.add(this.h5Marker);
        this.syncDetailMap(this.activeDetail);
      } catch (error) {
        this.mapUnavailable = true;
      } finally {
        this.mapInitializing = false;
      }
      // #endif
    },
    destroyMap() {
      // #ifdef H5
      if (this.h5Map && typeof this.h5Map.destroy === 'function') {
        this.h5Map.destroy();
      }
      this.h5Map = null;
      this.h5Marker = null;
      this.mapInitializing = false;
      this.mapUnavailable = false;
      // #endif
    },
    isCurrentResponder(item) {
      return Number(item.responderId) === Number(this.currentUser?.id);
    },
    async respondHelp(item) {
      uni.showModal({
        title: '确认响应',
        content: '确认接收这条求助？',
        success: async (res) => {
          if (!res.confirm) return;
          try {
            await emergencyApi.respond(item.id);
            uni.showToast({ title: '响应成功', icon: 'success' });
            await this.loadList();
            const refreshed = this.list.find((current) => current.id === item.id);
            if (refreshed && this.activeDetail?.id === item.id) {
              this.activeDetail = refreshed;
            }
          } catch (error) {
            uni.showToast({ title: error.message || '响应失败', icon: 'none' });
          }
        }
      });
    },
    async resolveHelp(item) {
      uni.showModal({
        title: '标记已处理',
        editable: true,
        placeholderText: '填写简短处理说明',
        success: async (res) => {
          if (!res.confirm) return;
          try {
            await emergencyApi.resolve(item.id, res.content || '已联系并处理');
            uni.showToast({ title: '已处理', icon: 'success' });
            await this.loadList();
            const refreshed = this.list.find((current) => current.id === item.id);
            if (refreshed && this.activeDetail?.id === item.id) {
              this.activeDetail = refreshed;
            }
          } catch (error) {
            uni.showToast({ title: error.message || '处理失败', icon: 'none' });
          }
        }
      });
    },
    openNavigation(item) {
      if (!item?.hasValidCoords) return;
      uni.openLocation({
        latitude: item.latitude,
        longitude: item.longitude,
        name: item.patientName || '患者位置',
        address: item.location || '患者位置',
        scale: 18
      });
    },
    openPatientChat(item) {
      if (!item?.userId) {
        uni.showToast({ title: '未找到患者信息', icon: 'none' });
        return;
      }
      const peerName = encodeURIComponent(item.patientName || '患者');
      const orderId = item.orderId ? `orderId=${item.orderId}&` : '';
      uni.navigateTo({
        url: `/pages/chat/index?${orderId}peerId=${item.userId}&peerName=${peerName}`
      });
    },
    copyLocation(item) {
      if (!item?.hasValidCoords) return;
      uni.setClipboardData({
        data: `${item.latitude},${item.longitude}`,
        success: () => uni.showToast({ title: '坐标已复制', icon: 'none' })
      });
    },
    truncate(value, length = 20) {
      const text = String(value || '');
      return text.length > length ? `${text.slice(0, length)}...` : text;
    },
    maskPhone(phone) {
      const text = String(phone || '');
      if (text.length !== 11) return text || '';
      return `${text.slice(0, 3)}****${text.slice(7)}`;
    },
    toNumber(value) {
      const num = Number(value);
      return Number.isFinite(num) ? num : null;
    },
    getSeverityText(content) {
      const text = String(content || '');
      if (/昏迷|骨折|出血|胸痛|呼吸|休克|抽搐/.test(text)) return '高优先级';
      if (/头晕|跌倒|疼痛|不适|药物/.test(text)) return '中优先级';
      return '普通优先级';
    },
    getSeverityClass(content) {
      const text = String(content || '');
      if (/昏迷|骨折|出血|胸痛|呼吸|休克|抽搐/.test(text)) return 'high';
      if (/头晕|跌倒|疼痛|不适|药物/.test(text)) return 'medium';
      return 'low';
    }
  }
};
</script>

<style lang="scss">
.page {
  min-height: 100vh;
  padding: 24rpx;
  background: #f4f6fb;
}

.hero-card {
  background: linear-gradient(145deg, #fff0f0, #fff9f9);
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: 0 12rpx 28rpx rgba(200, 59, 59, 0.08);
}

.hero-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #c83b3b;
}

.hero-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #8c6262;
}

.hero-stats {
  display: flex;
  gap: 12rpx;
  margin-top: 18rpx;
}

.stat-item {
  flex: 1;
  padding: 16rpx;
  border-radius: 18rpx;
  background: #ffffff;
}

.stat-item.danger { background: rgba(255, 92, 92, 0.12); }
.stat-item.primary { background: rgba(47, 101, 249, 0.10); }
.stat-item.muted { background: rgba(111, 122, 141, 0.10); }

.stat-value {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.stat-label {
  display: block;
  margin-top: 6rpx;
  font-size: 21rpx;
  color: #6c7385;
}

.filter-row {
  margin-top: 18rpx;
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}

.filter-chip {
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  background: #ffffff;
  color: #6c7385;
  font-size: 22rpx;
  border: 1rpx solid #e2e7f2;
}

.filter-chip.active {
  color: #ffffff;
  background: #ff5c5c;
  border-color: #ff5c5c;
}

.list-panel {
  margin-top: 18rpx;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.list-card,
.compact-card {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 18rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.list-card-head,
.summary-row,
.list-actions,
.popup-head,
.summary-top,
.compact-row,
.map-actions,
.popup-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.list-card-title,
.popup-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.list-card-time,
.popup-sub,
.summary-meta-text,
.compact-label {
  display: block;
  margin-top: 6rpx;
  font-size: 21rpx;
  color: #8d97a9;
}

.status-tag {
  font-size: 22rpx;
  font-weight: 600;
}

.status-tag.pending { color: #ff8a00; }
.status-tag.responded { color: #2f65f9; }
.status-tag.resolved { color: #6f7a8d; }

.severity-chip,
.patient-chip {
  display: inline-flex;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
  font-weight: 600;
}

.severity-chip.high { background: rgba(255, 92, 92, 0.12); color: #ff5c5c; }
.severity-chip.medium { background: rgba(255, 138, 0, 0.12); color: #ff8a00; }
.severity-chip.low,
.patient-chip { background: rgba(47, 101, 249, 0.10); color: #2f65f9; }

.list-card-content,
.info-text,
.help-text,
.compact-value,
.summary-name,
.summary-desc {
  display: block;
  color: #1c2b4a;
}

.list-card-content,
.help-text,
.compact-value {
  margin-top: 10rpx;
  font-size: 23rpx;
  line-height: 1.6;
}

.summary-meta {
  margin-top: 12rpx;
  display: flex;
  justify-content: space-between;
  gap: 12rpx;
}

.summary-meta-text {
  margin-top: 0;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12rpx;
  margin-top: 14rpx;
}

.info-box {
  padding: 14rpx;
  border-radius: 16rpx;
  background: #f8faff;
}

.info-label {
  font-size: 21rpx;
  color: #8d97a9;
}

.responder-row {
  margin-top: 12rpx;
  display: flex;
  justify-content: space-between;
}

.responder-text {
  font-size: 22rpx;
  color: #2f65f9;
}

.btn-mini,
.btn-primary,
.btn-secondary {
  border-radius: 999rpx;
  font-size: 22rpx;
}

.list-actions,
.map-actions,
.popup-actions {
  gap: 12rpx;
  margin-top: 16rpx;
}

.btn-mini {
  flex: 1;
  padding: 0 18rpx;
  line-height: 64rpx;
  background: #ffffff;
}

.btn-mini.danger,
.btn-primary { background: #ff5c5c; color: #ffffff; }
.btn-mini.primary { background: #2f65f9; color: #ffffff; }
.btn-mini.plain,
.btn-secondary { background: #ffffff; color: #2f65f9; border: 1rpx solid rgba(47, 101, 249, 0.25); }

.empty-box {
  margin-top: 180rpx;
  text-align: center;
}

.empty-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.empty-sub {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: #8d97a9;
}

.detail-popup {
  width: 680rpx;
  max-height: 82vh;
  background: #f4f6fb;
  border-radius: 24rpx;
  overflow: hidden;
}

.popup-head {
  padding: 20rpx 20rpx 12rpx;
  background: #ffffff;
  border-bottom: 1rpx solid #eef2f8;
}

.popup-close {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  background: #f4f6fb;
  color: #6c7385;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
}

.popup-scroll {
  max-height: calc(82vh - 148rpx);
  padding: 14rpx;
}

.summary-card {
  padding-bottom: 14rpx;
}

.patient-avatar {
  width: 68rpx;
  height: 68rpx;
  border-radius: 50%;
  background: linear-gradient(145deg, #ff5c5c, #ff8d8d);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  font-weight: 700;
}

.summary-main {
  flex: 1;
  margin-left: 14rpx;
  margin-right: 12rpx;
}

.summary-name {
  font-size: 26rpx;
  font-weight: 700;
}

.summary-desc {
  margin-top: 6rpx;
  font-size: 21rpx;
  color: #8d97a9;
}

.compact-row {
  padding: 10rpx 0;
  border-bottom: 1rpx solid #eef2f8;
  align-items: flex-start;
}

.compact-row:last-child {
  border-bottom: none;
}

.compact-label {
  margin-top: 0;
  min-width: 72rpx;
}

.compact-value {
  margin-top: 0;
  max-width: 78%;
  text-align: right;
}

.compact-value.multiline {
  line-height: 1.5;
}

.map-panel,
.mini-map {
  width: 100%;
  height: 240rpx;
  border-radius: 16rpx;
  overflow: hidden;
  background: #f7f8fc;
}

.map-panel {
  position: relative;
}

.amap-container {
  width: 100%;
  height: 100%;
}

.map-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  color: #8b5c5c;
  background: rgba(255, 255, 255, 0.72);
}

.error-mask { color: #ff5c5c; }

.popup-actions {
  padding: 14rpx 20rpx 20rpx;
  background: #ffffff;
  border-top: 1rpx solid #eef2f8;
}
</style>
