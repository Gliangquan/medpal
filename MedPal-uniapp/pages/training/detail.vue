<template>
  <view class="page">
    <view v-if="course" class="course-card">
      <text class="course-title">{{ course.title }}</text>
      <text class="course-meta">{{ course.category || '综合课程' }} · {{ course.duration || 0 }} 分钟 · 难度 {{ course.difficulty || 1 }}</text>
      <text class="course-meta">讲师：{{ course.instructor || '平台讲师' }}</text>
      <text class="course-desc">{{ course.description || '暂无课程简介' }}</text>
      <view class="content-box">
        <text class="content-text">{{ course.content || '暂无课程内容' }}</text>
      </view>
    </view>

    <view class="progress-card">
      <view class="section-head">
        <text class="section-title">学习进度</text>
      </view>
      <view class="progress-box">
        <text class="progress-value">{{ progress.progress || 0 }}%</text>
        <text class="progress-status">{{ progress.status === 'completed' ? '已学完' : progress.status === 'learning' ? '学习中' : '未开始' }}</text>
      </view>
      <view class="btn-row">
        <button class="btn-secondary" @tap="markLearning(50)">标记学习中</button>
        <button class="btn-primary" @tap="markLearning(100)">标记已学完</button>
      </view>
    </view>
  </view>
</template>

<script>
import { trainingApi } from '@/utils/api.js';

export default {
  data() {
    return {
      courseId: null,
      course: null,
      progress: {}
    };
  },
  onLoad(options) {
    this.courseId = Number(options?.id) || null;
    if (this.courseId) {
      this.loadDetail();
    }
  },
  methods: {
    async loadDetail() {
      try {
        const [course, progress] = await Promise.all([
          trainingApi.detail(this.courseId),
          trainingApi.progress({ courseId: this.courseId })
        ]);
        this.course = course || null;
        this.progress = progress || {};
      } catch (error) {
        uni.showToast({ title: error.message || '加载失败', icon: 'none' });
      }
    },
    async markLearning(progress) {
      try {
        await trainingApi.record({
          courseId: this.courseId,
          progress,
          learningTime: progress >= 100 ? (this.course?.duration || 0) : Math.max(10, Math.round((this.course?.duration || 20) / 2)),
          status: progress >= 100 ? 'completed' : 'learning'
        });
        uni.showToast({ title: progress >= 100 ? '已标记学完' : '已记录学习进度', icon: 'success' });
        await this.loadDetail();
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

.course-card,
.progress-card {
  background: #fff;
  border-radius: 22rpx;
  padding: 22rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.progress-card {
  margin-top: 18rpx;
}

.course-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.course-meta,
.course-desc,
.content-text,
.progress-status {
  font-size: 22rpx;
  color: #6f7c95;
}

.course-meta,
.course-desc {
  display: block;
  margin-top: 10rpx;
}

.content-box {
  margin-top: 16rpx;
  padding: 18rpx;
  border-radius: 18rpx;
  background: #f7f9ff;
  border: 1rpx solid #e5ebfb;
}

.content-text {
  line-height: 1.8;
}

.section-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.progress-box {
  margin-top: 16rpx;
  text-align: center;
}

.progress-value {
  display: block;
  font-size: 42rpx;
  font-weight: 700;
  color: #2f65f9;
}

.progress-status {
  display: block;
  margin-top: 8rpx;
}

.btn-row {
  margin-top: 20rpx;
  display: flex;
  gap: 12rpx;
}

.btn-primary,
.btn-secondary {
  flex: 1;
}
</style>
