<template>
  <view class="page-content">
    <view v-if="order">
      <!-- 状态卡片 -->
      <uni-card :border="false" padding="24" style="border-left: 6rpx solid;" :style="{ borderLeftColor: order.statusColor }">
        <view class="flex flex-between items-center">
          <text class="text-base font-bold" :style="{ color: order.statusColor }">{{ order.statusText }}</text>
          <text class="text-sm text-muted">订单号 {{ order.orderNo || `#${order.id}` }}</text>
        </view>
        <text class="text-sm text-muted" style="display: block; margin-top: 8rpx;">创建时间：{{ order.createTimeFormatted }}</text>
      </uni-card>

      <!-- 医院信息 -->
      <uni-section title="医院信息" class="section"></uni-section>
      <uni-list :border="false">
        <uni-list-item title="医院" :right-text="order.hospitalName"></uni-list-item>
        <uni-list-item title="科室" :right-text="order.departmentName"></uni-list-item>
        <uni-list-item title="医生" :right-text="order.doctorName"></uni-list-item>
        <uni-list-item title="就诊时间" :right-text="order.appointmentDate"></uni-list-item>
      </uni-list>

      <!-- 陪诊员信息 -->
      <template v-if="showCompanionSection">
        <uni-section title="陪诊员信息" class="section"></uni-section>
        <uni-card :border="false" padding="24" v-if="order.companionId">
          <view class="flex items-center gap-md">
            <avatar :src="order.companionAvatar" :name="order.companionName || '陪诊员'" size="lg" radius="md" />
            <view style="flex: 1;">
              <text class="text-base font-semibold text-primary">{{ order.companionName || '已接单陪诊员' }}</text>
              <text class="text-sm text-muted" style="display: block; margin-top: 4rpx;">{{ order.companionPhone || '平台内联系' }}</text>
            </view>
          </view>
        </uni-card>
        <uni-list :border="false" v-else>
          <uni-list-item title="姓名" :right-text="order.orderStatus === 'pending' ? '指定陪诊员待确认' : '已接单陪诊员'"></uni-list-item>
        </uni-list>
      </template>

      <!-- 服务详情 -->
      <uni-section title="服务详情" class="section"></uni-section>
      <uni-list :border="false">
        <uni-list-item title="陪诊时长" :right-text="order.durationText"></uni-list-item>
        <uni-list-item title="特殊需求" :right-text="order.specificNeeds || '无'"></uni-list-item>
      </uni-list>

      <!-- 费用明细 -->
      <uni-section title="费用明细" class="section"></uni-section>
      <uni-list :border="false">
        <uni-list-item title="服务费" :right-text="'¥' + order.serviceFee"></uni-list-item>
        <uni-list-item title="平台费" :right-text="'¥' + order.platformFee"></uni-list-item>
        <uni-list-item title="总费用" :right-text="'¥' + order.totalPrice" :style="{ fontWeight: 'bold' }"></uni-list-item>
      </uni-list>

      <!-- 支付状态 -->
      <uni-section title="支付状态" class="section"></uni-section>
      <uni-list :border="false">
        <uni-list-item>
          <template v-slot:header>
            <uni-tag :text="order.paymentStatus === 'paid' ? '已支付' : '待支付'" :type="order.paymentStatus === 'paid' ? 'success' : 'warning'"></uni-tag>
          </template>
        </uni-list-item>
      </uni-list>

      <!-- 操作按钮 -->
      <view class="action-panel">
        <view class="action-grid">
          <button v-if="canPay" class="btn-primary action-btn" @tap="goPayment">
            <image class="btn-icon" src="/static/icon_med/fukuan.png" mode="aspectFit" />
            <text>立即支付</text>
          </button>
          <button v-if="canEvaluate" class="btn-primary action-btn" @tap="goEvaluation">
            <image class="btn-icon" src="/static/icon_med/zan.png" mode="aspectFit" />
            <text>评价服务</text>
          </button>
          <button v-if="canAcceptOrder" class="btn-primary action-btn" @tap="acceptOrder">
            <image class="btn-icon" src="/static/icon_med/wancheng.png" mode="aspectFit" />
            <text>立即接单</text>
          </button>
          <button v-if="canComplete" class="btn-primary action-btn" @tap="completeOrder">
            <image class="btn-icon" src="/static/icon_med/wancheng.png" mode="aspectFit" />
            <text>完成订单</text>
          </button>
          <button
            v-if="isPatientUser"
            class="btn-ghost action-btn"
            :disabled="!order.companionId"
            @tap="contactCompanion"
          >
            <image class="btn-icon" src="/static/icon_med/zixun.png" mode="aspectFit" />
            <text>{{ patientContactText }}</text>
          </button>
          <button
            v-else-if="isCompanionUser && canContact"
            class="btn-ghost action-btn"
            @tap="contactCompanion"
          >
            <image class="btn-icon" src="/static/icon_med/lianxiren.png" mode="aspectFit" />
            <text>联系患者</text>
          </button>
          <button v-if="canCancel" type="warn" class="action-btn warn-btn" @tap="openCancel">
            <image class="btn-icon" src="/static/icon_med/yiquxiao.png" mode="aspectFit" />
            <text>取消订单</text>
          </button>
          <button class="btn-secondary action-btn action-back" @tap="goBack">
            <image class="btn-icon" src="/static/icon_med/shouye.png" mode="aspectFit" />
            <text>返回</text>
          </button>
        </view>
      </view>
    </view>

    <!-- 取消弹窗 -->
    <uni-popup ref="cancelPopup" type="center">
      <view style="width: 560rpx; background: #fff; border-radius: 20rpx; padding: 32rpx;">
        <text class="text-base font-semibold" style="display: block; margin-bottom: 16rpx;">取消订单</text>
        <uni-easyinput type="textarea" v-model="cancelReason" placeholder="请输入取消原因（可选）" />
        <view class="flex gap-md" style="margin-top: 24rpx;">
          <button class="btn-secondary" style="flex: 1;" @tap="closeCancel">返回</button>
          <button type="warn" style="flex: 1; border-radius: 999rpx;" @tap="confirmCancel">确认取消</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import { departmentApi, doctorApi, hospitalApi, orderApi, userApi } from '@/utils/api.js';
