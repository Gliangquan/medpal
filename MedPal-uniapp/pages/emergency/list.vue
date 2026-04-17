<template>
  <view class="page">
    <view class="hero-card">
      <text class="hero-title">紧急联动工作台</text>
      <text class="hero-sub">查看最新求助、及时响应、反馈处理进度</text>
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

    <view v-if="filteredList.length" class="list">
      <view v-for="item in filteredList" :key="item.id" class="card">
        <view class="card-head">
          <view>
            <text class="card-title">求助 #{{ item.id }}</text>
            <text class="card-time">{{ item.createTimeText }}</text>
          </view>
          <text class="status-tag" :class="item.status">{{ item.statusText }}</text>
        </view>

        <view class="card-section">
          <text class="label">位置</text>
          <text class="value">{{ item.location || '未获取到位置' }}</text>
        </view>

        <view class="card-section">
          <text class="label">求助内容</text>
          <text class="value">{{ item.content || '未填写' }}</text>
        </view>

        <view v-if="item.status === 'responded' && item.responderName" class="card-section compact">
          <text class="label">当前响应</text>
          <text class="value">{{ item.responderName }}</text>
        </view>

        <view class="action-row">
          <button
            v-if="item.status === 'pending'"
            class="btn-primary"
            @tap="respondHelp(item)"
          >立即响应</button>
          <button
            v-if="item.status === 'responded' && isCurrentResponder(item)"
            class="btn-primary"
            @tap="resolveHelp(item)"
          >标记已处理</button>
          <button class="btn-secondary" @tap="openDetail(item)">查看详情</button>
        </view>
      </view>
    </view>

    <view v-else class="empty-box">
      <text class="empty-title">暂无相关求助</text>
      <text class="empty-sub">当前筛选条件下没有需要处理的紧急求助</text>
    </view>
  </view>
</template>

<script>
import { emergencyApi, userApi } from '@/utils/api.js';
import { formatDateTime } from '@/utils/format.js';
import { isCompanionRole } from '@/utils/permission.js';

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
  methods: {
    async loadList() {
      try {
        const page = await emergencyApi.list({ current: 1, size: 50 });
        const records = page?.records || [];
        this.list = records.map((item) => ({
          ...item,
          statusText: STATUS_TEXT[item.status] || '未知',
          createTimeText: formatDateTime(item.createTime) || '刚刚',
          responderName: item.responderId && Number(item.responderId) === Number(this.currentUser?.id) ? '我' : (item.responderId ? `陪诊员 #${item.responderId}` : '')
        }));
      } catch (error) {
        uni.showToast({ title: error.message || '加载失败', icon: 'none' });
      }
    },
    switchStatus(status) {
      this.currentStatus = status;
    },
    isCurrentResponder(item) {
      return Number(item.responderId) === Number(this.currentUser?.id);
    },
    async respondHelp(item) {
      uni.showModal({
        title: '确认响应',
        content: '响应后，患者将收到“已有陪诊员接收求助”的提示。',
        success: async (res) => {
          if (!res.confirm) return;
          try {
            await emergencyApi.respond(item.id);
            uni.showToast({ title: '响应成功', icon: 'success' });
            this.loadList();
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
        placeholderText: '可填写处理说明，如：已电话联系并引导前往急诊',
        success: async (res) => {
          if (!res.confirm) return;
          try {
            await emergencyApi.resolve(item.id, res.content || '已联系并处理');
            uni.showToast({ title: '已标记处理完成', icon: 'success' });
            this.loadList();
          } catch (error) {
            uni.showToast({ title: error.message || '处理失败', icon: 'none' });
          }
        }
      });
    },
    openDetail(item) {
      uni.showModal({
        title: `求助 #${item.id}`,
        content: `位置：${item.location || '未获取'}\n状态：${item.statusText}\n内容：${item.content || '未填写'}${item.resolveNote ? `\n处理说明：${item.resolveNote}` : ''}`,
        showCancel: false
      });
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
  background: linear-gradient(145deg, #ffefef, #fff7f7);
  border-radius: 22rpx;
  padding: 22rpx;
  box-shadow: 0 10rpx 24rpx rgba(200, 59, 59, 0.08);
}

.hero-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #c83b3b;
}

.hero-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #8c6262;
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

.list {
  margin-top: 18rpx;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.card {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 18rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.card-head {
  display: flex;
  justify-content: space-between;
  gap: 12rpx;
}

.card-title {
  display: block;
  font-size: 26rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.card-time {
  display: block;
  margin-top: 8rpx;
  font-size: 21rpx;
  color: #8d97a9;
}

.status-tag {
  font-size: 22rpx;
  font-weight: 600;
}

.status-tag.pending {
  color: #ff8a00;
}

.status-tag.responded {
  color: #2f65f9;
}

.status-tag.resolved {
  color: #6f7a8d;
}

.card-section {
  margin-top: 14rpx;
}

.card-section.compact {
  margin-top: 10rpx;
}

.label {
  display: block;
  font-size: 21rpx;
  color: #8d97a9;
}

.value {
  display: block;
  margin-top: 6rpx;
  font-size: 23rpx;
  line-height: 1.6;
  color: #1c2b4a;
}

.action-row {
  margin-top: 16rpx;
  display: flex;
  gap: 12rpx;
}

.btn-primary,
.btn-secondary {
  flex: 1;
  border-radius: 999rpx;
  font-size: 23rpx;
}

.btn-primary {
  background: #ff5c5c;
  color: #ffffff;
}

.btn-secondary {
  background: #ffffff;
  color: #2f65f9;
  border: 1rpx solid rgba(47, 101, 249, 0.25);
}

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
</style>
