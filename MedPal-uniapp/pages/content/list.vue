<template>
  <view class="page-content">
    <view class="top-card">
      <view class="title-row">
        <text class="page-title">健康科普与公告</text>
        <text class="count-text">{{ list.length }} 条</text>
      </view>
      <uni-search-bar
        v-model="keyword"
        placeholder="搜索标题"
        cancel-button="none"
        @confirm="onSearch"
        @clear="onSearch"
      />
      <uni-segmented-control
        :values="tabLabels"
        :current="tabIndex"
        style-type="button"
        active-color="#2f65f9"
        @clickItem="onTabChange"
      />
    </view>

    <view v-if="list.length" class="list-wrap">
      <uni-card
        v-for="item in list"
        :key="item.id"
        :border="false"
        padding="20"
        class="article-card"
        @tap="goDetail(item.id)"
      >
        <view class="card-head">
          <text class="card-title">{{ item.title }}</text>
          <uni-tag :text="item.type === 'announcement' ? '公告' : '科普'" :type="item.type === 'announcement' ? 'warning' : 'primary'" size="small" />
        </view>
        <text class="card-summary">{{ item.summary || buildFallbackSummary(item.content) }}</text>
        <view class="card-foot">
          <text class="time-text">{{ formatPublishTime(item.publishTime || item.createTime) }}</text>
          <text class="view-link">查看详情</text>
        </view>
      </uni-card>
    </view>

    <view v-else class="empty-box">
      <uni-icons type="paperplane" size="56" color="#d8deeb"></uni-icons>
      <text class="empty-text">暂无内容</text>
    </view>

    <view v-if="hasMore && list.length" class="load-more">
      <text class="load-more-text" @tap="loadMore">加载更多</text>
    </view>
  </view>
</template>

<script>
import { contentApi } from '@/utils/api.js';
import { formatDateTime } from '@/utils/format.js';

const TAB_VALUES = ['article', 'announcement'];
const TAB_LABELS = ['科普文章', '平台公告'];

export default {
  data() {
    return {
      tabIndex: 0,
      tabLabels: TAB_LABELS,
      keyword: '',
      list: [],
      current: 1,
      pageSize: 10,
      hasMore: true,
      loading: false
    };
  },
  onLoad() {
    this.fetchList(true);
  },
  onPullDownRefresh() {
    this.fetchList(true).finally(() => uni.stopPullDownRefresh());
  },
  methods: {
    formatPublishTime(time) {
      if (!time) return '刚刚';
      return formatDateTime(time);
    },
    buildFallbackSummary(content) {
      if (!content) return '暂无摘要';
      return String(content)
        .replace(/[#>*`_\-\[\]\(\)]/g, ' ')
        .replace(/\s+/g, ' ')
        .trim()
        .slice(0, 56) || '暂无摘要';
    },
    async fetchList(reset = false) {
      if (this.loading) return;
      this.loading = true;
      if (reset) {
        this.current = 1;
        this.hasMore = true;
      }
      try {
        const res = await contentApi.list({
          current: this.current,
          size: this.pageSize,
          type: TAB_VALUES[this.tabIndex],
          keyword: this.keyword.trim() || undefined
        });
        const page = res || {};
        const records = page.records || [];
        if (reset) {
          this.list = records;
        } else {
          this.list = [...this.list, ...records];
        }
        this.hasMore = records.length === this.pageSize;
      } catch (error) {
        uni.showToast({ title: error.message || '加载失败', icon: 'none' });
      } finally {
        this.loading = false;
      }
    },
    onSearch() {
      this.fetchList(true);
    },
    onTabChange(e) {
      this.tabIndex = e.currentIndex;
      this.fetchList(true);
    },
    loadMore() {
      if (!this.hasMore || this.loading) return;
      this.current += 1;
      this.fetchList(false);
    },
    goDetail(id) {
      uni.navigateTo({ url: `/pages/content/detail?id=${id}` });
    }
  }
};
</script>

<style scoped lang="scss">
@import "@/styles/common.scss";

.top-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 20rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.page-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.count-text {
  font-size: 22rpx;
  color: #7b8291;
}

.list-wrap {
  margin-top: 16rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.article-card {
  margin: 0;
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12rpx;
}

.card-title {
  flex: 1;
  font-size: 28rpx;
  font-weight: 600;
  color: #1c2b4a;
}

.card-summary {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #59627a;
  line-height: 1.6;
}

.card-foot {
  margin-top: 14rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time-text {
  font-size: 22rpx;
  color: #8b95ab;
}

.view-link {
  font-size: 24rpx;
  color: #2f65f9;
}

.empty-box {
  margin-top: 140rpx;
  text-align: center;
}

.empty-text {
  display: block;
  margin-top: 16rpx;
  color: #9aa4b8;
  font-size: 24rpx;
}

.load-more {
  margin: 20rpx 0 10rpx;
  text-align: center;
}

.load-more-text {
  color: #2f65f9;
  font-size: 24rpx;
}
</style>
