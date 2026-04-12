<template>
  <view class="page">
    <view class="card" v-if="order">
      <text class="card-title">{{ order.hospitalName || '未指定医院' }}</text>
      <text class="card-sub">{{ order.departmentName || '未指定科室' }} · {{ order.doctorName || '未指定医生' }}</text>
    </view>

    <view class="amount">
      <text class="label">支付金额</text>
      <text class="value">¥{{ order ? order.totalPrice : amount }}</text>
    </view>

    <uni-section title="订单详情" type="line"></uni-section>
    <view class="card">
      <view class="row"><text class="label">订单编号</text><text>{{ order ? (order.orderNo || `#${order.id}`) : orderNo }}</text></view>
      <view class="row"><text class="label">就诊时间</text><text>{{ order ? order.appointmentDate : appointmentTime }}</text></view>
      <view class="row"><text class="label">陪诊时长</text><text>{{ durationText }}</text></view>
      <view class="row"><text class="label">服务类型</text><text>陪诊服务</text></view>
    </view>

    <uni-section title="选择支付方式" type="line"></uni-section>
    <view class="card">
      <uni-data-checkbox v-model="paymentMethod" :localdata="paymentOptions" />
    </view>

    <uni-section title="备注" type="line"></uni-section>
    <view class="card">
      <uni-easyinput v-model="remark" placeholder="请输入备注信息（选填）" />
    </view>

    <button class="btn-primary" :disabled="paying" @tap="handlePay">
      确认支付 ¥{{ order ? order.totalPrice : amount }}
    </button>

    <view class="tip">支付环境安全，请放心支付</view>
  </view>
</template>

<script>
import { departmentApi, doctorApi, hospitalApi, orderApi, paymentApi } from '@/utils/api.js';
import { isPatientRole } from '@/utils/permission.js';

export default {
  data() {
    return {
      currentUser: null,
      orderId: null,
      order: null,
      amount: 0,
      orderNo: '',
      appointmentTime: '',
      duration: 0,
      paymentMethod: 'wechat',
      remark: '',
      paying: false,
      paymentOptions: [
        { text: '微信支付', value: 'wechat' },
        { text: '余额支付', value: 'balance' }
      ]
    };
  },
  computed: {
    durationText() {
      const raw = this.order ? this.order.duration : this.duration;
      if (raw === null || raw === undefined || raw === '') return '-';
      const text = String(raw);
      if (/^\d+$/.test(text)) return `${text} 小时`;
      if (/^\d+(\.\d+)?h$/i.test(text)) return `${text.replace(/h$/i, '')} 小时`;
      return text;
    }
  },
  async onLoad(options) {
    const opts = options || {};
    this.currentUser = uni.getStorageSync('userInfo') || null;
    if (!this.currentUser?.id) {
      uni.showToast({ title: '请先登录', icon: 'none' });
      uni.reLaunch({ url: '/pages/login/index' });
      return;
    }
    if (!isPatientRole(this.currentUser)) {
      uni.showToast({ title: '仅患者可支付订单', icon: 'none' });
      uni.switchTab({ url: '/pages/order/index' });
      return;
    }
    if (opts.orderId) {
      this.orderId = opts.orderId;
      try {
        const rawOrder = await orderApi.get(this.orderId);
        this.order = await this.enrichOrderNames(rawOrder);
        if (!this.order) {
          uni.showToast({ title: '订单不存在', icon: 'none' });
          setTimeout(() => uni.navigateBack(), 600);
          return;
        }
        if (Number(this.order?.userId) !== Number(this.currentUser.id)) {
          uni.showToast({ title: '仅可支付自己的订单', icon: 'none' });
          setTimeout(() => uni.switchTab({ url: '/pages/order/index' }), 600);
          return;
        }
        const payableStatusSet = ['pending', 'confirmed'];
        if (!payableStatusSet.includes(this.order?.orderStatus) || this.order?.paymentStatus === 'paid') {
          uni.showToast({ title: '当前订单无需支付', icon: 'none' });
          setTimeout(() => uni.navigateBack(), 600);
        }
      } catch (error) {
        uni.showToast({ title: error.message || '加载订单失败', icon: 'none' });
        setTimeout(() => uni.navigateBack(), 600);
      }
    } else {
      this.amount = opts.amount || 0;
      this.orderNo = opts.orderNo || '';
      this.appointmentTime = opts.appointmentTime || '';
      this.duration = opts.duration || 0;
    }
  },
  methods: {
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
    async handlePay() {
      if (this.paying) return;
      if (!isPatientRole(this.currentUser)) {
        uni.showToast({ title: '仅患者可支付订单', icon: 'none' });
        return;
      }
      if (this.order && Number(this.order.userId) !== Number(this.currentUser?.id)) {
        uni.showToast({ title: '无权支付该订单', icon: 'none' });
        return;
      }
      if (!this.order && !this.orderId) {
        return uni.showToast({ title: '订单信息不完整', icon: 'none' });
      }
      this.paying = true;
      try {
        const orderIdentifier = this.order?.id || this.orderId;
        const amount = this.order?.totalPrice || this.amount;
        const payment = await paymentApi.create({
          orderId: orderIdentifier,
          amount,
          description: this.remark
        });
        if (payment?.id) {
          await paymentApi.mockPay(payment.id);
        }
        uni.showModal({
          title: '支付成功',
          content: '订单支付成功！',
          showCancel: false,
          success: () => {
            // 跳转到订单列表(tabBar页面用switchTab)
            uni.switchTab({ url: '/pages/order/index' });
          }
        });
      } catch (error) {
        uni.showToast({ title: error.message || '支付失败', icon: 'none' });
      } finally {
        this.paying = false;
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

.card {
  background: #ffffff;
  border-radius: 18rpx;
  padding: 18rpx;
  margin-bottom: 18rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.card-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1c2b4a;
}

.card-sub {
  font-size: 22rpx;
  color: #7b8291;
  margin-top: 6rpx;
  display: block;
}

.amount {
  background: #ffffff;
  border-radius: 18rpx;
  padding: 20rpx;
  margin: 18rpx 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.amount .label {
  font-size: 24rpx;
  color: #7b8291;
}

.amount .value {
  font-size: 32rpx;
  font-weight: 700;
  color: #2f65f9;
}

.row {
  display: flex;
  justify-content: space-between;
  font-size: 24rpx;
  color: #1c2b4a;
  margin-bottom: 10rpx;
}

.label {
  color: #7b8291;
}

.btn-primary {
  background: #2f65f9;
  color: #ffffff;
  border-radius: 999rpx;
  padding: 18rpx 0;
  font-size: 24rpx;
}

.tip {
  text-align: center;
  font-size: 22rpx;
  color: #9aa4b8;
  margin-top: 16rpx;
}
</style>
