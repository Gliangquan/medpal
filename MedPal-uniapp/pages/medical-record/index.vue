<template>
  <view class="page">
    <view class="hero-card">
      <view class="hero-head">
        <view>
          <text class="hero-title">我的电子病历</text>
          <text class="hero-subtitle">最近就诊：{{ latestVisitText }}</text>
        </view>
        <view class="refresh-chip" @tap="loadRecords(true)">
          <image class="chip-icon" src="/static/icon_med/shuaxing.png" mode="aspectFit" />
          <text class="chip-text">刷新</text>
        </view>
      </view>
      <view class="stats-row">
        <view class="stat-item">
          <text class="stat-value">{{ records.length }}</text>
          <text class="stat-label">病历总数</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ thisYearCount }}</text>
          <text class="stat-label">本年就诊</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ withPrescriptionCount }}</text>
          <text class="stat-label">含处方</text>
        </view>
      </view>
    </view>

    <view class="filter-card">
      <uni-easyinput
        v-model="keyword"
        trim
        :clearable="true"
        placeholder="搜索医院、科室、医生或诊断"
      />
      <view class="filter-row">
        <uni-segmented-control
          :values="rangeTabs"
          :current="currentRange"
          style-type="button"
          active-color="#2f65f9"
          @clickItem="changeRange"
        />
      </view>
      <view class="sort-row">
        <text class="sort-label">共 {{ filteredRecords.length }} 条符合条件</text>
        <view class="sort-btn" @tap="toggleSort">
          <image class="sort-icon" src="/static/icon_med/rili.png" mode="aspectFit" />
          <text>{{ sortNewest ? '最新在前' : '最早在前' }}</text>
        </view>
      </view>
    </view>

    <view v-if="filteredRecords.length" class="list">
      <view v-for="item in filteredRecords" :key="item.id" class="record-card" @tap="goDetail(item.id)">
        <view class="record-head">
          <view class="record-title-wrap">
            <image class="record-icon" src="/static/icon_med/bingli.png" mode="aspectFit" />
            <view>
              <text class="hospital">{{ item.hospitalName }}</text>
              <text class="meta">{{ item.departmentName }} · {{ item.doctorName }}</text>
            </view>
          </view>
          <text class="date">{{ item.visitDateText }}</text>
        </view>
        <view class="record-body">
          <text class="label">诊断</text>
          <text class="value">{{ item.diagnosis || '暂无诊断描述' }}</text>
        </view>
        <view class="tag-row">
          <uni-tag v-if="item.checkResults" text="有检查结果" size="small" type="primary" />
          <uni-tag v-if="item.prescription" text="有处方" size="small" type="success" />
          <uni-tag v-if="item.treatment" text="有治疗方案" size="small" type="warning" />
          <uni-tag v-if="item.doctorAdvice" text="有医嘱" size="small" type="error" />
        </view>
        <view class="record-footer">
          <text class="record-no">病历号：{{ item.recordNo || '未提供' }}</text>
          <text class="detail-link">查看详情</text>
        </view>
      </view>
    </view>

    <view v-else class="empty">
      <image class="empty-icon" src="/static/icon_med/bingli.png" mode="aspectFit" />
      <text class="empty-title">暂无符合条件的病历</text>
      <text class="empty-subtitle">你可以调整筛选条件，或完成一次就诊后自动生成病历</text>
      <button class="btn-primary empty-btn" @tap="goOrder">去查看订单</button>
    </view>
  </view>
</template>

<script>
import { recordApi } from '@/utils/api.js';
import { formatDate } from '@/utils/format.js';
import { isPatientRole } from '@/utils/permission.js';

const RANGE_TABS = ['全部', '近3个月', '近1年'];
const THREE_MONTH_MS = 1000 * 60 * 60 * 24 * 90;
const ONE_YEAR_MS = 1000 * 60 * 60 * 24 * 365;

