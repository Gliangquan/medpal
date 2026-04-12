<template>
  <view class="page-content">
    <!-- 搜索和筛选 -->
    <uni-card :border="false" padding="16" style="margin-bottom: 16rpx;">
      <uni-search-bar v-model="keyword" placeholder="搜索陪诊员名字/专长" cancel-button="none" @confirm="applySearch" @clear="clearSearch" />
      <view class="flex gap-sm" style="margin-top: 16rpx; flex-wrap: wrap;">
        <uni-tag
          v-for="item in filters"
          :key="item.value"
          :text="item.label"
          :type="filterType === item.value ? 'primary' : 'default'"
          @click="switchFilter(item.value)"
        />
      </view>
    </uni-card>

    <!-- 陪诊员列表 -->
    <view v-if="filteredCompanions.length" style="display: flex; flex-direction: column; gap: 16rpx;">
      <uni-card
        v-for="item in filteredCompanions"
        :key="item.id"
        :border="false"
        padding="24"
        @tap="goDetail(item.id)"
      >
        <view class="flex items-center gap-md">
          <avatar :src="item.userAvatar" :name="displayName(item)" size="lg" radius="md" />
          <view style="flex: 1;">
            <text class="text-base font-semibold text-primary">{{ displayName(item) }}</text>
            <text class="text-sm text-muted" style="display: block; margin-top: 4rpx;">★ {{ item.rating }} · 服务 {{ item.serviceCount }} 次</text>
            <text class="text-sm text-secondary" style="display: block; margin-top: 4rpx;">{{ item.specialties || '暂无专长信息' }}</text>
          </view>
          <button v-if="canBook" size="mini" type="primary" @tap.stop="quickAppointment(item.id)">预约</button>
        </view>
        <view style="margin-top: 16rpx; font-size: 22rpx; color: #666;">
          <text style="display: block;">工作年限：{{ item.workYears }} 年</text>
          <text style="display: block; margin-top: 4rpx;">服务范围：{{ item.serviceArea }}</text>
        </view>
        <view class="flex gap-sm" style="margin-top: 16rpx;">
          <uni-tag :text="item.realNameStatus === 'approved' ? '✓ 实名认证' : '待认证'" :type="item.realNameStatus === 'approved' ? 'success' : 'warning'" size="small" />
          <uni-tag :text="item.qualificationStatus === 'approved' ? '✓ 资质认证' : '待认证'" :type="item.qualificationStatus === 'approved' ? 'success' : 'warning'" size="small" />
        </view>
      </uni-card>
    </view>

    <!-- 空状态 -->
    <view v-else style="margin-top: 120rpx; text-align: center;">
      <uni-icons type="contact" size="48" color="#ccc"></uni-icons>
      <text class="text-sm text-muted" style="display: block; margin-top: 16rpx;">暂无陪诊员</text>
    </view>
  </view>
</template>

<script>
import { companionApi } from '@/utils/api.js';
import { isPatientRole } from '@/utils/permission.js';
import avatar from '@/components/avatar.vue';

export default {
  components: {
    avatar
  },
  data() {
    return {
      currentUser: null,
      keyword: '',
      filterType: 'all',
      companions: [],
      filters: [
        { label: '全部', value: 'all' },
        { label: '高评分', value: 'rating' },
        { label: '经验丰富', value: 'experience' },
        { label: '新手', value: 'new' }
      ]
    };
  },
  computed: {
    canBook() {
      return isPatientRole(this.currentUser);
    },
    filteredCompanions() {
      let list = [...this.companions];
      if (this.keyword) {
        const keyword = this.keyword.trim();
        list = list.filter((item) => {
          const name = this.displayName(item);
          const specialties = item.specialties || '';
          return name.includes(keyword) || specialties.includes(keyword);
        });
      }
      if (this.filterType === 'rating') {
        list = list.sort((a, b) => b.rating - a.rating);
      }
      if (this.filterType === 'experience') {
        list = list.sort((a, b) => b.workYears - a.workYears);
      }
      if (this.filterType === 'new') {
        list = list.sort((a, b) => a.workYears - b.workYears);
      }
      return list;
    }
  },
  async onLoad() {
    this.currentUser = uni.getStorageSync('userInfo') || null;
    await this.loadCompanions();
  },
  methods: {
    displayName(item) {
      return item?.companionName || item?.userName || '陪诊员';
    },
    async loadCompanions() {
      try {
        const page = await companionApi.list({ current: 1, size: 20 });
        this.companions = page.records || [];
      } catch (error) {
        console.error('加载陪诊员失败', error);
      }
    },
    applySearch() {},
    clearSearch() {
      this.keyword = '';
    },
    switchFilter(value) {
      this.filterType = value;
    },
    goDetail(id) {
      uni.navigateTo({ url: `/pages/companion/detail?id=${id}` });
    },
    quickAppointment(id) {
      if (!this.canBook) {
        uni.showToast({ title: '仅患者可预约', icon: 'none' });
        return;
      }
      uni.navigateTo({ url: `/pages/appointment/index?companionId=${id}` });
    }
  }
};
</script>

<style lang="scss">
@import "@/styles/common.scss";
</style>
