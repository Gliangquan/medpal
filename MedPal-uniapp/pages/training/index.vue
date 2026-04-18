<template>
  <view class="page">
    <view class="hero-card">
      <text class="hero-title">培训学习</text>
      <text class="hero-desc">学习医疗知识、服务技巧、沟通技巧，提升专业陪诊能力。</text>
    </view>

    <view class="summary-grid">
      <view class="summary-card primary">
        <text class="summary-label">课程总数</text>
        <text class="summary-value">{{ courses.length }}</text>
      </view>
      <view class="summary-card">
        <text class="summary-label">已学完</text>
        <text class="summary-value">{{ completedCount }}</text>
      </view>
      <view class="summary-card">
        <text class="summary-label">学习中</text>
        <text class="summary-value">{{ learningCount }}</text>
      </view>
      <view class="summary-card">
        <text class="summary-label">累计学习</text>
        <text class="summary-value">{{ totalLearningMinutes }} 分钟</text>
      </view>
    </view>

    <view class="record-card">
      <view class="section-head">
        <text class="section-title">我的学习记录</text>
      </view>
      <view v-if="records.length" class="record-list">
        <view v-for="item in records" :key="item.id" class="record-item">
          <view>
            <text class="record-title">{{ item.courseTitle }}</text>
            <text class="record-meta">{{ item.category || '培训课程' }} · {{ item.learningTime || 0 }} 分钟</text>
          </view>
          <view class="record-side">
            <text class="progress-text">{{ item.progress || 0 }}%</text>
            <text class="status-text" :class="item.status">{{ item.status === 'completed' ? '已学完' : '学习中' }}</text>
          </view>
        </view>
      </view>
      <view v-else class="empty-inline">暂无学习记录</view>
    </view>

    <view class="course-card">
      <view class="section-head section-head-column">
        <text class="section-title">培训课程</text>
        <uni-segmented-control
          :values="categoryTabs"
          :current="categoryIndex"
          style-type="button"
          active-color="#2f65f9"
          @clickItem="changeCategory"
        />
      </view>
      <view v-if="courses.length" class="course-list">
        <view v-for="item in courses" :key="item.id" class="course-item" @tap="goDetail(item.id)">
          <text class="course-title">{{ item.title }}</text>
          <text class="course-desc">{{ item.description || '暂无课程简介' }}</text>
          <view class="course-foot">
            <text class="course-meta">{{ item.category || '综合课程' }}</text>
            <text class="course-meta">{{ item.duration || 0 }} 分钟</text>
            <text class="course-meta">难度 {{ item.difficulty || 1 }}</text>
          </view>
        </view>
      </view>
      <view v-else class="empty-inline">暂无课程</view>
    </view>
  </view>
</template>

<script>
import { trainingApi } from '@/utils/api.js';

export default {
  data() {
    return {
      categoryTabs: ['全部', '沟通技巧', '服务技巧', '医疗知识', '应急处理'],
      categoryIndex: 0,
      courses: [],
      records: []
    };
  },
  computed: {
    completedCount() {
      return this.records.filter((item) => item.status === 'completed').length;
    },
    learningCount() {
      return this.records.filter((item) => item.status === 'learning').length;
    },
    totalLearningMinutes() {
      return this.records.reduce((sum, item) => sum + Number(item.learningTime || 0), 0);
    }
  },
  onShow() {
    this.loadData();
  },
  methods: {
    async loadData() {
      try {
        const category = this.categoryIndex === 0 ? undefined : this.categoryTabs[this.categoryIndex];
        const [coursePage, recordList] = await Promise.all([
          trainingApi.courses({ current: 1, size: 20, category }),
          trainingApi.records({ current: 1, size: 20 })
        ]);
        this.courses = coursePage?.records || [];
        this.records = recordList || [];
      } catch (error) {
        uni.showToast({ title: error.message || '加载失败', icon: 'none' });
      }
    },
    changeCategory(e) {
      this.categoryIndex = e.currentIndex;
      this.loadData();
    },
    goDetail(id) {
      uni.navigateTo({ url: `/pages/training/detail?id=${id}` });
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
.record-card,
.course-card {
  background: #fff;
  border-radius: 22rpx;
  padding: 22rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
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

.record-card,
.course-card {
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

.section-head {
  margin-bottom: 12rpx;
}

.section-head-column {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 12rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.record-list,
.course-list {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.record-item,
.course-item {
  padding: 18rpx;
  border-radius: 18rpx;
  background: #f7f9ff;
  border: 1rpx solid #e5ebfb;
}

.record-item {
  display: flex;
  justify-content: space-between;
  gap: 12rpx;
  align-items: center;
}

.record-title,
.course-title {
  display: block;
  font-size: 26rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.record-meta,
.course-desc,
.course-meta,
.empty-inline {
  font-size: 21rpx;
  color: #7b8291;
}

.record-meta,
.course-desc {
  display: block;
  margin-top: 8rpx;
}

.record-side {
  text-align: right;
}

.progress-text {
  display: block;
  font-size: 26rpx;
  font-weight: 700;
  color: #2f65f9;
}

.status-text {
  display: block;
  margin-top: 6rpx;
  font-size: 21rpx;
}

.status-text.completed {
  color: #18a058;
}

.status-text.learning {
  color: #ff9500;
}

.course-foot {
  display: flex;
  gap: 12rpx;
  margin-top: 10rpx;
  flex-wrap: wrap;
}

.empty-inline {
  text-align: center;
  padding: 24rpx 0;
}
</style>
