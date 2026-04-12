<template>
  <view class="page">
    <view v-if="record">
      <view class="summary-card">
        <view class="summary-head">
          <view class="summary-main">
            <text class="hospital">{{ record.hospitalName }}</text>
            <text class="meta">{{ record.departmentName }} · {{ record.doctorName }}</text>
            <text class="meta">就诊日期：{{ record.visitDateText }}</text>
          </view>
          <view class="record-chip" @tap="copyRecordNo">
            <text class="record-chip-label">病历号</text>
            <text class="record-chip-value">{{ record.recordNo || '未提供' }}</text>
          </view>
        </view>
        <view class="flag-row">
          <uni-tag v-if="record.checkResults" text="有检查结果" size="small" type="primary" />
          <uni-tag v-if="record.treatment" text="有治疗方案" size="small" type="warning" />
          <uni-tag v-if="record.prescription" text="有处方" size="small" type="success" />
          <uni-tag v-if="record.doctorAdvice" text="有医嘱" size="small" type="error" />
        </view>
      </view>

      <view class="card">
        <view class="text-item">
          <text class="text-label">主要症状</text>
          <text class="text-content">{{ record.symptoms || '暂无记录' }}</text>
        </view>
        <view class="text-item">
          <text class="text-label">诊断结果</text>
          <text class="text-content emphasis">{{ record.diagnosis || '暂无记录' }}</text>
        </view>
      </view>

      <view v-if="record.checkResults" class="card">
        <view class="section-head">
          <image class="section-icon" src="/static/icon_med/zhenduanjilu.png" mode="aspectFit" />
          <text class="section-title">检查结果</text>
        </view>
        <text class="text-content">{{ record.checkResults }}</text>
      </view>

      <view v-if="record.treatment" class="card">
        <view class="section-head">
          <image class="section-icon" src="/static/icon_med/fuwuzhong.png" mode="aspectFit" />
          <text class="section-title">治疗方案</text>
        </view>
        <text class="text-content">{{ record.treatment }}</text>
      </view>

      <view v-if="record.prescription" class="card">
        <view class="section-head">
          <image class="section-icon" src="/static/icon_med/yaoxiang.png" mode="aspectFit" />
          <text class="section-title">处方信息</text>
        </view>
        <text class="text-content">{{ record.prescription }}</text>
      </view>

      <view v-if="record.doctorAdvice" class="card">
        <view class="section-head">
          <image class="section-icon" src="/static/icon_med/suoming.png" mode="aspectFit" />
          <text class="section-title">医生建议</text>
        </view>
        <text class="text-content">{{ record.doctorAdvice }}</text>
      </view>

      <view v-if="attachmentList.length" class="card">
        <view class="section-head">
          <image class="section-icon" src="/static/icon_med/tupian.png" mode="aspectFit" />
          <text class="section-title">附件</text>
        </view>
        <view class="attachment-list">
          <view
            v-for="(item, index) in attachmentList"
            :key="index"
            class="attachment-item"
            @tap="openAttachment(item)"
          >
            <text class="attachment-name">{{ item }}</text>
            <text class="attachment-op">{{ isImageFile(item) ? '预览' : '复制' }}</text>
          </view>
        </view>
      </view>

      <view class="action-row">
        <button class="btn-secondary" style="flex: 1;" @tap="copySummary">复制病历摘要</button>
        <button class="btn-primary" style="flex: 1;" @tap="goBack">返回</button>
      </view>
    </view>
  </view>
</template>

<script>
import { recordApi } from '@/utils/api.js';
import { formatDate } from '@/utils/format.js';
import { isPatientRole } from '@/utils/permission.js';

