<template>
  <view class="page">
    <view class="hero-card">
      <text class="hero-title">社区广场</text>
      <text class="hero-desc">交流就医经验、留言评论、推荐陪诊员，查看金牌陪诊员榜单。</text>
    </view>

    <view class="composer-card">
      <uni-easyinput v-model="postForm.title" placeholder="帖子标题（必填）" maxlength="40" />
      <uni-easyinput
        v-model="postForm.content"
        type="textarea"
        placeholder="写下你的就医问题、经验或推荐建议..."
        maxlength="500"
      />
      <view class="upload-row">
        <button class="btn-secondary upload-btn" @tap="pickPostImage">上传图片</button>
      </view>
      <view v-if="postForm.imageUrls.length" class="attachment-box">
        <text class="attachment-text">已选图片：{{ postForm.imageUrls.length }} 张</text>
      </view>
      <button class="btn-primary publish-btn" :disabled="publishing" @tap="submitPost">
        {{ publishing ? '发布中...' : '发布帖子' }}
      </button>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">金牌陪诊员</text>
        <text class="section-link" @tap="goCompanionList">查看全部</text>
      </view>
      <scroll-view v-if="goldCompanions.length" scroll-x class="gold-scroll">
        <view class="gold-row">
          <view v-for="item in goldCompanions" :key="item.id" class="gold-card" @tap="goCompanionDetail(item.id)">
            <image v-if="item.userAvatar" class="gold-avatar" :src="item.userAvatar" mode="aspectFill" />
            <view v-else class="gold-avatar fallback-avatar">{{ (item.userName || '陪').slice(0, 1) }}</view>
            <text class="gold-name">{{ item.userName || '陪诊员' }}</text>
            <text class="gold-meta">点赞 {{ item.likeCount || 0 }}</text>
            <text class="gold-meta">评分 {{ item.rating || 0 }}</text>
          </view>
        </view>
      </scroll-view>
      <view v-else class="empty-inline">暂无金牌陪诊员数据</view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">社区帖子</text>
        <uni-search-bar v-model="keyword" placeholder="搜索帖子" cancel-button="none" @confirm="searchPosts" @clear="searchPosts" />
      </view>
      <view v-if="posts.length" class="post-list">
        <view v-for="item in posts" :key="item.id" class="post-card" @tap="goDetail(item.id)">
          <view class="post-head">
            <text class="post-title">{{ item.title }}</text>
            <text class="post-time">{{ formatTime(item.createTime) }}</text>
          </view>
          <text class="post-author">{{ item.authorName || '用户' }}</text>
          <text class="post-content">{{ item.content }}</text>
          <view class="post-footer">
            <text class="footer-text">评论 {{ item.commentCount || 0 }}</text>
            <view class="like-pill" @tap.stop="togglePostLike(item)">
              <uni-icons :type="item.liked ? 'heart-filled' : 'heart'" size="18" :color="item.liked ? '#ff5c5c' : '#7b8291'" />
              <text class="like-pill-text" :class="{ liked: item.liked }">{{ item.likeCount || 0 }}</text>
            </view>
          </view>
        </view>
      </view>
      <view v-else class="empty-inline">暂无帖子，快来发布第一条内容</view>
    </view>
  </view>
</template>

<script>
import { communityApi, fileApi } from '@/utils/api.js';
import { formatDateTime } from '@/utils/format.js';

