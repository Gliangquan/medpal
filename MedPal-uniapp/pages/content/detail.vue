<template>
  <view class="page-content">
    <view v-if="loading" class="loading-box">
      <uni-load-more status="loading" />
    </view>

    <view v-else-if="!detail" class="empty-box">
      <uni-icons type="info" size="48" color="#d8deeb"></uni-icons>
      <text class="empty-text">内容不存在或已下架</text>
      <button class="btn-primary" style="width: 260rpx; margin-top: 20rpx;" @tap="goBack">返回列表</button>
    </view>

    <view v-else>
      <view class="header-card">
        <view class="head-top">
          <uni-tag :text="detail.type === 'announcement' ? '平台公告' : '健康科普'" :type="detail.type === 'announcement' ? 'warning' : 'primary'" size="small" />
          <text class="view-count">{{ detail.viewCount || 0 }} 阅读</text>
        </view>
        <text class="title">{{ detail.title }}</text>
        <text class="meta">{{ formatPublishTime(detail.publishTime || detail.createTime) }}</text>
      </view>

      <view class="content-card">
        <rich-text :nodes="renderNodes"></rich-text>
      </view>
    </view>
  </view>
</template>

<script>
import { contentApi } from '@/utils/api.js';
import { formatDateTime } from '@/utils/format.js';

export default {
  data() {
    return {
      loading: false,
      detail: null
    };
  },
  computed: {
    renderNodes() {
      return this.markdownToHtml(this.detail?.content || '');
    }
  },
  onLoad(options) {
    if (!options?.id) {
      this.detail = null;
      return;
    }
    this.fetchDetail(options.id);
  },
  methods: {
    formatPublishTime(time) {
      if (!time) return '刚刚';
      return formatDateTime(time);
    },
    async fetchDetail(id) {
      this.loading = true;
      try {
        const res = await contentApi.detail(id);
        this.detail = res || null;
      } catch (error) {
        this.detail = null;
        uni.showToast({ title: error.message || '加载失败', icon: 'none' });
      } finally {
        this.loading = false;
      }
    },
    goBack() {
      const pages = getCurrentPages();
      if (pages.length > 1) {
        uni.navigateBack();
      } else {
        uni.navigateTo({ url: '/pages/content/list' });
      }
    },
    markdownToHtml(md) {
      const escape = (text) => String(text || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
      const inline = (line) => line
        .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
        .replace(/__(.+?)__/g, '<strong>$1</strong>')
        .replace(/\*(.+?)\*/g, '<em>$1</em>')
        .replace(/_(.+?)_/g, '<em>$1</em>')
        .replace(/`(.+?)`/g, '<code>$1</code>')
        .replace(/\[(.+?)\]\((.+?)\)/g, '<a href="$2">$1</a>');

      const lines = escape(md).split('\n');
      const html = [];
      let inList = false;
      lines.forEach((rawLine) => {
        const line = rawLine.trim();
        if (!line) {
          if (inList) {
            html.push('</ul>');
            inList = false;
          }
          return;
        }
        if (/^#{1,6}\s/.test(line)) {
          if (inList) {
            html.push('</ul>');
            inList = false;
          }
          const level = (line.match(/^#+/) || ['#'])[0].length;
          html.push(`<h${level}>${inline(line.replace(/^#{1,6}\s/, ''))}</h${level}>`);
          return;
        }
        if (/^[-*]\s+/.test(line)) {
          if (!inList) {
            html.push('<ul>');
            inList = true;
          }
          html.push(`<li>${inline(line.replace(/^[-*]\s+/, ''))}</li>`);
          return;
        }
        if (inList) {
          html.push('</ul>');
          inList = false;
        }
        html.push(`<p>${inline(line)}</p>`);
      });
      if (inList) {
        html.push('</ul>');
      }
      return html.join('');
    }
  }
};
</script>

<style scoped lang="scss">
@import "@/styles/common.scss";

.loading-box,
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

.header-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.head-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.view-count {
  font-size: 22rpx;
  color: #8b95ab;
}

.title {
  margin-top: 14rpx;
  display: block;
  font-size: 34rpx;
  color: #1c2b4a;
  font-weight: 700;
  line-height: 1.4;
}

.meta {
  margin-top: 10rpx;
  display: block;
  font-size: 22rpx;
  color: #8b95ab;
}

.content-card {
  margin-top: 16rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

:deep(.content-card h1),
:deep(.content-card h2),
:deep(.content-card h3),
:deep(.content-card h4) {
  margin: 14rpx 0 12rpx;
  color: #1c2b4a;
}

:deep(.content-card p) {
  margin: 10rpx 0;
  color: #2e3b54;
  line-height: 1.8;
  font-size: 28rpx;
}

:deep(.content-card ul) {
  padding-left: 28rpx;
}

:deep(.content-card li) {
  margin: 8rpx 0;
  color: #2e3b54;
  font-size: 28rpx;
}

:deep(.content-card code) {
  background: #f1f3f6;
  border-radius: 8rpx;
  padding: 2rpx 10rpx;
}
</style>
