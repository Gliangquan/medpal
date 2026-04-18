<template>
  <view class="page-content">
    <view v-if="companion">
      <!-- 头部信息 -->
      <uni-card :border="false" padding="24">
        <view class="flex items-center gap-md">
          <avatar :src="companion.userAvatar" :name="displayName(companion)" size="xl" radius="lg" />
          <view style="flex: 1;">
            <text class="text-base font-semibold text-primary">{{ displayName(companion) }}</text>
            <text class="text-sm text-muted" style="display: block; margin-top: 4rpx;">★ {{ companion.rating }} ({{ companion.serviceCount }} 次服务)</text>
            <text class="text-sm text-theme" style="display: block; margin-top: 4rpx;">社区点赞 {{ companionLikeCount }}</text>
          </view>
        </view>
        <view class="flex gap-md" style="margin-top: 16rpx;">
          <button v-if="canBook" class="btn-primary" style="flex: 1;" @tap="quickAppointment">预约</button>
          <button class="btn-secondary" style="flex: 1;" @tap="toggleCompanionLike">{{ companionLiked ? '取消点赞' : '推荐点赞' }}</button>
        </view>
      </uni-card>

      <!-- 基本信息 -->
      <uni-section title="基本信息" class="section"></uni-section>
      <uni-card :border="false" padding="24">
        <view class="info-grid">
          <view class="info-item">
            <text class="text-sm text-muted">工作年限</text>
            <text class="text-base font-semibold text-primary" style="display: block; margin-top: 4rpx;">{{ companion.workYears }} 年</text>
          </view>
          <view class="info-item">
            <text class="text-sm text-muted">服务范围</text>
            <text class="text-base font-semibold text-primary" style="display: block; margin-top: 4rpx;">{{ companion.serviceArea }}</text>
          </view>
          <view class="info-item">
            <text class="text-sm text-muted">总收入</text>
            <text class="text-base font-semibold text-theme" style="display: block; margin-top: 4rpx;">¥{{ companion.totalIncome }}</text>
          </view>
          <view class="info-item">
            <text class="text-sm text-muted">服务次数</text>
            <text class="text-base font-semibold text-primary" style="display: block; margin-top: 4rpx;">{{ companion.serviceCount }} 次</text>
          </view>
        </view>
      </uni-card>

      <!-- 专长与资质 -->
      <uni-section title="专长与资质" class="section"></uni-section>
      <uni-card :border="false" padding="24">
        <text class="text-base text-primary" style="display: block; margin-bottom: 16rpx;">{{ companion.specialties || '暂无专长信息' }}</text>
        <view class="flex gap-sm">
          <uni-tag :text="companion.realNameStatus === 'approved' ? '✓ 实名认证' : '待认证'" :type="companion.realNameStatus === 'approved' ? 'success' : 'warning'" size="small" />
          <uni-tag :text="companion.qualificationStatus === 'approved' ? '✓ 资质认证' : '待认证'" :type="companion.qualificationStatus === 'approved' ? 'success' : 'warning'" size="small" />
        </view>
      </uni-card>

      <!-- 用户评价 -->
      <template v-if="evaluations.length">
        <uni-section title="用户评价" class="section"></uni-section>
        <view style="display: flex; flex-direction: column; gap: 16rpx;">
          <uni-card v-for="item in evaluations" :key="item.id" :border="false" padding="24">
            <view class="flex flex-between items-center" style="margin-bottom: 8rpx;">
              <text class="text-sm text-primary">用户评价</text>
              <text class="text-sm text-theme">★ {{ item.averageScore }}</text>
            </view>
            <text class="text-sm text-secondary" style="display: block;">{{ item.evaluationText }}</text>
            <view style="margin-top: 12rpx; font-size: 20rpx; color: #999; display: grid; grid-template-columns: repeat(2, 1fr); gap: 8rpx;">
              <text>专业性 {{ item.professionalismScore }}/5</text>
              <text>态度 {{ item.attitudeScore }}/5</text>
              <text>效率 {{ item.efficiencyScore }}/5</text>
              <text>满意度 {{ item.satisfactionScore }}/5</text>
            </view>
          </uni-card>
        </view>
      </template>

      <!-- 联系方式 -->
      <uni-section title="联系方式" class="section"></uni-section>
      <uni-list :border="false">
        <uni-list-item title="电话" :right-text="companion.companionPhone || companion.userPhone || '平台内联系'"></uni-list-item>
        <uni-list-item title="账号" :right-text="companion.companionAccount || companion.userAccount || '-'"></uni-list-item>
      </uni-list>
    </view>
  </view>
</template>

<script>
import { companionApi, communityApi, evaluationApi } from '@/utils/api.js';
import { isPatientRole } from '@/utils/permission.js';
import avatar from '@/components/avatar.vue';

export default {
  components: {
    avatar
  },
  data() {
    return {
      currentUser: null,
      companionId: null,
      companion: null,
      evaluations: [],
      companionLikeCount: 0,
      companionLiked: false
    };
  },
  computed: {
    canBook() {
      return isPatientRole(this.currentUser);
    }
  },
  onLoad(options) {
    this.currentUser = uni.getStorageSync('userInfo') || null;
    if (options && options.id) {
      this.companionId = options.id;
      this.loadCompanion(options.id);
      this.loadEvaluations(options.id);
      this.loadCompanionLikeState(options.id);
    }
  },
  methods: {
    displayName(item) {
      return item?.companionName || item?.userName || '陪诊员';
    },
    async loadCompanion(id) {
      try {
        this.companion = await companionApi.detail(id);
      } catch (error) {
        uni.showToast({ title: '加载陪诊员失败', icon: 'none' });
      }
    },
    async loadEvaluations(id) {
      try {
        const page = await evaluationApi.list({ companionId: id, current: 1, size: 10 });
        this.evaluations = page.records || [];
      } catch (error) {
        console.error('加载评价失败', error);
      }
    },
    async loadCompanionLikeState(id) {
      try {
        const list = await communityApi.goldCompanions(20);
        const matched = (list || []).find((item) => Number(item.id) === Number(id));
        this.companionLikeCount = Number(matched?.likeCount || 0);
      } catch (error) {
        this.companionLikeCount = 0;
      }
    },
    async toggleCompanionLike() {
      try {
        const result = await communityApi.toggleCompanionLike(this.companionId);
        this.companionLiked = !!result.liked;
        this.companionLikeCount = Number(result.likeCount || 0);
        uni.showToast({ title: this.companionLiked ? '已点赞推荐' : '已取消点赞', icon: 'success' });
      } catch (error) {
        uni.showToast({ title: error.message || '操作失败', icon: 'none' });
      }
    },
    quickAppointment() {
      if (!this.canBook) {
        uni.showToast({ title: '仅患者可预约', icon: 'none' });
        return;
      }
      uni.navigateTo({ url: `/pages/appointment/index?companionId=${this.companionId}` });
    },
    follow() {
      uni.showToast({ title: '已关注', icon: 'success' });
    }
  }
};
</script>

<style lang="scss">
@import "@/styles/common.scss";

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
}

.info-item {
  background: #f4f6fb;
  border-radius: 16rpx;
  padding: 16rpx;
}
</style>
