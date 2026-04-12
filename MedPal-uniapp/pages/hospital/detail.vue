<template>
  <view class="page">
    <view v-if="hospital" class="header-card">
      <text class="name">{{ hospital.hospitalName }}</text>
      <view class="meta">
        <text class="level">{{ hospital.hospitalLevel }}</text>
        <text class="rating">★ {{ hospital.rating }}</text>
      </view>
      <text class="address">{{ hospital.address }}</text>
      <text class="phone">{{ hospital.phone }}</text>
    </view>

    <uni-section title="医院介绍" type="line"></uni-section>
    <view class="card">
      <text class="text">{{ hospital.introduction }}</text>
    </view>

    <uni-section title="科室列表" type="line"></uni-section>
    <view class="card">
      <view class="dept-list">
        <view
          v-for="item in departments"
          :key="item.id"
          class="dept-item"
          :class="{ active: selectedDepartmentId === item.id }"
          @tap="selectDepartment(item.id)"
        >
          <text>{{ item.departmentName }}</text>
          <text class="dept-meta">{{ item.doctorCount }} 位医生</text>
        </view>
      </view>
    </view>

    <uni-section title="医生列表" type="line" v-if="doctors.length"></uni-section>
    <view class="card" v-if="doctors.length">
      <view v-for="item in doctors" :key="item.id" class="doctor-item">
        <view class="doctor-head">
          <view>
            <text class="doctor-name">{{ item.doctorName }}</text>
            <text class="doctor-title">{{ item.title }}</text>
          </view>
          <text class="doctor-rating">★ {{ item.rating }}</text>
        </view>
        <text class="doctor-specialty">擅长：{{ item.specialty }}</text>
        <view class="doctor-footer">
          <text class="fee">挂号费 ¥{{ item.registrationFee }}</text>
          <button v-if="canAppointment" class="btn-primary" @tap="quickAppointmentDoctor(item.id)">预约</button>
        </view>
      </view>
    </view>

    <button v-if="canAppointment" class="btn-primary" @tap="quickAppointment">快速预约</button>
  </view>
</template>

<script>
import { departmentApi, doctorApi, hospitalApi } from '@/utils/api.js';
import { isPatientRole } from '@/utils/permission.js';

export default {
  data() {
    return {
      currentUser: null,
      hospitalId: null,
      hospital: null,
      departments: [],
      doctors: [],
      selectedDepartmentId: null
    };
  },
  computed: {
    canAppointment() {
      return isPatientRole(this.currentUser);
    }
  },
  async onLoad(options) {
    this.currentUser = uni.getStorageSync('userInfo') || null;
    if (options && options.id) {
      this.hospitalId = options.id;
      await this.loadHospital(options.id);
      await this.loadDepartments(options.id);
    }
  },
  methods: {
    async loadHospital(id) {
      try {
        this.hospital = await hospitalApi.detail(id);
      } catch (error) {
        console.error('加载医院详情失败', error);
      }
    },
    async loadDepartments(hospitalId) {
      try {
        const page = await departmentApi.list({ hospitalId, current: 1, size: 20 });
        this.departments = page.records || [];
      } catch (error) {
        console.error('加载科室失败', error);
      }
    },
    async selectDepartment(id) {
      this.selectedDepartmentId = id;
      this.doctors = [];
      try {
        const page = await doctorApi.list({ departmentId: id, current: 1, size: 20 });
        this.doctors = page.records || [];
      } catch (error) {
        console.error('加载医生失败', error);
      }
    },
    quickAppointment() {
      if (!this.canAppointment) {
        uni.showToast({ title: '仅患者可预约', icon: 'none' });
        return;
      }
      uni.navigateTo({ url: `/pages/appointment/index?hospitalId=${this.hospitalId}` });
    },
    quickAppointmentDoctor(id) {
      if (!this.canAppointment) {
        uni.showToast({ title: '仅患者可预约', icon: 'none' });
        return;
      }
      uni.navigateTo({ url: `/pages/appointment/index?hospitalId=${this.hospitalId}&doctorId=${id}` });
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

.header-card {
  background: #ffffff;
  border-radius: 22rpx;
  padding: 20rpx;
  margin-bottom: 18rpx;
  box-shadow: 0 12rpx 30rpx rgba(33, 56, 100, 0.08);
}

.name {
  font-size: 30rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.meta {
  display: flex;
  gap: 12rpx;
  margin-top: 8rpx;
}

.level,
.rating {
  font-size: 22rpx;
  color: #2f65f9;
}

.address,
.phone {
  font-size: 22rpx;
  color: #7b8291;
  margin-top: 6rpx;
  display: block;
}

.card {
  background: #ffffff;
  border-radius: 18rpx;
  padding: 18rpx;
  margin-bottom: 18rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.text {
  font-size: 24rpx;
  color: #1c2b4a;
  line-height: 1.6;
}

.dept-list {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.dept-item {
  padding: 14rpx;
  border-radius: 14rpx;
  border: 1rpx solid #f0f2f8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 24rpx;
  color: #1c2b4a;
}

.dept-item.active {
  border-color: #2f65f9;
  background: rgba(47, 101, 249, 0.08);
}

.dept-meta {
  font-size: 20rpx;
  color: #7b8291;
}

.doctor-item {
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f0f2f8;
  margin-bottom: 16rpx;
}

.doctor-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.doctor-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.doctor-name {
  font-size: 26rpx;
  font-weight: 600;
  color: #1c2b4a;
}

.doctor-title {
  display: block;
  font-size: 22rpx;
  color: #7b8291;
  margin-top: 4rpx;
}

.doctor-rating {
  font-size: 22rpx;
  color: #ffb339;
}

.doctor-specialty {
  font-size: 22rpx;
  color: #5f6f94;
  margin-top: 8rpx;
}

.doctor-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12rpx;
}

.fee {
  font-size: 22rpx;
  color: #7b8291;
}

.btn-primary {
  background: #2f65f9;
  color: #ffffff;
  border-radius: 999rpx;
  padding: 8rpx 20rpx;
  font-size: 22rpx;
}
</style>
