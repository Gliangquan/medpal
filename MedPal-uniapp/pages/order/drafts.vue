<template>
  <view class="page">
    <view v-if="drafts.length" class="draft-list">
      <view v-for="item in drafts" :key="item.id" class="draft-card">
        <view class="draft-head">
          <view>
            <text class="draft-title">{{ item.hospitalName || '未选择医院' }}</text>
            <text class="draft-sub">{{ item.departmentName || '未选择科室' }}{{ item.doctorName ? ` · ${item.doctorName}` : '' }}</text>
          </view>
          <text class="draft-time">{{ item.updateText }}</text>
        </view>
        <view class="draft-body">
          <text class="draft-line">预约时间：{{ item.appointmentText }}</text>
          <text class="draft-line">陪诊时长：{{ item.durationText }}</text>
        </view>
        <view class="draft-actions">
          <button class="btn-secondary action-btn" @tap="restoreDraft(item)">恢复编辑</button>
          <button class="btn-primary action-btn" @tap="submitDraft(item)">直接提交</button>
        </view>
      </view>
    </view>
    <view v-else class="empty-box">
      <text class="empty-title">暂无草稿</text>
      <text class="empty-sub">你保存的预约草稿会显示在这里</text>
      <button class="btn-primary empty-btn" @tap="goAppointment">去新建草稿</button>
    </view>
  </view>
</template>

<script>
import { departmentApi, doctorApi, hospitalApi, orderApi } from '@/utils/api.js';
import { formatDateTime } from '@/utils/format.js';

export default {
  data() {
    return {
      drafts: []
    };
  },
  onShow() {
    this.loadDrafts();
  },
  methods: {
    normalizeDuration(duration) {
      if (!duration) return '-';
      const raw = String(duration).trim();
      if (/^\d+(\.\d+)?h$/i.test(raw)) return `${raw.replace(/h$/i, '')} 小时`;
      return raw;
    },
    async resolveHospitalName(item) {
      if (item.hospitalName || !item.hospitalId) return item.hospitalName || '';
      try {
        const hospital = await hospitalApi.detail(item.hospitalId);
        return hospital?.hospitalName || '';
      } catch (error) {
        return '';
      }
    },
    async resolveDepartmentName(item) {
      if (item.departmentName || !item.departmentId) return item.departmentName || '';
      try {
        const department = await departmentApi.list({ current: 1, size: 1, hospitalId: item.hospitalId });
        return (department?.records || []).find((record) => Number(record.id) === Number(item.departmentId))?.departmentName || '';
      } catch (error) {
        return '';
      }
    },
    async resolveDoctorName(item) {
      if (item.doctorName || !item.doctorId || !item.departmentId) return item.doctorName || '';
      try {
        const doctorPage = await doctorApi.list({ current: 1, size: 100, departmentId: item.departmentId });
        return (doctorPage?.records || []).find((record) => Number(record.id) === Number(item.doctorId))?.doctorName || '';
      } catch (error) {
        return '';
      }
    },
    async loadDrafts() {
      try {
        const page = await orderApi.listDrafts({ current: 1, size: 50 });
        const records = page?.records || [];
        this.drafts = await Promise.all(records.map(async (item) => {
          const [hospitalName, departmentName, doctorName] = await Promise.all([
            this.resolveHospitalName(item),
            this.resolveDepartmentName(item),
            this.resolveDoctorName(item)
          ]);
          return {
            ...item,
            hospitalName,
            departmentName,
            doctorName,
            appointmentText: item.appointmentDate ? formatDateTime(item.appointmentDate) : '未设置',
            durationText: this.normalizeDuration(item.duration),
            updateText: formatDateTime(item.updateTime || item.createTime) || '刚刚'
          };
        }));
      } catch (error) {
        uni.showToast({ title: error.message || '加载草稿失败', icon: 'none' });
      }
    },
    restoreDraft(item) {
      uni.setStorageSync('medpal_appointment_draft', {
        draftId: item.id,
        selectedHospital: item.hospitalId ? { id: item.hospitalId, hospitalName: item.hospitalName } : null,
        selectedDepartment: item.departmentId ? { id: item.departmentId, departmentName: item.departmentName } : null,
        selectedDoctor: item.doctorId ? { id: item.doctorId, doctorName: item.doctorName } : null,
        skipDoctorSelection: !item.doctorId,
        appointmentTimeRange: item.appointmentDate ? [item.appointmentDate, item.appointmentDate] : [],
        duration: Number(String(item.duration || '').replace(/h$/i, '')) || 2,
        contactPhone: this.extractField(item.specificNeeds, '联系手机号：'),
        meetingPoint: this.extractField(item.specificNeeds, '汇合地点：'),
        remarks: this.extractField(item.specificNeeds, '补充说明：')
      });
      uni.navigateTo({ url: '/pages/appointment/index?draftId=' + item.id });
    },
    extractField(text, prefix) {
      if (!text || !prefix) return '';
      const matched = String(text).split('；').find((item) => item.startsWith(prefix));
      return matched ? matched.replace(prefix, '').trim() : '';
    },
    async submitDraft(item) {
      try {
        await orderApi.submitDraft(item.id);
        uni.showToast({ title: '草稿已提交', icon: 'success' });
        this.loadDrafts();
      } catch (error) {
        uni.showToast({ title: error.message || '提交失败', icon: 'none' });
      }
    },
    goAppointment() {
      uni.navigateTo({ url: '/pages/appointment/index' });
    }
  }
};
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 24rpx;
  background: #f4f6fb;
}

.draft-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.draft-card {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 20rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.draft-head {
  display: flex;
  justify-content: space-between;
  gap: 12rpx;
}

.draft-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.draft-sub,
.draft-line,
.draft-time {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #7b8291;
}

.draft-actions {
  display: flex;
  gap: 12rpx;
  margin-top: 20rpx;
}

.action-btn {
  flex: 1;
  border-radius: 999rpx;
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
  margin-top: 12rpx;
  font-size: 22rpx;
  color: #8e98ab;
}

.empty-btn {
  margin-top: 24rpx;
  border-radius: 999rpx;
}
</style>
