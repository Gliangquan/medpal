<template>
  <view class="page">
    <view class="hero-card">
      <text class="hero-title">收入统计</text>
      <text class="hero-desc">查看每笔陪诊订单的收入、结算状态和累计收入。</text>
    </view>

    <view class="summary-grid">
      <view class="summary-card primary">
        <text class="summary-label">累计收入</text>
        <text class="summary-value">¥{{ summary.amount || 0 }}</text>
      </view>
      <view class="summary-card">
        <text class="summary-label">已结算</text>
        <text class="summary-value">¥{{ summary.settledIncome || 0 }}</text>
      </view>
      <view class="summary-card">
        <text class="summary-label">待结算</text>
        <text class="summary-value">¥{{ summary.pendingSettlementIncome || 0 }}</text>
      </view>
      <view class="summary-card">
        <text class="summary-label">订单数</text>
        <text class="summary-value">{{ summary.orderCount || 0 }}</text>
      </view>
    </view>

    <view class="filter-card">
      <view class="filter-head">
        <text class="filter-title">统计周期</text>
      </view>
      <uni-segmented-control
        :values="rangeTabs"
        :current="rangeIndex"
        style-type="button"
        active-color="#2f65f9"
        @clickItem="changeRange"
      />
      <view class="rule-box">
        <text class="rule-text">结算规则：{{ settlementRule.settlementCycle || '月结' }} · 平台费率 {{ settlementRule.serviceFeeRate || 0 }}</text>
      </view>
    </view>

    <view class="list-card">
      <view class="list-head">
        <text class="list-title">收入明细</text>
      </view>
      <view v-if="details.length" class="detail-list">
        <view v-for="item in details" :key="item.orderId" class="detail-item">
          <view class="detail-top">
            <view>
              <text class="order-no">{{ item.orderNo || ('#' + item.orderId) }}</text>
              <text class="order-time">{{ formatTime(item.appointmentDate || item.createTime) }}</text>
            </view>
            <text class="income-amount">¥{{ item.incomeAmount || 0 }}</text>
          </view>
          <text class="detail-line">{{ item.hospitalName || '未填写医院' }} · {{ item.departmentName || '未填写科室' }}</text>
          <view class="tag-row">
            <view class="status-pill" :class="settlementClass(item.settlementStatus)">
              {{ item.settlementStatusText || '待结算' }}
            </view>
            <text class="fee-text">服务费 ¥{{ item.serviceFee || 0 }} / 平台费 ¥{{ item.platformFee || 0 }}</text>
          </view>
          <text v-if="item.settlementTime" class="settlement-time">结算时间：{{ formatTime(item.settlementTime) }}</text>
        </view>
      </view>
      <view v-else class="empty-box">
        <text class="empty-text">暂无收入明细</text>
      </view>
    </view>
  </view>
</template>

<script>
import { incomeApi } from '@/utils/api.js';
import { formatDateTime } from '@/utils/format.js';

export default {
  data() {
    return {
      rangeTabs: ['近7天', '近30天', '全部'],
      rangeIndex: 1,
      summary: {},
      details: [],
      settlementRule: {}
    };
  },
  onShow() {
    this.loadIncomeData();
  },
  methods: {
    formatTime(value) {
      return formatDateTime(value) || '刚刚';
    },
    rangeType() {
      if (this.rangeIndex === 0) return 'week';
      if (this.rangeIndex === 1) return 'month';
      return 'all';
    },
    settlementClass(status) {
      if (status === 'settled') return 'settled';
      if (status === 'pending_settlement') return 'pending';
      return 'serving';
    },
    async loadIncomeData() {
      try {
        const [total, details, rule] = await Promise.all([
          incomeApi.total(),
          incomeApi.detail(),
          incomeApi.settlementRule()
        ]);
        this.summary = total || {};
        this.details = details || [];
        this.settlementRule = rule || {};
      } catch (error) {
        uni.showToast({ title: error.message || '加载收入失败', icon: 'none' });
      }
    },
    async changeRange(e) {
      this.rangeIndex = e.currentIndex;
      try {
        const type = this.rangeType();
        const [stats, details] = await Promise.all([
          incomeApi.statistics({ type }),
          incomeApi.detail()
        ]);
        this.summary = {
          amount: stats.totalIncome || 0,
          settledIncome: stats.settledIncome || 0,
          pendingSettlementIncome: stats.pendingSettlementIncome || 0,
          orderCount: stats.orderCount || 0
        };
        this.details = details || [];
      } catch (error) {
        uni.showToast({ title: error.message || '切换失败', icon: 'none' });
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

.hero-card,
.filter-card,
.list-card {
  background: #fff;
  border-radius: 22rpx;
  padding: 22rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.filter-card,
.list-card {
  margin-top: 18rpx;
}

.hero-title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.hero-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #6f7c95;
  line-height: 1.6;
}

.summary-grid {
  margin-top: 18rpx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14rpx;
}

.summary-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 20rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.summary-card.primary {
  background: linear-gradient(145deg, #2f65f9, #5a84ff);
}

.summary-label {
  display: block;
  font-size: 22rpx;
  color: #7b8291;
}

.summary-card.primary .summary-label,
.summary-card.primary .summary-value {
  color: #fff;
}

.summary-value {
  display: block;
  margin-top: 10rpx;
  font-size: 34rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.filter-title,
.list-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.rule-box {
  margin-top: 14rpx;
}

.rule-text,
.order-time,
.detail-line,
.fee-text,
.settlement-time,
.empty-text {
  font-size: 21rpx;
  color: #7b8291;
}

.detail-list {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.detail-item {
  border-radius: 18rpx;
  background: #f7f9ff;
  border: 1rpx solid #e5ebfb;
  padding: 18rpx;
  margin-top: 14rpx;
}

.detail-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12rpx;
}

.order-no {
  display: block;
  font-size: 24rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.order-time {
  display: block;
  margin-top: 6rpx;
}

.income-amount {
  font-size: 30rpx;
  font-weight: 700;
  color: #1948be;
}

.tag-row {
  margin-top: 12rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  align-items: center;
}

.status-pill {
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  font-size: 21rpx;
}

.status-pill.settled {
  background: rgba(24, 160, 88, 0.12);
  color: #18a058;
}

.status-pill.pending {
  background: rgba(255, 149, 0, 0.12);
  color: #ff9500;
}

.status-pill.serving {
  background: rgba(47, 101, 249, 0.12);
  color: #2f65f9;
}

.empty-box {
  padding: 26rpx 0 12rpx;
  text-align: center;
}
</style>
