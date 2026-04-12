<template>
  <view class="page">
    <view class="search-card">
      <uni-search-bar v-model="keyword" placeholder="搜索医院名称" cancel-button="none" @confirm="applySearch" @clear="clearSearch" />
      <view class="filters">
        <view class="filter-chip">医院等级</view>
        <view class="filter-chip">距离最近</view>
        <view class="filter-chip">评分最高</view>
      </view>
    </view>

    <view class="section">
      <view class="section-header">
        <text class="section-title">医院列表</text>
        <text class="section-count">{{ filteredHospitals.length }}家</text>
      </view>
      <view class="list">
        <view v-for="item in filteredHospitals" :key="item.id" class="hospital-card" @tap="goDetail(item.id)">
          <view class="hospital-head">
            <view>
              <text class="hospital-name">{{ item.hospitalName }}</text>
              <view class="tags">
                <text class="tag">{{ item.hospitalLevel }}</text>
                <text class="tag">★ {{ item.rating }}</text>
              </view>
            </view>
            <text class="distance">{{ item.distance }}km</text>
          </view>
          <view class="hospital-address">{{ item.address }}</view>
          <view class="features" v-if="item.features && item.features.length">
            <text v-for="feature in item.features" :key="feature" class="feature">{{ feature }}</text>
          </view>
          <view class="hospital-footer">
            <view class="stats">
              <text>预约量 {{ item.bookingCount }}</text>
              <text>科室数 {{ item.departmentCount }}</text>
            </view>
            <button v-if="canAppointment" class="btn-primary" @tap.stop="quickAppointment(item.id)">立即预约</button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { hospitalApi } from '@/utils/api.js';
import { isPatientRole } from '@/utils/permission.js';

export default {
  data() {
    return {
      currentUser: null,
      keyword: '',
      hospitals: []
    };
  },
  computed: {
    canAppointment() {
      return isPatientRole(this.currentUser);
    },
    filteredHospitals() {
      if (!this.keyword) return this.hospitals;
      const keyword = this.keyword.trim();
      return this.hospitals.filter((item) => item.hospitalName.includes(keyword));
    }
  },
  async onLoad(options) {
    this.currentUser = uni.getStorageSync('userInfo') || null;
    await this.loadHospitals();
    if (options && options.keyword) {
      this.keyword = options.keyword;
    }
  },
  methods: {
    async loadHospitals() {
      try {
        const page = await hospitalApi.list({ current: 1, size: 50 });
        this.hospitals = page.records || [];
      } catch (error) {
        console.error('加载医院列表失败', error);
      }
    },
    applySearch() {},
    clearSearch() {
      this.keyword = '';
    },
    goDetail(id) {
      uni.navigateTo({ url: `/pages/hospital/detail?id=${id}` });
    },
    quickAppointment(id) {
      if (!this.canAppointment) {
        uni.showToast({ title: '仅患者可预约', icon: 'none' });
        return;
      }
      uni.navigateTo({ url: `/pages/appointment/index?hospitalId=${id}` });
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

.search-card {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 16rpx;
  box-shadow: 0 12rpx 30rpx rgba(33, 56, 100, 0.08);
}

.filters {
  display: flex;
  gap: 12rpx;
  margin-top: 12rpx;
}

.filter-chip {
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  background: rgba(47, 101, 249, 0.1);
  color: #2f65f9;
  font-size: 22rpx;
}

.section {
  margin-top: 20rpx;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1c2b4a;
}

.section-count {
  font-size: 22rpx;
  color: #7b8291;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.hospital-card {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 18rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.hospital-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hospital-name {
  font-size: 26rpx;
  font-weight: 600;
  color: #1c2b4a;
}

.tags {
  display: flex;
  gap: 10rpx;
  margin-top: 6rpx;
}

.tag {
  font-size: 20rpx;
  color: #2f65f9;
  background: rgba(47, 101, 249, 0.1);
  padding: 2rpx 12rpx;
  border-radius: 999rpx;
}

.distance {
  font-size: 22rpx;
  color: #7b8291;
}

.hospital-address {
  font-size: 22rpx;
  color: #7b8291;
  margin-top: 10rpx;
}

.features {
  display: flex;
  gap: 8rpx;
  flex-wrap: wrap;
  margin-top: 10rpx;
}

.feature {
  font-size: 20rpx;
  color: #5f6f94;
  background: #f4f6fb;
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
}

.hospital-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12rpx;
}

.stats {
  font-size: 20rpx;
  color: #7b8291;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.btn-primary {
  background: #2f65f9;
  color: #fff;
  border-radius: 999rpx;
  padding: 8rpx 20rpx;
  font-size: 22rpx;
}
</style>
