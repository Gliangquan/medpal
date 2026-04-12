<template>
  <view class="page-content">
    <view v-if="isPatientRole" class="flex gap-md" style="margin-bottom: 16rpx;">
      <button class="btn-primary order-top-btn" style="flex: 1;" @tap="goAppointment">
        <image class="btn-icon" src="/static/icon_med/bianji.png" mode="aspectFit" />
        <text>新建订单</text>
      </button>
      <button class="btn-ghost order-top-btn" style="flex: 1;" @tap="showDrafts">
        <image class="btn-icon" src="/static/icon_med/lishijilu.png" mode="aspectFit" />
        <text>草稿箱</text>
      </button>
    </view>
    <view v-else-if="isCompanionRole" class="flex gap-md" style="margin-bottom: 16rpx;">
      <button class="btn-primary order-top-btn" style="flex: 1;" @tap="refreshList">
        <image class="btn-icon" src="/static/icon_med/shuaxing.png" mode="aspectFit" />
        <text>刷新列表</text>
      </button>
      <button class="btn-ghost order-top-btn" style="flex: 1;" @tap="showCompanionGuide">
        <image class="btn-icon" src="/static/icon_med/suoming.png" mode="aspectFit" />
        <text>接单说明</text>
      </button>
    </view>

    <uni-segmented-control
      :values="statusTabs"
      :current="currentTab"
      style-type="button"
      active-color="#2f65f9"
      @clickItem="filterStatus"
    />

    <view v-if="orders.length" style="margin-top: 16rpx;">
      <uni-card
        v-for="item in orders"
        :key="item.id"
        :border="false"
        padding="24"
        style="margin-bottom: 16rpx;"
        @tap="goDetail(item.id)"
      >
        <view class="flex flex-between items-center">
          <view>
            <text class="text-sm text-muted">订单号 {{ item.orderNo || `#${item.id}` }}</text>
            <text class="text-sm text-muted" style="display: block; margin-top: 4rpx;">{{ item.createTimeFormatted }}</text>
          </view>
          <text class="text-sm font-semibold" :style="{ color: item.statusColor }">{{ item.statusText }}</text>
        </view>
        <view style="margin: 16rpx 0;">
          <text class="text-base font-semibold text-primary">{{ item.hospitalName }}</text>
          <text class="text-sm text-secondary" style="display: block; margin: 8rpx 0;">{{ item.departmentName }}</text>
          <text class="text-sm text-muted" style="display: block;">就诊时间：{{ item.appointmentDate }}</text>
          <text class="text-sm text-muted" style="display: block; margin-top: 4rpx;">陪诊时长：{{ item.durationText }}</text>
          <text v-if="isCompanionRole" class="text-sm text-muted" style="display: block; margin-top: 4rpx;">患者：{{ item.userName || '患者' }}</text>
        </view>
        <view class="flex flex-between items-center">
          <text class="text-base font-bold text-theme">¥{{ item.totalPrice }}</text>
          <view class="flex gap-sm">
            <button v-if="canPatientCancel(item)" size="mini" class="mini-action" @tap.stop="cancelOrder(item.id)">
              <image class="mini-icon" src="/static/icon_med/yiquxiao.png" mode="aspectFit" />
              <text>取消</text>
            </button>
            <button v-if="canPatientEvaluate(item)" size="mini" type="primary" plain class="mini-action" @tap.stop="goEvaluation(item.id)">
              <image class="mini-icon" src="/static/icon_med/zan.png" mode="aspectFit" />
              <text>评价</text>
            </button>
            <button v-if="canCompanionAccept(item)" size="mini" type="primary" class="mini-action" @tap.stop="acceptOrder(item.id)">
              <image class="mini-icon" src="/static/icon_med/wancheng.png" mode="aspectFit" />
              <text>接单</text>
            </button>
            <button v-if="canComplete(item)" size="mini" type="primary" class="mini-action" @tap.stop="completeOrder(item.id)">
              <image class="mini-icon" src="/static/icon_med/wancheng.png" mode="aspectFit" />
              <text>完成</text>
            </button>
            <button size="mini" type="primary" class="mini-action" @tap.stop="goDetail(item.id)">
              <image class="mini-icon" src="/static/icon_med/yulan.png" mode="aspectFit" />
              <text>查看</text>
            </button>
          </view>
        </view>
      </uni-card>
    </view>

    <view v-else style="margin-top: 80rpx; text-align: center;">
      <image class="empty-icon" src="/static/icon_med/dingdan.png" mode="aspectFit" />
      <text class="text-sm text-muted" style="display: block; margin: 16rpx 0;">{{ emptyText }}</text>
      <button v-if="isPatientRole" class="btn-primary order-top-btn" style="width: 280rpx;" @tap="goAppointment">
        <image class="btn-icon" src="/static/icon_med/rili.png" mode="aspectFit" />
        <text>去预约</text>
      </button>
      <button v-else-if="isCompanionRole" class="btn-primary order-top-btn" style="width: 280rpx;" @tap="refreshList">
        <image class="btn-icon" src="/static/icon_med/shuaxing.png" mode="aspectFit" />
        <text>刷新</text>
      </button>
    </view>
  </view>
