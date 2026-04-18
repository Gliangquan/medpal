<template>
  <view class="page">
    <view v-if="detail" class="post-card">
      <text class="post-title">{{ detail.post.title }}</text>
      <text class="post-meta">{{ detail.post.authorName || '用户' }} · {{ formatTime(detail.post.createTime) }}</text>
      <text class="post-content">{{ detail.post.content }}</text>
      <view v-if="detail.post.imageUrls" class="media-box">
        <image v-for="(item, index) in imageList(detail.post.imageUrls)" :key="index" class="media-image" :src="item" mode="aspectFill" @tap="previewImage(item, detail.post.imageUrls)" />
      </view>
      <view class="post-actions">
        <text class="action-text">评论 {{ detail.post.commentCount || 0 }}</text>
        <text class="action-text">点赞 {{ detail.post.likeCount || 0 }}</text>
        <button class="mini-btn" @tap="togglePostLike">{{ detail.post.liked ? '取消赞' : '点赞帖子' }}</button>
      </view>
    </view>

    <view class="comment-card">
      <view class="section-head">
        <text class="section-title">留言评论</text>
      </view>
      <view class="comment-form">
        <uni-easyinput v-model="commentForm.content" type="textarea" placeholder="写下你的建议或经验..." maxlength="300" />
        <view class="upload-row">
          <button class="btn-secondary upload-btn" @tap="pickCommentImage">上传图片</button>
        </view>
        <view v-if="commentForm.imageUrls.length" class="attachment-box">
          <text class="attachment-text">已选图片：{{ commentForm.imageUrls.length }} 张</text>
        </view>
        <button class="btn-primary submit-btn" :disabled="submitting" @tap="submitComment">
          {{ submitting ? '提交中...' : '提交评论' }}
        </button>
      </view>
    </view>

    <view class="comment-card">
      <view class="section-head">
        <text class="section-title">评论列表</text>
      </view>
      <view v-if="comments.length" class="comment-list">
        <view v-for="item in comments" :key="item.id" class="comment-item">
          <text class="comment-author">{{ item.authorName || '用户' }}</text>
          <text class="comment-time">{{ formatTime(item.createTime) }}</text>
          <text class="comment-content">{{ item.content }}</text>
          <view v-if="item.imageUrls" class="media-box small-box">
            <image v-for="(img, idx) in imageList(item.imageUrls)" :key="idx" class="small-image" :src="img" mode="aspectFill" @tap="previewImage(img, item.imageUrls)" />
          </view>
          <view class="comment-actions">
            <text class="action-text">点赞 {{ item.likeCount || 0 }}</text>
            <button class="mini-btn" @tap="toggleCommentLike(item)">{{ item.liked ? '取消赞' : '点赞' }}</button>
          </view>
        </view>
      </view>
      <view v-else class="empty-inline">暂无评论，快来抢沙发</view>
    </view>
  </view>
</template>

<script>
import { communityApi, fileApi } from '@/utils/api.js';
import { formatDateTime } from '@/utils/format.js';

export default {
  data() {
    return {
      postId: null,
      detail: null,
      comments: [],
      submitting: false,
      commentForm: {
        content: '',
        imageUrls: []
      }
    };
  },
  onLoad(options) {
    this.postId = Number(options?.id) || null;
    if (this.postId) {
      this.loadDetail();
    }
  },
  methods: {
    formatTime(value) {
      return formatDateTime(value) || '刚刚';
    },
    imageList(value) {
      if (!value) return [];
      return String(value).split(',').map((item) => item.trim()).filter(Boolean);
    },
    previewImage(current, value) {
      const urls = this.imageList(value);
      if (!urls.length) return;
      uni.previewImage({ current, urls });
    },
    async loadDetail() {
      try {
        const result = await communityApi.detail(this.postId);
        this.detail = result || null;
        this.comments = result?.comments || [];
      } catch (error) {
        uni.showToast({ title: error.message || '加载失败', icon: 'none' });
      }
    },
    async pickCommentImage() {
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
        this.commentForm.imageUrls = uploaded;
        uni.showToast({ title: '图片已上传', icon: 'success' });
      } catch (error) {
        uni.showToast({ title: error.message || '上传失败', icon: 'none' });
      }
    },
    async submitComment() {
      if (this.submitting) return;
      if (!this.commentForm.content.trim()) {
        uni.showToast({ title: '请输入评论内容', icon: 'none' });
        return;
      }
      this.submitting = true;
      try {
        await communityApi.createComment({
          postId: this.postId,
          content: this.commentForm.content.trim(),
          imageUrls: this.commentForm.imageUrls.join(',')
        });
        this.commentForm = { content: '', imageUrls: [] };
        uni.showToast({ title: '评论成功', icon: 'success' });
        await this.loadDetail();
      } catch (error) {
        uni.showToast({ title: error.message || '评论失败', icon: 'none' });
      } finally {
        this.submitting = false;
      }
    },
    async togglePostLike() {
      try {
        const result = await communityApi.togglePostLike(this.postId);
        this.detail.post.liked = !!result.liked;
        this.detail.post.likeCount = Number(result.likeCount || 0);
      } catch (error) {
        uni.showToast({ title: error.message || '操作失败', icon: 'none' });
      }
    },
    async toggleCommentLike(item) {
      try {
        const result = await communityApi.toggleCommentLike(item.id);
        item.liked = !!result.liked;
        item.likeCount = Number(result.likeCount || 0);
      } catch (error) {
        uni.showToast({ title: error.message || '操作失败', icon: 'none' });
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

.post-card,
.comment-card {
  background: #fff;
  border-radius: 22rpx;
  padding: 22rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.comment-card {
  margin-top: 18rpx;
}

.post-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.post-meta,
.comment-time,
.action-text,
.attachment-text,
.empty-inline {
  font-size: 21rpx;
  color: #7b8291;
}

.post-meta {
  display: block;
  margin-top: 8rpx;
}

.post-content,
.comment-content {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #42506a;
  line-height: 1.75;
}

.media-box {
  margin-top: 12rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.media-image,
.small-image {
  border-radius: 16rpx;
}

.media-image {
  width: 200rpx;
  height: 200rpx;
}

.small-image {
  width: 140rpx;
  height: 140rpx;
}

.post-actions,
.comment-actions,
.upload-row {
  margin-top: 12rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.comment-form {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.upload-btn,
.submit-btn {
  width: 100%;
}

.section-head {
  margin-bottom: 12rpx;
}

.section-title,
.comment-author {
  font-size: 26rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.comment-item {
  padding: 18rpx;
  border-radius: 18rpx;
  background: #f7f9ff;
  border: 1rpx solid #e5ebfb;
}

.mini-btn {
  margin-left: auto;
  padding: 0 22rpx;
  min-height: 56rpx;
  line-height: 56rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  background: rgba(47, 101, 249, 0.12);
  color: #2f65f9;
  border: 1rpx solid rgba(47, 101, 249, 0.25);
}

.empty-inline {
  text-align: center;
  padding: 24rpx 0;
}
</style>