export default {
  data() {
    return {
      records: [],
      keyword: '',
      currentRange: 0,
      sortNewest: true,
      rangeTabs: RANGE_TABS
    };
  },
  computed: {
    latestVisitText() {
      if (!this.records.length) return '暂无记录';
      return this.records[0].visitDateText || '暂无记录';
    },
    thisYearCount() {
      const year = new Date().getFullYear();
      return this.records.filter((item) => item.visitYear === year).length;
    },
    withPrescriptionCount() {
      return this.records.filter((item) => Boolean(item.prescription)).length;
    },
    filteredRecords() {
      let list = [...this.records];
      const keyword = (this.keyword || '').trim().toLowerCase();
      if (keyword) {
        list = list.filter((item) => {
          const text = `${item.hospitalName} ${item.departmentName} ${item.doctorName} ${item.diagnosis}`.toLowerCase();
          return text.includes(keyword);
        });
      }

      const now = Date.now();
      if (this.currentRange === 1) {
        list = list.filter((item) => item.visitTimestamp && now - item.visitTimestamp <= THREE_MONTH_MS);
      } else if (this.currentRange === 2) {
        list = list.filter((item) => item.visitTimestamp && now - item.visitTimestamp <= ONE_YEAR_MS);
      }

      list.sort((a, b) => {
        if (this.sortNewest) return (b.visitTimestamp || 0) - (a.visitTimestamp || 0);
        return (a.visitTimestamp || 0) - (b.visitTimestamp || 0);
      });
      return list;
    }
  },
  onShow() {
    const user = uni.getStorageSync('userInfo');
    if (!isPatientRole(user)) {
      uni.showToast({ title: '仅患者可查看病历', icon: 'none' });
      uni.switchTab({ url: '/pages/order/index' });
      return;
    }
    this.loadRecords();
  },
  onPullDownRefresh() {
    this.loadRecords().finally(() => uni.stopPullDownRefresh());
  },
  methods: {
    normalizeRecord(item) {
      const visitDateText = formatDate(item.visitDate) || item.visitDate || '-';
      const parsed = new Date(item.visitDate).getTime();
      return {
        ...item,
        hospitalName: item.hospitalName || '未填写医院',
        departmentName: item.departmentName || '未填写科室',
        doctorName: item.doctorName || '未填写医生',
        diagnosis: item.diagnosis || '',
        visitDateText,
        visitTimestamp: Number.isNaN(parsed) ? 0 : parsed,
        visitYear: Number.isNaN(parsed) ? 0 : new Date(parsed).getFullYear()
      };
    },
    async loadRecords(showToast = false) {
      const user = uni.getStorageSync('userInfo');
      if (!user?.id) {
        this.records = [];
        return;
      }
      try {
        const page = await recordApi.list({ userId: user.id, current: 1, size: 100 });
        const list = (page.records || []).map((item) => this.normalizeRecord(item));
        list.sort((a, b) => (b.visitTimestamp || 0) - (a.visitTimestamp || 0));
        this.records = list;
        if (showToast) {
          uni.showToast({ title: '病历已更新', icon: 'success' });
        }
      } catch (error) {
        uni.showToast({ title: error.message || '加载病历失败', icon: 'none' });
      }
    },
    changeRange(e) {
      this.currentRange = e.currentIndex;
    },
    toggleSort() {
      this.sortNewest = !this.sortNewest;
    },
    goDetail(id) {
      uni.navigateTo({ url: `/pages/medical-record/detail?id=${id}` });
    },
    goOrder() {
      uni.switchTab({ url: '/pages/order/index' });
    }
  }
};
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 24rpx;
  background: linear-gradient(160deg, #f3f6ff, #fbfdff 65%);
}

.hero-card {
  background: linear-gradient(145deg, #2a60e2, #4d83ff);
  border-radius: 24rpx;
  padding: 24rpx;
  color: #fff;
  box-shadow: 0 16rpx 34rpx rgba(38, 82, 201, 0.22);
}

.hero-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.hero-title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
}

.hero-subtitle {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  opacity: 0.92;
}

.refresh-chip {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 999rpx;
  padding: 8rpx 14rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.chip-icon {
  width: 24rpx;
  height: 24rpx;
}

.chip-text {
  font-size: 20rpx;
}

.stats-row {
  margin-top: 18rpx;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10rpx;
}

.stat-item {
  background: rgba(255, 255, 255, 0.14);
  border-radius: 14rpx;
  padding: 12rpx;
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
}

.stat-label {
  display: block;
  margin-top: 4rpx;
  font-size: 20rpx;
  opacity: 0.95;
}

.filter-card {
  margin-top: 16rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 16rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.filter-row {
  margin-top: 12rpx;
}

.sort-row {
  margin-top: 12rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sort-label {
  font-size: 22rpx;
  color: #66748f;
}

.sort-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 22rpx;
  color: #2f65f9;
}

.sort-icon {
  width: 24rpx;
  height: 24rpx;
}

.list {
  margin-top: 16rpx;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.record-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 18rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.record-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10rpx;
}

.record-title-wrap {
  display: flex;
  gap: 10rpx;
}

.record-icon {
  width: 34rpx;
  height: 34rpx;
  margin-top: 2rpx;
}

.hospital {
  font-size: 26rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.meta {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #68758f;
}

.date {
  font-size: 22rpx;
  color: #75829b;
  white-space: nowrap;
}

.record-body {
  margin-top: 14rpx;
  background: #f5f8ff;
  border-radius: 14rpx;
  padding: 12rpx;
}

.label {
  font-size: 20rpx;
  color: #6f7c95;
}

.value {
  display: block;
  margin-top: 6rpx;
  font-size: 23rpx;
  color: #1c2b4a;
  line-height: 1.5;
}

.tag-row {
  margin-top: 12rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.record-footer {
  margin-top: 12rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.record-no {
  font-size: 20rpx;
  color: #98a3b8;
}

.detail-link {
  font-size: 22rpx;
  color: #2f65f9;
  font-weight: 600;
}

.empty {
  margin-top: 120rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.empty-icon {
  width: 88rpx;
  height: 88rpx;
}

.empty-title {
  margin-top: 16rpx;
  font-size: 28rpx;
  color: #4d5b76;
  font-weight: 600;
}

.empty-subtitle {
  margin-top: 10rpx;
  font-size: 22rpx;
  color: #98a3b8;
  line-height: 1.6;
  padding: 0 36rpx;
}

.empty-btn {
  margin-top: 20rpx;
}
</style>
