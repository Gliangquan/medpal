<template>
  <view class="page-content">
    <!-- 顶部统计 - 改为白色卡片风格 -->
    <uni-card :border="false" padding="24">
      <view class="flex flex-between items-center">
        <view style="text-align: center; flex: 1;">
          <text class="text-lg font-bold text-theme" style="display: block;">{{ unreadCount }}</text>
          <text class="text-sm text-muted" style="display: block; margin-top: 4rpx;">未读消息</text>
        </view>
        <view style="width: 2rpx; height: 50rpx; background: #eee;"></view>
        <view style="text-align: center; flex: 1;">
          <text class="text-lg font-bold text-primary" style="display: block;">{{ notifications.length }}</text>
          <text class="text-sm text-muted" style="display: block; margin-top: 4rpx;">全部消息</text>
        </view>
      </view>
      <view class="flex gap-md" style="margin-top: 20rpx; padding-top: 20rpx; border-top: 1rpx solid #f0f0f0;">
        <button size="mini" type="primary" style="flex: 1;" @tap="markAll" v-if="unreadCount > 0">
          <uni-icons type="checkmarkempty" size="12" color="#fff"></uni-icons> 全部已读
        </button>
        <button size="mini" style="flex: 1; background: #fff; color: #ff5c5c; border: 1rpx solid #ff5c5c;" @tap="deleteAllRead" v-if="readCount > 0">
          <uni-icons type="trash" size="12" color="#ff5c5c"></uni-icons> 删除已读
        </button>
      </view>
    </uni-card>

    <!-- 消息类型筛选 - 使用uni-tag -->
    <view style="margin: 16rpx 0; padding: 0 8rpx;">
      <scroll-view scroll-x style="white-space: nowrap;">
        <view style="display: inline-flex; gap: 12rpx; padding: 8rpx;">
          <view 
            v-for="(item, index) in typeFilters" 
            :key="index"
            style="position: relative;"
            @tap="switchType(item.value)"
          >
            <uni-tag 
              :text="item.label" 
              :type="currentType === item.value ? 'primary' : 'default'"
              size="normal"
              style="margin-right: 8rpx;"
            />
            <view 
              v-if="item.badge > 0" 
              style="position: absolute; top: -8rpx; right: 0; min-width: 28rpx; height: 28rpx; background: #ff5c5c; color: #fff; font-size: 18rpx; border-radius: 14rpx; display: flex; align-items: center; justify-content: center; padding: 0 6rpx; z-index: 1;"
            >{{ item.badge > 99 ? '99+' : item.badge }}</view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 消息列表 -->
    <view v-if="filteredNotifications.length" style="display: flex; flex-direction: column; gap: 12rpx;">
      <uni-card
        v-for="(item, index) in filteredNotifications" 
        :key="item.id" 
        :border="false"
        padding="20"
        @tap="openNotice(index)"
        @longpress="showActionMenu(index)"
      >
        <view class="flex">
          <view style="width: 64rpx; height: 64rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin-right: 16rpx; flex-shrink: 0;" :style="{ background: getIconBg(item.type) }">
            <uni-icons :type="getIconType(item.type)" size="18" :color="getIconColor(item.type)"></uni-icons>
          </view>
          <view style="flex: 1; min-width: 0;">
            <view class="flex flex-between items-center" style="margin-bottom: 8rpx;">
              <text class="text-base font-semibold" :class="item.status !== 'read' ? 'text-primary' : 'text-secondary'" style="flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ item.title }}</text>
              <view class="flex items-center gap-sm" style="margin-left: 12rpx; flex-shrink: 0;">
                <view v-if="item.status !== 'read'" style="width: 12rpx; height: 12rpx; background: #ff5c5c; border-radius: 50%;"></view>
                <text class="text-sm text-muted">{{ item.timeText }}</text>
              </view>
            </view>
            <text class="text-sm text-secondary" style="line-height: 1.5;">{{ item.content }}</text>
            <view v-if="item.relatedType === 'order'" class="flex items-center gap-sm" style="margin-top: 12rpx;">
              <text class="text-sm text-theme" style="padding: 4rpx 12rpx; background: rgba(47, 101, 249, 0.08); border-radius: 20rpx;">
                <uni-icons type="shop" size="12" color="#2f65f9"></uni-icons> 查看订单
              </text>
            </view>
          </view>
        </view>
      </uni-card>
    </view>

    <!-- 空状态 -->
    <view v-else style="margin-top: 160rpx; text-align: center;">
      <uni-icons type="notification" size="64" color="#ddd"></uni-icons>
      <text class="text-base font-semibold text-primary" style="display: block; margin-top: 24rpx;">暂无消息</text>
      <text class="text-sm text-muted" style="display: block; margin-top: 8rpx;">{{ currentType === 'all' ? '您还没有收到任何消息' : '该分类下暂无消息' }}</text>
    </view>

    <!-- 加载更多 -->
    <view v-if="hasMore && filteredNotifications.length > 0" style="text-align: center; padding: 24rpx;">
      <text class="text-sm text-theme" @tap="loadMore">加载更多</text>
    </view>
  </view>