export default {
  data() {
    return {
      keyword: '',
      posts: [],
      goldCompanions: [],
      publishing: false,
      postForm: {
        title: '',
        content: '',
        imageUrls: []
      }
    };
  },
  onShow() {
    this.loadData();
  },
  methods: {
    formatTime(value) {
      return formatDateTime(value) || '刚刚';
    },
    async loadData() {
      await Promise.all([this.loadPosts(), this.loadGoldCompanions()]);
    },
    async loadPosts() {
      try {
        const page = await communityApi.listPosts({ current: 1, size: 20, keyword: this.keyword.trim() || undefined });
        this.posts = page?.records || [];
      } catch (error) {
        this.posts = [];
      }
    },
    async loadGoldCompanions() {
      try {
        this.goldCompanions = await communityApi.goldCompanions(8);
      } catch (error) {
        this.goldCompanions = [];
      }
    },
    searchPosts() {
      this.loadPosts();
    },
    async pickPostImage() {
      try {
        const chooseRes = await new Promise((resolve, reject) => {
          uni.chooseImage({ count: 3, success: resolve, fail: reject });
        });
        const files = chooseRes?.tempFilePaths || [];
        if (!files.length) return;
        const uploaded = [];
        for (const file of files) {
          const url = await fileApi.upload(file, 'order_attachment');
          uploaded.push(url);
        }
        this.postForm.imageUrls = uploaded;
        uni.showToast({ title: '图片已上传', icon: 'success' });
      } catch (error) {
        uni.showToast({ title: error.message || '上传失败', icon: 'none' });
      }
    },
    async submitPost() {
      if (this.publishing) return;
      if (!this.postForm.title.trim() || !this.postForm.content.trim()) {
        uni.showToast({ title: '请填写标题和内容', icon: 'none' });
        return;
      }
      this.publishing = true;
      try {
        await communityApi.createPost({
          title: this.postForm.title.trim(),
          content: this.postForm.content.trim(),
          imageUrls: this.postForm.imageUrls.join(',')
        });
        this.postForm = { title: '', content: '', imageUrls: [] };
        uni.showToast({ title: '发布成功', icon: 'success' });
        await this.loadPosts();
      } catch (error) {
        uni.showToast({ title: error.message || '发布失败', icon: 'none' });
      } finally {
        this.publishing = false;
      }
    },
    async togglePostLike(item) {
      try {
        const result = await communityApi.togglePostLike(item.id);
        item.liked = !!result.liked;
        item.likeCount = Number(result.likeCount || 0);
      } catch (error) {
        uni.showToast({ title: error.message || '操作失败', icon: 'none' });
      }
    },
    goDetail(id) {
      uni.navigateTo({ url: `/pages/community/detail?id=${id}` });
    },
    goCompanionDetail(id) {
      uni.navigateTo({ url: `/pages/companion/detail?id=${id}` });
    },
    goCompanionList() {
      uni.navigateTo({ url: '/pages/companion/list' });
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
.composer-card,
.section-card {
  background: #fff;
  border-radius: 22rpx;
  padding: 22rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.composer-card,
.section-card {
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

.upload-row {
  margin-top: 12rpx;
  display: flex;
  gap: 12rpx;
}

.upload-btn {
  flex: 1;
}

.attachment-box {
  margin-top: 10rpx;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.attachment-text {
  font-size: 21rpx;
  color: #7b8291;
}

.publish-btn {
  margin-top: 16rpx;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.section-link {
  font-size: 22rpx;
  color: #2f65f9;
}

.gold-scroll {
  white-space: nowrap;
}

.gold-row {
  display: inline-flex;
  gap: 14rpx;
}

.gold-card {
  width: 220rpx;
  padding: 18rpx;
  border-radius: 18rpx;
  background: #f7f9ff;
  border: 1rpx solid #e5ebfb;
  text-align: center;
}

.gold-avatar,
.fallback-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin: 0 auto 12rpx;
}

.fallback-avatar {
  background: #2f65f9;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 700;
}

.gold-name {
  display: block;
  font-size: 24rpx;
  font-weight: 600;
  color: #1c2b4a;
}

.gold-meta {
  display: block;
  margin-top: 6rpx;
  font-size: 21rpx;
  color: #7b8291;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.post-card {
  padding: 18rpx;
  border-radius: 18rpx;
  background: #f7f9ff;
  border: 1rpx solid #e5ebfb;
}

.post-head {
  display: flex;
  justify-content: space-between;
  gap: 12rpx;
}

.post-title {
  flex: 1;
  font-size: 27rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.post-time,
.post-author,
.footer-text,
.empty-inline {
  font-size: 21rpx;
  color: #7b8291;
}

.post-author {
  display: block;
  margin-top: 8rpx;
}

.post-content {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #42506a;
  line-height: 1.7;
}

.post-footer {
  margin-top: 12rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.like-pill {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  background: #fff;
  border: 1rpx solid #e5ebfb;
}

.like-pill-text {
  font-size: 22rpx;
  color: #7b8291;
}

.like-pill-text.liked {
  color: #ff5c5c;
  font-weight: 600;
}

.empty-inline {
  text-align: center;
  padding: 24rpx 0;
}
</style>