export default {
  data() {
    return {
      record: null
    };
  },
  computed: {
    attachmentList() {
      const raw = this.record?.attachments;
      if (!raw) return [];
      if (Array.isArray(raw)) return raw.filter(Boolean);
      return String(raw)
        .split(/[,\n;]/)
        .map((item) => item.trim())
        .filter(Boolean);
    }
  },
  async onLoad(options) {
    const user = uni.getStorageSync('userInfo');
    if (!isPatientRole(user)) {
      uni.showToast({ title: '仅患者可查看病历', icon: 'none' });
      uni.switchTab({ url: '/pages/order/index' });
      return;
    }
    if (!options?.id) return;
    try {
      const raw = await recordApi.detail(options.id);
      this.record = this.normalizeRecord(raw);
    } catch (error) {
      uni.showToast({ title: error.message || '加载失败', icon: 'none' });
    }
  },
  methods: {
    normalizeRecord(record) {
      return {
        ...record,
        hospitalName: record?.hospitalName || '未填写医院',
        departmentName: record?.departmentName || '未填写科室',
        doctorName: record?.doctorName || '未填写医生',
        visitDateText: formatDate(record?.visitDate) || record?.visitDate || '-',
        recordNo: record?.recordNo || '',
        symptoms: record?.symptoms || '',
        diagnosis: record?.diagnosis || '',
        checkResults: record?.checkResults || '',
        treatment: record?.treatment || '',
        prescription: record?.prescription || '',
        doctorAdvice: record?.doctorAdvice || '',
        attachments: record?.attachments || ''
      };
    },
    isImageFile(path) {
      return /\.(jpg|jpeg|png|gif|webp|bmp)$/i.test(path || '');
    },
    openAttachment(path) {
      if (!path) return;
      if (this.isImageFile(path)) {
        uni.previewImage({ urls: [path] });
        return;
      }
      uni.setClipboardData({
        data: path,
        success: () => {
          uni.showToast({ title: '附件名已复制', icon: 'success' });
        }
      });
    },
    copyRecordNo() {
      if (!this.record?.recordNo) {
        uni.showToast({ title: '病历号为空', icon: 'none' });
        return;
      }
      uni.setClipboardData({
        data: this.record.recordNo,
        success: () => {
          uni.showToast({ title: '病历号已复制', icon: 'success' });
        }
      });
    },
    copySummary() {
      if (!this.record) return;
      const summary = [
        `医院：${this.record.hospitalName}`,
        `科室/医生：${this.record.departmentName} / ${this.record.doctorName}`,
        `就诊日期：${this.record.visitDateText}`,
        `病历号：${this.record.recordNo || '未提供'}`,
        `主要症状：${this.record.symptoms || '暂无'}`,
        `诊断结果：${this.record.diagnosis || '暂无'}`,
        this.record.treatment ? `治疗方案：${this.record.treatment}` : '',
        this.record.prescription ? `处方信息：${this.record.prescription}` : '',
        this.record.doctorAdvice ? `医生建议：${this.record.doctorAdvice}` : ''
      ].filter(Boolean).join('\n');
      uni.setClipboardData({
        data: summary,
        success: () => {
          uni.showToast({ title: '摘要已复制', icon: 'success' });
        }
      });
    },
    goBack() {
      uni.navigateBack();
    }
  }
};
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 24rpx;
  background: linear-gradient(160deg, #f3f6ff, #fbfdff 65%);
}

.summary-card {
  background: linear-gradient(145deg, #ffffff, #f6f9ff);
  border-radius: 20rpx;
  padding: 20rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.summary-head {
  display: flex;
  justify-content: space-between;
  gap: 14rpx;
}

.summary-main {
  flex: 1;
}

.hospital {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.meta {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #5d6b84;
}

.record-chip {
  min-width: 180rpx;
  border-radius: 14rpx;
  background: #edf3ff;
  padding: 10rpx;
}

.record-chip-label {
  display: block;
  font-size: 20rpx;
  color: #5a6e97;
}

.record-chip-value {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  font-weight: 600;
  color: #244eaf;
}

.flag-row {
  margin-top: 14rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.card {
  margin-top: 14rpx;
  background: #fff;
  border-radius: 18rpx;
  padding: 18rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.section-head {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 10rpx;
}

.section-icon {
  width: 30rpx;
  height: 30rpx;
}

.section-title {
  font-size: 25rpx;
  font-weight: 700;
  color: #1f2d4a;
}

.text-item + .text-item {
  margin-top: 14rpx;
}

.text-label {
  display: block;
  font-size: 22rpx;
  color: #75829b;
}

.text-content {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  line-height: 1.6;
  color: #1c2b4a;
}

.emphasis {
  color: #1d4ec0;
  font-weight: 600;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.attachment-item {
  border: 1rpx solid #e6ecfb;
  border-radius: 12rpx;
  padding: 12rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12rpx;
}

.attachment-name {
  font-size: 22rpx;
  color: #2b3b5a;
  word-break: break-all;
}

.attachment-op {
  font-size: 22rpx;
  color: #2f65f9;
  white-space: nowrap;
}

.action-row {
  margin-top: 24rpx;
  display: flex;
  gap: 14rpx;
}
</style>