</template>

<script>
import { departmentApi, hospitalApi, orderApi, userApi } from '@/utils/api.js';
import { formatDate } from '@/utils/format.js';
import { isCompanionCertified, normalizeRole as getNormalizedRole } from '@/utils/permission.js';

const PATIENT_STATUS_TABS = ['全部', '待接单', '待确认完成', '已接单', '已完成', '已取消'];
const PATIENT_STATUS_VALUES = ['all', 'pending', 'completion_pending', 'confirmed', 'completed', 'cancelled'];
const COMPANION_STATUS_TABS = ['待接单大厅', '指派待确认', '已接单', '待用户确认', '已完成', '已取消'];
const COMPANION_STATUS_VALUES = ['market', 'assigned_pending', 'confirmed', 'completion_pending', 'completed', 'cancelled'];

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
  data() {
    return {
      userRole: 'unknown',
      currentUser: null,
      currentTab: 0,
      orders: [],
      loading: false,
      hospitalNameMap: {},
      departmentNameMap: {},
      departmentLoadedHospitalMap: {}
    };
  },
  computed: {
    isPatientRole() {
      return this.userRole === 'patient';
    },
    isCompanionRole() {
      return this.userRole === 'companion';
    },
    statusTabs() {
      return this.isCompanionRole ? COMPANION_STATUS_TABS : PATIENT_STATUS_TABS;
    },
    statusValues() {
      return this.isCompanionRole ? COMPANION_STATUS_VALUES : PATIENT_STATUS_VALUES;
    },
    emptyText() {
      if (this.isPatientRole) return '暂无订单';
      if (this.statusValues[this.currentTab] === 'market') return '当前暂无可接订单';
      if (this.statusValues[this.currentTab] === 'assigned_pending') return '暂无指派给您的待确认订单';
      if (this.isCompanionRole) return '暂无服务订单';
      return '当前角色暂无订单视图';
    }
  },
  onShow() {
    this.loadOrders();
  },
  methods: {
    getOrderStatusText(order) {
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
        return this.isPatientRole ? '待确认完成' : '待用户确认';
      }
      return STATUS_TEXT[status] || '未知';
    },
    getOrderStatusColor(order) {
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
    normalizeRole(user) {
      return getNormalizedRole(user);
    },
    normalizeDuration(duration) {
      if (duration === null || duration === undefined || duration === '') return '-';
      const raw = String(duration).trim();
      if (!raw) return '-';
      if (/^\d+$/.test(raw)) return `${raw} 小时`;
      if (/^\d+(\.\d+)?h$/i.test(raw)) return `${raw.replace(/h$/i, '')} 小时`;
      return raw;
    },
    normalizeOrders(records) {
      return records.map((order) => ({
        ...order,
        statusText: this.getOrderStatusText(order),
        statusColor: this.getOrderStatusColor(order),
        createTimeFormatted: formatDate(order.createTime || order.appointmentDate),
        durationText: this.normalizeDuration(order.duration),
        hospitalName: order.hospitalName || '未指定医院',
        departmentName: order.departmentName || '未指定科室',
        appointmentDate: order.appointmentDate || '待确认',
        totalPrice: order.totalPrice || 0
      }));
    },
    toIdKey(id) {
      if (id === null || id === undefined || id === '') return '';
      const num = Number(id);
      if (Number.isNaN(num) || num <= 0) return '';
      return String(num);
    },
    async ensureHospitalMap() {
      if (Object.keys(this.hospitalNameMap).length > 0) return;
      const nextMap = { ...this.hospitalNameMap };
      const size = 100;
      for (let current = 1; current <= 10; current += 1) {
        const page = await hospitalApi.list({ current, size });
        const records = page?.records || [];
        records.forEach((item) => {
          const idKey = this.toIdKey(item.id);
          if (idKey && item.hospitalName) {
            nextMap[idKey] = item.hospitalName;
          }
        });
        if (records.length < size) break;
      }
      this.hospitalNameMap = nextMap;
    },
    async ensureDepartmentMapByHospitalId(hospitalId) {
      const idKey = this.toIdKey(hospitalId);
      if (!idKey) return;
      if (this.departmentLoadedHospitalMap[idKey]) return;
      const nextMap = { ...this.departmentNameMap };
      const size = 100;
      for (let current = 1; current <= 10; current += 1) {
        const page = await departmentApi.list({ current, size, hospitalId: idKey });
        const records = page?.records || [];
        records.forEach((item) => {
          const departmentIdKey = this.toIdKey(item.id);
          if (departmentIdKey && item.departmentName) {
            nextMap[departmentIdKey] = item.departmentName;
          }
        });
        if (records.length < size) break;
      }
      this.departmentNameMap = nextMap;
      this.departmentLoadedHospitalMap = {
        ...this.departmentLoadedHospitalMap,
        [idKey]: true
      };
    },
    async enrichOrdersWithNames(records) {
      if (!Array.isArray(records) || !records.length) return records || [];
      await this.ensureHospitalMap();
      const hospitalIdSet = new Set(
        records
          .filter((item) => !item.departmentName)
          .map((item) => this.toIdKey(item.hospitalId))
          .filter(Boolean)
      );
      if (hospitalIdSet.size > 0) {
        await Promise.all(Array.from(hospitalIdSet).map((hospitalId) => this.ensureDepartmentMapByHospitalId(hospitalId)));
      }
      return records.map((item) => {
        const hospitalId = this.toIdKey(item.hospitalId);
        const departmentId = this.toIdKey(item.departmentId);
        return {
          ...item,
          hospitalName: item.hospitalName || this.hospitalNameMap[hospitalId] || '',
          departmentName: item.departmentName || this.departmentNameMap[departmentId] || ''
        };
      });
    },
    async loadOrders() {
      let user = uni.getStorageSync('userInfo');
      try {
        const fresh = await userApi.fetchCurrentUser();
        if (fresh?.id) {
          user = fresh;
          uni.setStorageSync('userInfo', fresh);
        }
      } catch (error) {
        // ignore
      }
      if (!user?.id) {
        this.orders = [];
        return;
      }
      this.currentUser = user;
      this.userRole = this.normalizeRole(user);
      if (this.isCompanionRole) {
        uni.setNavigationBarTitle({ title: '接单中心' });
      } else {
        uni.setNavigationBarTitle({ title: '订单' });
      }
      if (!this.isPatientRole && !this.isCompanionRole) {
        this.orders = [];
        return;
      }
      if (this.currentTab >= this.statusValues.length) {
        this.currentTab = 0;
      }
      const status = this.statusValues[this.currentTab];
      this.loading = true;
      try {
        if (this.isCompanionRole) {
          let records = [];
          if (status === 'market') {
            const [pendingPage, confirmedPage] = await Promise.all([
              orderApi.list({
                current: 1,
                size: 20,
                orderStatus: 'pending'
              }),
              orderApi.list({
                current: 1,
                size: 20,
                orderStatus: 'confirmed'
              })
            ]);
            const raw = [...(pendingPage.records || []), ...(confirmedPage.records || [])];
            const uniqueMap = new Map(raw.map((item) => [String(item.id), item]));
            records = Array.from(uniqueMap.values()).filter((item) => !item.companionId && item.paymentStatus === 'paid');
          } else if (status === 'assigned_pending') {
            const page = await orderApi.list({
              current: 1,
              size: 20,
              companionId: user.id,
              orderStatus: 'pending'
            });
            records = (page.records || []).filter((item) => item.paymentStatus === 'paid');
          } else {
            if (status === 'confirmed') {
              const [confirmedPage, servingPage] = await Promise.all([
                orderApi.list({
                  current: 1,
                  size: 20,
                  companionId: user.id,
                  orderStatus: 'confirmed'
                }),
                orderApi.list({
                  current: 1,
                  size: 20,
                  companionId: user.id,
                  orderStatus: 'serving'
                })
              ]);
              const uniqueMap = new Map(
                [...(confirmedPage.records || []), ...(servingPage.records || [])].map((item) => [String(item.id), item])
              );
              records = Array.from(uniqueMap.values());
            } else {
              const page = await orderApi.list({
                current: 1,
                size: 20,
                companionId: user.id,
                orderStatus: status
              });
              records = page.records || [];
            }
          }
          const enrichedRecords = await this.enrichOrdersWithNames(records);
          this.orders = this.normalizeOrders(enrichedRecords);
        } else {
          let records = [];
          if (status === 'pending') {
            const [pendingPage, confirmedPage] = await Promise.all([
              orderApi.list({
                current: 1,
                size: 20,
                userId: user.id,
                orderStatus: 'pending'
              }),
              orderApi.list({
                current: 1,
                size: 20,
                userId: user.id,
                orderStatus: 'confirmed'
              })
            ]);
            const pendingRecords = pendingPage.records || [];
            const legacyPendingRecords = (confirmedPage.records || []).filter((item) => !item.companionId);
            const uniqueMap = new Map([...pendingRecords, ...legacyPendingRecords].map((item) => [String(item.id), item]));
            records = Array.from(uniqueMap.values());
          } else {
            if (status === 'confirmed') {
              const [confirmedPage, servingPage] = await Promise.all([
                orderApi.list({
                  current: 1,
                  size: 20,
                  userId: user.id,
                  orderStatus: 'confirmed'
                }),
                orderApi.list({
                  current: 1,
                  size: 20,
                  userId: user.id,
                  orderStatus: 'serving'
                })
              ]);
              const uniqueMap = new Map(
                [...(confirmedPage.records || []), ...(servingPage.records || [])].map((item) => [String(item.id), item])
              );
              records = Array.from(uniqueMap.values());
            } else {
              const page = await orderApi.list({
                current: 1,
                size: 20,
                userId: user.id,
                orderStatus: status === 'all' ? undefined : status
              });
              records = page.records || [];
            }
          }
          const enrichedRecords = await this.enrichOrdersWithNames(records);
          this.orders = this.normalizeOrders(enrichedRecords);
        }
      } catch (error) {
        console.error('加载订单失败', error);
      } finally {
        this.loading = false;
      }
    },
    filterStatus(e) {
      this.currentTab = e.currentIndex;
      this.loadOrders();
    },
    goDetail(id) {
      uni.navigateTo({ url: `/pages/order/detail?id=${id}` });
    },
    goAppointment() {
      if (!this.isPatientRole) {
        uni.showToast({ title: '当前角色不支持发单预约', icon: 'none' });
        return;
      }
      uni.navigateTo({ url: '/pages/appointment/index' });
    },
    goEvaluation(id) {
      if (!this.isPatientRole) return;
      uni.navigateTo({ url: `/pages/evaluation/index?orderId=${id}` });
    },
    cancelOrder(id) {
      if (!this.isPatientRole) return;
      uni.showModal({
        title: '取消订单',
        content: '确定要取消该订单吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await orderApi.cancel(id, '用户主动取消');
              uni.showToast({ title: '已取消', icon: 'success' });
              this.loadOrders();
            } catch (error) {
              uni.showToast({ title: error.message || '取消失败', icon: 'none' });
            }
          }
        }
      });
    },
    acceptOrder(id) {
      const user = uni.getStorageSync('userInfo');
      if (!user?.id) return;
      if (!this.ensureCompanionCertified(user)) return;
      uni.showModal({
        title: '确认接单',
        content: `确定接订单 #${id} 吗？`,
        success: async (res) => {
          if (!res.confirm) return;
          try {
            await orderApi.accept(id, user.id);
            uni.showToast({ title: '接单成功', icon: 'success' });
            this.loadOrders();
          } catch (error) {
            uni.showToast({ title: error.message || '接单失败', icon: 'none' });
          }
        }
      });
    },
    ensureCompanionCertified(user) {
      if (!this.isCompanionRole) return true;
      if (isCompanionCertified(user)) return true;
      uni.showModal({
        title: '请先完成认证',
        content: '完成实名认证与资质认证后才可接单，是否前往认证中心？',
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({ url: '/pages/companion/certification' });
          }
        }
      });
      return false;
    },
    canPatientCancel(item) {
      return this.isPatientRole
        && item.orderStatus === 'pending';
    },
    canPatientEvaluate(item) {
      return this.isPatientRole && item.orderStatus === 'completed';
    },
    canComplete(item) {
      if (!item || item.paymentStatus !== 'paid') return false;
      if (this.isPatientRole) {
        return item.orderStatus === 'completion_pending' && Number(item.userId) === Number(this.currentUser?.id);
      }
      if (this.isCompanionRole) {
        return ['confirmed', 'serving'].includes(item.orderStatus)
          && Number(item.companionId) === Number(this.currentUser?.id);
      }
      return false;
    },
    canCompanionAccept(item) {
      if (!this.isCompanionRole || !this.currentUser?.id || !item) return false;
      const isPending = item.orderStatus === 'pending';
      const isLegacyUnassignedConfirmed = item.orderStatus === 'confirmed' && !item.companionId;
      if (!isPending && !isLegacyUnassignedConfirmed) return false;
      if (item.paymentStatus !== 'paid') return false;
      if (!item.companionId) return true;
      return Number(item.companionId) === Number(this.currentUser.id);
    },
    completeOrder(id) {
      const actionText = this.isCompanionRole ? '发起完成申请' : '确认订单完成';
      const actionRequest = this.isCompanionRole ? () => orderApi.complete(id) : () => orderApi.confirmComplete(id);
      const successText = this.isCompanionRole ? '已提交，等待用户确认' : '订单已完成';
      uni.showModal({
        title: '完成订单',
        content: this.isCompanionRole ? '确认发起完成申请，等待用户确认吗？' : '确认该订单服务已结束并完成吗？',
        success: async (res) => {
          if (!res.confirm) return;
          try {
            await actionRequest();
            uni.showToast({ title: successText, icon: 'success' });
            this.loadOrders();
          } catch (error) {
            uni.showToast({ title: error.message || `${actionText}失败`, icon: 'none' });
          }
        }
      });
    },
    refreshList() {
      this.loadOrders();
    },
    showDrafts() {
      if (!this.isPatientRole) {
        uni.showToast({ title: '当前角色无草稿箱', icon: 'none' });
        return;
      }
      uni.navigateTo({ url: '/pages/order/drafts' });
    },
    showCompanionGuide() {
      uni.showToast({ title: '可在大厅抢单，或处理“指派待确认”订单', icon: 'none' });
    }
  }
};
</script>

<style scoped lang="scss">
@import "@/styles/common.scss";

.order-top-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
}

.btn-icon {
  width: 28rpx;
  height: 28rpx;
}

.mini-action {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
}

.mini-icon {
  width: 20rpx;
  height: 20rpx;
}

.empty-icon {
  width: 86rpx;
  height: 86rpx;
}
</style>