import { formatDateTime } from '@/utils/format.js';
import {
  isCompanionCertified,
  isCompanionRole as checkCompanionRole,
  isPatientRole as checkPatientRole
} from '@/utils/permission.js';
import avatar from '@/components/avatar.vue';

const STATUS_TEXT = {
  pending: '待接单',
  confirmed: '已接单',
  serving: '已接单',
  completion_pending: '待确认完成',
  completed: '已完成',
  cancelled: '已取消'
};

const STATUS_COLOR = {
  pending: '#FF9500',
  confirmed: '#2f65f9',
  serving: '#2f65f9',
  completion_pending: '#6f52ed',
  completed: '#7b8291',
  cancelled: '#ff5c5c'
};

export default {
  components: {
    avatar
  },
  data() {
    return {
      order: null,
      orderId: null,
      cancelReason: '',
      currentUser: null
    };
  },
  computed: {
    currentUserRole() {
      return this.currentUser?.userRole;
    },
    isCompanionUser() {
      return checkCompanionRole(this.currentUser);
    },
    isPatientUser() {
      return checkPatientRole(this.currentUser);
    },
    showCompanionSection() {
      return Boolean(this.order?.companionId || this.order?.companionName);
    },
    canPay() {
      return this.isPatientUser
        && this.order?.paymentStatus !== 'paid'
        && ['pending', 'confirmed'].includes(this.order?.orderStatus);
    },
    canEvaluate() {
      return this.isPatientUser && this.order?.orderStatus === 'completed';
    },
    canCancel() {
      if (!this.isPatientUser || !this.order) return false;
      return this.order.orderStatus === 'pending';
    },
    canComplete() {
      if (!this.order || this.order.paymentStatus !== 'paid') return false;
      if (!['confirmed', 'serving'].includes(this.order.orderStatus)) return false;
      if (this.isPatientUser) {
        return this.order.orderStatus === 'completion_pending'
          && Number(this.order.userId) === Number(this.currentUser?.id);
      }
      if (this.isCompanionUser) {
        return ['confirmed', 'serving'].includes(this.order.orderStatus)
          && Number(this.order.companionId) === Number(this.currentUser?.id);
      }
      return false;
    },
    canAcceptOrder() {
      if (!this.isCompanionUser || !this.order || !this.currentUser?.id) return false;
      const isPending = this.order.orderStatus === 'pending';
      const isLegacyUnassignedConfirmed = this.order.orderStatus === 'confirmed' && !this.order.companionId;
      if (!isPending && !isLegacyUnassignedConfirmed) return false;
      if (this.order.paymentStatus !== 'paid') return false;
      const isMarketOrder = !this.order.companionId;
      const isAssignedToMe = Number(this.order.companionId) === Number(this.currentUser.id);
      return (isMarketOrder || isAssignedToMe) && isCompanionCertified(this.currentUser);
    },
    canContact() {
      if (!this.order || !this.currentUser?.id) return false;
      if (this.isCompanionUser) {
        return Number(this.order.companionId) === Number(this.currentUser.id)
          && ['confirmed', 'completed'].includes(this.order.orderStatus);
      }
      return Boolean(this.order.companionId);
    },
    patientContactText() {
      return this.order?.companionId ? '联系陪诊员' : '待接单后可联系陪诊员';
    },
    chatPeerId() {
      if (!this.order) return null;
      return this.isCompanionUser ? this.order.userId : this.order.companionId;
    },
    chatPeerName() {
      if (this.isCompanionUser) return '患者';
      return this.order?.companionName || '陪诊员';
    }
  },
  onLoad(options) {
    this.currentUser = uni.getStorageSync('userInfo') || null;
    this.refreshCurrentUser();
    if (options && options.id) {
      this.orderId = options.id;
      this.loadDetail();
    }
  },
  methods: {
    getDisplayStatusText(order) {
      const status = order?.orderStatus;
      const paymentStatus = order?.paymentStatus;
      const hasCompanion = Boolean(order?.companionId);
      if (status === 'pending') {
        if (paymentStatus !== 'paid') return '待支付';
        return hasCompanion ? '已支付待确认' : '已支付待接单';
      }
      if (status === 'confirmed' && !hasCompanion) {
        return paymentStatus === 'paid' ? '已支付待接单' : '待支付';
      }
      if (status === 'completion_pending') {
        return this.isPatientUser ? '待确认完成' : '待用户确认';
      }
      return STATUS_TEXT[status] || '未知';
    },
    getDisplayStatusColor(order) {
      const status = order?.orderStatus;
      const paymentStatus = order?.paymentStatus;
      if (status === 'pending' && paymentStatus !== 'paid') {
        return '#f08c00';
      }
      if (status === 'pending') {
        return '#2f65f9';
      }
      return STATUS_COLOR[status] || '#7b8291';
    },
    async refreshCurrentUser() {
      try {
        const user = await userApi.fetchCurrentUser();
        if (user?.id) {
          this.currentUser = user;
          uni.setStorageSync('userInfo', user);
        }
      } catch (error) {
        // ignore
      }
    },
    normalizeDuration(duration) {
      if (duration === null || duration === undefined || duration === '') return '-';
      const raw = String(duration).trim();
      if (!raw) return '-';
      if (/^\d+$/.test(raw)) return `${raw} 小时`;
      if (/^\d+(\.\d+)?h$/i.test(raw)) return `${raw.replace(/h$/i, '')} 小时`;
      return raw;
    },
    toIdKey(id) {
      if (id === null || id === undefined || id === '') return '';
      const num = Number(id);
      if (Number.isNaN(num) || num <= 0) return '';
      return String(num);
    },
    async resolveHospitalName(order) {
      if (order?.hospitalName) return order.hospitalName;
      const hospitalId = this.toIdKey(order?.hospitalId);
      if (!hospitalId) return '';
      try {
        const hospital = await hospitalApi.detail(hospitalId);
        return hospital?.hospitalName || '';
      } catch (error) {
        return '';
      }
    },
    async resolveDepartmentName(order) {
      if (order?.departmentName) return order.departmentName;
      const hospitalId = this.toIdKey(order?.hospitalId);
      const departmentId = this.toIdKey(order?.departmentId);
      if (!hospitalId || !departmentId) return '';
      const size = 100;
      try {
        for (let current = 1; current <= 10; current += 1) {
          const page = await departmentApi.list({ current, size, hospitalId });
          const records = page?.records || [];
          const matched = records.find((item) => this.toIdKey(item.id) === departmentId);
          if (matched?.departmentName) return matched.departmentName;
          if (records.length < size) break;
        }
      } catch (error) {
        return '';
      }
      return '';
    },
    async resolveDoctorName(order) {
      if (order?.doctorName) return order.doctorName;
      const departmentId = this.toIdKey(order?.departmentId);
      const doctorId = this.toIdKey(order?.doctorId);
      if (!departmentId || !doctorId) return '';
      const size = 100;
      try {
        for (let current = 1; current <= 10; current += 1) {
          const page = await doctorApi.list({ current, size, departmentId });
          const records = page?.records || [];
          const matched = records.find((item) => this.toIdKey(item.id) === doctorId);
          if (matched?.doctorName) return matched.doctorName;
          if (records.length < size) break;
        }
      } catch (error) {
        return '';
      }
      return '';
    },
    async enrichOrderNames(order) {
      if (!order) return order;
      const [hospitalName, departmentName, doctorName] = await Promise.all([
        this.resolveHospitalName(order),
        this.resolveDepartmentName(order),
        this.resolveDoctorName(order)
      ]);
      return {
        ...order,
        hospitalName: hospitalName || order?.hospitalName || '',
        departmentName: departmentName || order?.departmentName || '',
        doctorName: doctorName || order?.doctorName || ''
      };
    },
    async loadDetail() {
      try {
        const rawOrder = await orderApi.get(this.orderId);
        if (!rawOrder) {
          uni.showToast({ title: '订单不存在', icon: 'none' });
          return;
        }
        const order = await this.enrichOrderNames(rawOrder);
        if (!order) {
          uni.showToast({ title: '订单不存在', icon: 'none' });
          return;
        }
        this.order = {
          ...order,
          statusText: this.getDisplayStatusText(order),
          statusColor: this.getDisplayStatusColor(order),
          createTimeFormatted: formatDateTime(order.createTime || order.appointmentDate),
          // 确保字段有默认值
          hospitalName: order.hospitalName || '未指定医院',
          departmentName: order.departmentName || '未指定科室',
          doctorName: order.doctorName || '未指定医生',
          companionName: order.companionName || null,
          companionPhone: order.companionPhone || null,
          duration: order.duration || '-',
          durationText: this.normalizeDuration(order.duration),
          specificNeeds: order.specificNeeds || '无',
          serviceFee: order.serviceFee || 0,
          platformFee: order.platformFee || 0,
          totalPrice: order.totalPrice || 0,
          paymentStatus: order.paymentStatus || 'unpaid'
        };
      } catch (error) {
        uni.showToast({ title: error.message || '订单不存在', icon: 'none' });
      }
    },
    goPayment() {
      if (!this.canPay) return;
      uni.navigateTo({ url: `/pages/payment/index?orderId=${this.orderId}` });
    },
    goEvaluation() {
      if (!this.canEvaluate) return;
      uni.navigateTo({ url: `/pages/evaluation/index?orderId=${this.orderId}` });
    },
    acceptOrder() {
      if (!isCompanionCertified(this.currentUser)) {
        uni.showModal({
          title: '请先完成认证',
          content: '完成实名认证与资质认证后才可接单，是否前往认证中心？',
          success: (res) => {
            if (res.confirm) {
              uni.navigateTo({ url: '/pages/companion/certification' });
            }
          }
        });
        return;
      }
      if (!this.canAcceptOrder) return;
      uni.showModal({
        title: '确认接单',
        content: `确定接订单 #${this.orderId} 吗？`,
        success: async (res) => {
          if (!res.confirm) return;
          try {
            await orderApi.accept(this.orderId, this.currentUser.id);
            uni.showToast({ title: '接单成功', icon: 'success' });
            this.loadDetail();
          } catch (error) {
            uni.showToast({ title: error.message || '接单失败', icon: 'none' });
          }
        }
      });
    },
    completeOrder() {
      if (!this.canComplete) return;
      const isCompanionAction = this.isCompanionUser;
      uni.showModal({
        title: '确认完成',
        content: isCompanionAction ? '确认发起完成申请，等待用户确认吗？' : '确认该订单服务已结束并完成吗？',
        success: async (res) => {
          if (!res.confirm) return;
          try {
            if (isCompanionAction) {
              await orderApi.complete(this.orderId);
              uni.showToast({ title: '已提交完成申请', icon: 'success' });
            } else {
              await orderApi.confirmComplete(this.orderId);
              uni.showToast({ title: '订单已完成', icon: 'success' });
            }
            this.loadDetail();
          } catch (error) {
            uni.showToast({ title: error.message || '完成失败', icon: 'none' });
          }
        }
      });
    },
    openCancel() {
      if (!this.canCancel) return;
      this.$refs.cancelPopup.open();
    },
    closeCancel() {
      this.cancelReason = '';
      this.$refs.cancelPopup.close();
    },
    async confirmCancel() {
      if (!this.canCancel) return;
      try {
        await orderApi.cancel(this.orderId, this.cancelReason || '用户主动取消');
        this.closeCancel();
        this.loadDetail();
        uni.showToast({ title: '订单已取消', icon: 'success' });
      } catch (error) {
        uni.showToast({ title: error.message || '取消失败', icon: 'none' });
      }
    },
    contactCompanion() {
      if (!this.canContact) {
        uni.showToast({ title: '当前状态不可联系', icon: 'none' });
        return;
      }
      if (!this.chatPeerId) {
        uni.showToast({ title: '未找到聊天对象', icon: 'none' });
        return;
      }
      const peerName = encodeURIComponent(this.chatPeerName);
      uni.navigateTo({
        url: `/pages/chat/index?orderId=${this.orderId}&peerId=${this.chatPeerId}&peerName=${peerName}`
      });
    },
    goBack() {
      uni.navigateBack();
    }
  }
};
</script>

<style scoped lang="scss">
@import "@/styles/common.scss";

.action-panel {
  margin-top: 32rpx;
  position: sticky;
  bottom: 0;
  padding: 16rpx 0 calc(16rpx + env(safe-area-inset-bottom));
  background: linear-gradient(180deg, rgba(244, 246, 251, 0), #f4f6fb 24rpx, #f4f6fb);
}

.action-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  padding: 16rpx;
  border-radius: 24rpx;
  background: #fff;
  box-shadow: 0 10rpx 28rpx rgba(33, 56, 100, 0.08);
}

.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  flex: 1 1 calc(50% - 6rpx);
  width: calc(50% - 6rpx) !important;
  min-height: 84rpx;
  line-height: 84rpx;
  margin: 0;
  border-radius: 18rpx;
  font-size: 28rpx;
}

.action-back {
  flex-basis: 100%;
  width: 100% !important;
}

.warn-btn {
  border-radius: 18rpx !important;
}

.action-grid button[disabled] {
  opacity: 0.6;
}

.btn-icon {
  width: 30rpx;
  height: 30rpx;
}
</style>
