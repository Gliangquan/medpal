<template>
  <view class="page">
    <view v-if="companion" class="companion-card">
      <avatar :src="companion.userAvatar" :name="companionName" size="lg" radius="md" />
      <view>
        <text class="name">{{ companionName }}</text>
        <text class="role">陪诊员</text>
      </view>
    </view>

    <uni-section title="服务评分" type="line"></uni-section>
    <view class="card">
      <view class="rate-item">
        <text class="rate-label">专业度</text>
        <uni-rate v-model="ratings.professionalism" />
      </view>
      <view class="rate-item">
        <text class="rate-label">态度</text>
        <uni-rate v-model="ratings.attitude" />
      </view>
      <view class="rate-item">
        <text class="rate-label">效率</text>
        <uni-rate v-model="ratings.efficiency" />
      </view>
      <view class="rate-item">
        <text class="rate-label">满意度</text>
        <uni-rate v-model="ratings.satisfaction" />
      </view>
    </view>

    <uni-section title="评价内容" type="line"></uni-section>
    <view class="card">
      <uni-easyinput
        type="textarea"
        v-model="evaluationText"
        placeholder="请输入您的评价（1-500字）"
        maxlength="500"
      />
      <text class="count">{{ evaluationText.length }}/500</text>
    </view>

    <uni-section title="上传图片" type="line"></uni-section>
    <view class="card">
      <uni-file-picker v-model="images" limit="5" file-mediatype="image" />
    </view>

    <button class="btn-primary" :disabled="submitting" @tap="submitEvaluation">提交评价</button>
  </view>
</template>

<script>
import { companionApi, evaluationApi, orderApi } from '@/utils/api.js';
import { isPatientRole } from '@/utils/permission.js';
import avatar from '@/components/avatar.vue';

export default {
  components: {
    avatar
  },
  data() {
    return {
      currentUser: null,
      orderId: null,
      order: null,
      companion: null,
      ratings: {
        professionalism: 0,
        attitude: 0,
        efficiency: 0,
        satisfaction: 0
      },
      evaluationText: '',
      images: [],
      submitting: false
    };
  },
  async onLoad(options) {
    this.currentUser = uni.getStorageSync('userInfo') || null;
    if (!this.currentUser?.id) {
      uni.showToast({ title: '请先登录', icon: 'none' });
      uni.reLaunch({ url: '/pages/login/index' });
      return;
    }
    if (!isPatientRole(this.currentUser)) {
      uni.showToast({ title: '仅患者可评价服务', icon: 'none' });
      uni.switchTab({ url: '/pages/order/index' });
      return;
    }
    if (options && options.orderId) {
      this.orderId = options.orderId;
      await this.loadOrder();
    }
  },
  computed: {
    companionName() {
      return this.companion?.companionName || this.companion?.userName || '陪诊员';
    }
  },
  methods: {
    async loadOrder() {
      try {
        this.order = await orderApi.get(this.orderId);
        if (!this.order) {
          uni.showToast({ title: '订单不存在', icon: 'none' });
          return;
        }
        if (Number(this.order.userId) !== Number(this.currentUser?.id)) {
          uni.showToast({ title: '仅可评价自己的订单', icon: 'none' });
          setTimeout(() => uni.switchTab({ url: '/pages/order/index' }), 600);
          return;
        }
        if (this.order.orderStatus !== 'completed') {
          uni.showToast({ title: '订单未完成，暂不可评价', icon: 'none' });
          setTimeout(() => uni.navigateBack(), 600);
          return;
        }
        if (this.order?.companionId) {
          this.companion = await companionApi.detail(this.order.companionId);
        }
      } catch (error) {
        uni.showToast({ title: error.message || '加载订单失败', icon: 'none' });
      }
    },
    async submitEvaluation() {
      const { professionalism, attitude, efficiency, satisfaction } = this.ratings;
      if (!professionalism || !attitude || !efficiency || !satisfaction) {
        return uni.showToast({ title: '请完成所有评分', icon: 'none' });
      }
      if (!this.evaluationText.trim()) {
        return uni.showToast({ title: '请输入评价内容', icon: 'none' });
      }
      if (!this.order || !this.companion) {
        return uni.showToast({ title: '订单或陪诊员信息缺失', icon: 'none' });
      }
      if (!this.currentUser?.id || !isPatientRole(this.currentUser)) {
        uni.showToast({ title: '仅患者可评价', icon: 'none' });
        return;
      }
      if (Number(this.order.userId) !== Number(this.currentUser.id)) {
        uni.showToast({ title: '无权评价该订单', icon: 'none' });
        return;
      }

      this.submitting = true;
      try {
        const averageScore = (professionalism + attitude + efficiency + satisfaction) / 4;
        await evaluationApi.create({
          orderId: this.orderId,
          userId: this.currentUser.id,
          companionId: this.companion.id,
          professionalismScore: professionalism,
          attitudeScore: attitude,
          efficiencyScore: efficiency,
          satisfactionScore: satisfaction,
          averageScore: Math.round(averageScore * 10) / 10,
          evaluationText: this.evaluationText,
          evaluationImages: this.images.length ? this.images.join(',') : null,
          status: 'published'
        });
        uni.showToast({ title: '评价提交成功', icon: 'success' });
        setTimeout(() => {
          uni.navigateBack();
        }, 800);
      } catch (error) {
        console.error('提交评价失败', error);
        uni.showToast({ title: error.message || '提交失败', icon: 'none' });
      } finally {
        this.submitting = false;
      }
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

.companion-card {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 20rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  box-shadow: 0 12rpx 30rpx rgba(33, 56, 100, 0.08);
  margin-bottom: 20rpx;
}

.name {
  font-size: 26rpx;
  font-weight: 600;
  color: #1c2b4a;
  display: block;
}

.role {
  font-size: 22rpx;
  color: #7b8291;
}

.card {
  background: #ffffff;
  border-radius: 18rpx;
  padding: 18rpx;
  margin-bottom: 18rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.rate-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.rate-label {
  font-size: 24rpx;
  color: #1c2b4a;
}

.count {
  display: block;
  text-align: right;
  font-size: 20rpx;
  color: #9aa4b8;
  margin-top: 8rpx;
}

.btn-primary {
  background: #2f65f9;
  color: #ffffff;
  border-radius: 999rpx;
  padding: 18rpx 0;
  font-size: 24rpx;
}
</style>