</template>

<script>
import { notificationApi } from '@/utils/api.js';
import { formatRelative } from '@/utils/format.js';

export default {
  data() {
    return {
      notifications: [],
      currentType: 'all',
      currentPage: 1,
      pageSize: 20,
      hasMore: true,
      loading: false,
      typeFilters: [
        { label: '全部', value: 'all', badge: 0 },
        { label: '订单', value: 'order', badge: 0 },
        { label: '系统', value: 'system', badge: 0 },
        { label: '活动', value: 'activity', badge: 0 }
      ]
    };
  },
  computed: {
    filteredNotifications() {
      if (this.currentType === 'all') {
        return this.notifications;
      }
      return this.notifications.filter(item => item.type === this.currentType);
    },
    unreadCount() {
      return this.notifications.filter(item => item.status !== 'read').length;
    },
    readCount() {
      return this.notifications.filter(item => item.status === 'read').length;
    }
  },
  onShow() {
    this.refreshNotifications();
  },
  onPullDownRefresh() {
    this.refreshNotifications();
    uni.stopPullDownRefresh();
  },
  methods: {
    async refreshNotifications() {
      this.currentPage = 1;
      await this.loadNotifications();
    },
    async loadNotifications() {
      const user = uni.getStorageSync('userInfo');
      if (!user?.id) {
        this.notifications = [];
        return;
      }
      
      this.loading = true;
      try {
        const res = await notificationApi.list({ 
          current: this.currentPage, 
          size: this.pageSize, 
          userId: user.id 
        });
        
        const records = Array.isArray(res) ? res : (res.records || res.data || []);
        const list = records.map((item) => ({
          ...item,
          timeText: formatRelative(item.createTime)
        }));
        
        if (this.currentPage === 1) {
          this.notifications = list;
        } else {
          this.notifications = [...this.notifications, ...list];
        }
        
        this.hasMore = list.length === this.pageSize;
        this.updateTypeBadges();
      } catch (error) {
        console.error('加载通知失败', error);
        uni.showToast({ title: '加载失败', icon: 'none' });
      } finally {
        this.loading = false;
      }
    },
    updateTypeBadges() {
      this.typeFilters.forEach(filter => {
        if (filter.value === 'all') {
          filter.badge = this.unreadCount;
        } else {
          filter.badge = this.notifications.filter(
            item => item.type === filter.value && item.status !== 'read'
          ).length;
        }
      });
    },
    switchType(type) {
      this.currentType = type;
    },
    async loadMore() {
      if (!this.hasMore || this.loading) return;
      this.currentPage++;
      await this.loadNotifications();
    },
    async openNotice(index) {
      const notice = this.filteredNotifications[index];
      if (!notice || !notice.id) return;
      
      if (notice.status !== 'read') {
        try {
          await notificationApi.markAsRead(notice.id);
          notice.status = 'read';
          this.updateTypeBadges();
        } catch (error) {
          console.error('标记已读失败', error);
        }
      }
      
      uni.showModal({
        title: notice.title || '消息详情',
        content: notice.content || '',
        showCancel: notice.relatedType === 'order' || notice.relatedType === 'content',
        cancelText: '关闭',
        confirmText: notice.relatedType === 'order'
          ? '查看订单'
          : (notice.relatedType === 'content' ? '查看内容' : '确定'),
        success: (res) => {
          if (res.confirm && notice.relatedType === 'order' && notice.relatedId) {
            uni.navigateTo({ url: `/pages/order/detail?id=${notice.relatedId}` });
          }
          if (res.confirm && notice.relatedType === 'content' && notice.relatedId) {
            uni.navigateTo({ url: `/pages/content/detail?id=${notice.relatedId}` });
          }
        }
      });
    },
    showActionMenu(index) {
      const notice = this.filteredNotifications[index];
      const itemList = ['删除消息'];
      if (notice.status !== 'read') {
        itemList.unshift('标记为已读');
      }
      
      uni.showActionSheet({
        itemList,
        success: async (res) => {
          if (res.tapIndex === 0 && notice.status !== 'read') {
            await this.markSingleRead(notice);
          } else {
            await this.deleteNotification(notice.id);
          }
        }
      });
    },
    async markSingleRead(notice) {
      try {
        await notificationApi.markAsRead(notice.id);
        notice.status = 'read';
        this.updateTypeBadges();
        uni.showToast({ title: '已标记', icon: 'success' });
      } catch (error) {
        uni.showToast({ title: '操作失败', icon: 'none' });
      }
    },
    markAll() {
      const user = uni.getStorageSync('userInfo');
      if (!user?.id || this.unreadCount === 0) return;
      
      uni.showModal({
        title: '标记全部已读',
        content: `确定将 ${this.unreadCount} 条未读消息标记为已读吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              await notificationApi.markAllAsRead(user.id);
              this.notifications.forEach(item => item.status = 'read');
              this.updateTypeBadges();
              uni.showToast({ title: '全部已读', icon: 'success' });
            } catch (error) {
              uni.showToast({ title: '操作失败', icon: 'none' });
            }
          }
        }
      });
    },
    deleteAllRead() {
      if (this.readCount === 0) return;
      
      uni.showModal({
        title: '删除已读消息',
        content: `确定删除 ${this.readCount} 条已读消息吗？`,
        confirmColor: '#ff5c5c',
        success: async (res) => {
          if (res.confirm) {
            this.notifications = this.notifications.filter(item => item.status !== 'read');
            uni.showToast({ title: '已删除', icon: 'success' });
          }
        }
      });
    },
    async deleteNotification(id) {
      this.notifications = this.notifications.filter(item => item.id !== id);
      uni.showToast({ title: '已删除', icon: 'success' });
    },
    getIconType(type) {
      const iconMap = {
        order: 'shop',
        system: 'info',
        activity: 'gift',
        default: 'notification'
      };
      return iconMap[type] || iconMap.default;
    },
    getIconColor(type) {
      const colorMap = {
        order: '#2f65f9',
        system: '#7b8291',
        activity: '#ff9500',
        default: '#7b8291'
      };
      return colorMap[type] || colorMap.default;
    },
    getIconBg(type) {
      const bgMap = {
        order: 'rgba(47, 101, 249, 0.1)',
        system: 'rgba(123, 130, 145, 0.1)',
        activity: 'rgba(255, 149, 0, 0.1)',
        default: 'rgba(123, 130, 145, 0.1)'
      };
      return bgMap[type] || bgMap.default;
    }
  }
};
</script>

<style lang="scss">
@import "@/styles/common.scss";
</style>
