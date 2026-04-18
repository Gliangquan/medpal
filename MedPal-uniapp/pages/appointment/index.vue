<template>
  <view class="page">
    <uni-steps :options="steps" :active="stepIndex" active-color="#2f65f9"></uni-steps>

    <view class="section search-card">
      <view v-if="selectedHospital" class="step-actions">
        <view class="back-step-btn" @tap="clearHospitalSelection">
          <uni-icons type="left" size="14" color="#2f65f9"></uni-icons>
          <text>重新选择医院</text>
        </view>
        <text class="step-hint">当前医院：{{ selectedHospital.hospitalName }}</text>
      </view>
      <uni-search-bar
        v-model="searchKeyword"
        :placeholder="selectedHospital ? '在当前医院内搜索科室 / 医生' : '搜索医院'"
        cancel-button="none"
        @confirm="applySearch"
        @clear="clearSearch"
      />
      <view class="filters">
        <view class="filter-chip">{{ selectedHospital ? selectedHospital.hospitalName : '选择医院' }}</view>
        <view class="filter-chip">{{ selectedDepartment ? selectedDepartment.departmentName : '科室可选' }}</view>
        <view class="filter-chip">{{ selectedDoctor ? selectedDoctor.doctorName : '医生可选' }}</view>
        <view class="filter-chip">{{ selectedCompanion ? displayCompanionName(selectedCompanion) : '大厅抢单' }}</view>
        <view v-if="selectedRangeText" class="filter-chip">{{ selectedRangeText }}</view>
      </view>
    </view>

    <view v-if="!selectedHospital" class="section">
      <view class="section-title">选择医院</view>
      <view class="list">
        <view v-for="item in filteredHospitals" :key="item.id" class="list-card" @tap="selectHospital(item)">
          <view class="list-head">
            <text class="list-title">{{ item.hospitalName }}</text>
            <uni-tag :text="item.hospitalLevel" type="primary" size="small"></uni-tag>
          </view>
          <text class="list-sub">{{ item.address }}</text>
          <view class="list-meta">
            <text class="meta-item">{{ getHospitalDepartmentCount(item) }} 个科室</text>
            <text class="meta-dot">·</text>
            <text class="meta-item">{{ getHospitalDoctorCount(item) }} 位医生</text>
          </view>
        </view>
      </view>
    </view>

    <view v-else class="section">
      <view class="section-title">医院内搜索科室 / 医生</view>
      <view class="hospital-picked-card">
        <view>
          <text class="picked-title">{{ selectedHospital.hospitalName }}</text>
          <text class="picked-sub">先选科室，再按需选择医生；也可直接搜医生自动匹配科室</text>
        </view>
      </view>

      <view class="choice-summary">
        <view class="summary-item">
          <text class="summary-label">已选科室</text>
          <text class="summary-value">{{ selectedDepartment ? selectedDepartment.departmentName : '暂未选择' }}</text>
        </view>
        <view class="summary-item">
          <text class="summary-label">已选医生</text>
          <text class="summary-value">{{ selectedDoctor ? selectedDoctor.doctorName : '暂不指定' }}</text>
        </view>
      </view>

      <view class="split-section">
        <view class="section-subtitle-row">
          <text class="section-subtitle">科室结果</text>
          <text class="section-subhint">{{ filteredDepartments.length }} 个结果</text>
        </view>
        <view class="list compact-list">
          <view
            v-for="item in filteredDepartments"
            :key="`dept-${item.id}`"
            class="list-card compact-card"
            :class="{ active: selectedDepartment && Number(selectedDepartment.id) === Number(item.id) }"
            @tap="selectDepartment(item)"
          >
            <text class="list-title">{{ item.departmentName }}</text>
            <text class="list-sub">{{ getDepartmentDoctorCount(item) }} 位医生</text>
          </view>
        </view>
      </view>

      <view class="split-section">
        <view class="section-subtitle-row">
          <text class="section-subtitle">医生结果（可选）</text>
          <text class="section-subhint">{{ filteredDoctors.length }} 个结果</text>
        </view>
        <view v-if="filteredDoctors.length" class="list compact-list">
          <view
            v-for="item in filteredDoctors"
            :key="`doctor-${item.id}`"
            class="list-card"
            :class="{ active: selectedDoctor && Number(selectedDoctor.id) === Number(item.id) }"
            @tap="selectDoctor(item)"
          >
            <view class="list-head">
              <text class="list-title">{{ item.doctorName }}</text>
              <text class="list-badge">{{ item.doctorTitle || item.title || '医生' }}</text>
            </view>
            <text class="list-sub">所属科室：{{ getDoctorDepartmentName(item) }}</text>
            <text class="list-sub">擅长：{{ item.specialties || item.specialty || '暂无' }}</text>
          </view>
        </view>
        <view v-else class="empty-inline">当前筛选下暂无医生结果</view>
        <view class="doctor-skip-wrap">
          <button class="btn-secondary doctor-skip-btn" @tap="clearDoctorSelection">暂不指定医生</button>
        </view>
      </view>
    </view>

    <view v-if="selectedHospital" class="section">
      <view class="section-title">填写预约信息</view>
      <view class="form-card">
        <view class="form-item">
          <text class="form-label">指定陪诊员（可选）</text>
          <view class="companion-select-card" @tap="goChooseCompanion">
            <view class="companion-select-main">
              <text class="companion-select-title">
                {{ selectedCompanion ? displayCompanionName(selectedCompanion) : '暂不指定，发布到大厅抢单' }}
              </text>
              <text class="companion-select-sub">
                {{ selectedCompanion
                  ? '评分 ' + (selectedCompanion.rating || '暂无') + ' · 服务 ' + (selectedCompanion.serviceCount || 0) + ' 次'
                  : '可指定心仪陪诊员优先接单' }}
              </text>
            </view>
            <uni-icons type="right" size="16" color="#9aa3b6"></uni-icons>
          </view>
          <view class="companion-action-row">
            <button class="btn-secondary companion-action-btn" @tap="goChooseCompanion">
              {{ selectedCompanion ? '更换陪诊员' : '去挑选陪诊员' }}
            </button>
            <button class="btn-ghost companion-action-btn" :class="{ 'companion-action-btn-active': !selectedCompanion }" @tap="clearSelectedCompanion">
              {{ selectedCompanion ? '不指定' : '当前：大厅抢单' }}
            </button>
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">期望开始时间</text>
          <uni-datetime-picker
            type="datetime"
            v-model="appointmentStartTime"
            :start="minAppointmentTime"
            :end="maxAppointmentTime"
          />
          <text class="form-hint">先选择开始时间，系统会根据陪诊时长自动计算结束时间</text>
        </view>

        <view class="form-item">
          <text class="form-label">预计结束时间</text>
          <view class="readonly-box">
            <text>{{ appointmentEndTimeText || '请先选择开始时间' }}</text>
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">陪诊时长</text>
          <view class="duration-list">
            <view
              v-for="item in durationOptions"
              :key="item.value"
              class="duration-card"
              :class="{ active: duration === item.value }"
              @tap="selectDuration(item.value)"
            >
              <text class="duration-label">{{ item.label }}</text>
              <text class="duration-price">¥{{ item.price }}</text>
            </view>
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">就诊人</text>
          <view class="visit-role-list">
            <view
              v-for="item in visitRoleOptions"
              :key="item.value"
              class="visit-role-item"
              :class="{ active: visitRole === item.value }"
              @tap="visitRole = item.value"
            >
              {{ item.label }}
            </view>
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">联系手机号</text>
          <uni-easyinput v-model="contactPhone" type="number" maxlength="11" placeholder="用于陪诊员联系您" />
        </view>

        <view class="form-item">
          <text class="form-label">建议汇合地点</text>
          <uni-easyinput v-model="meetingPoint" placeholder="如：门诊楼1号门 / 地铁A口" />
        </view>

        <view class="form-item">
          <text class="form-label">额外需求</text>
          <uni-data-checkbox v-model="requirements" :localdata="requirementOptions" mode="tag" @change="onRequirementChange" />
          <view class="custom-requirement-row">
            <uni-easyinput
              v-model="customRequirementInput"
              placeholder="可自定义需求，如：全程搀扶、优先上午就诊"
              :maxlength="30"
              @confirm="addCustomRequirement"
            />
            <button class="btn-add-requirement" @tap="addCustomRequirement">添加</button>
          </view>
          <view v-if="customRequirements.length" class="custom-requirement-list">
            <view v-for="(item, index) in customRequirements" :key="`${item}-${index}`" class="custom-requirement-chip">
              <text class="custom-requirement-text">{{ item }}</text>
              <uni-icons type="closeempty" size="14" color="#7b8291" @tap="removeCustomRequirement(index)"></uni-icons>
            </view>
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">病情与补充说明</text>
          <uni-easyinput type="textarea" v-model="remarks" placeholder="例如：行动不便、需陪同取药、希望女性陪诊员等" />
        </view>
      </view>

      <view class="cost-card">
        <view class="cost-row">
          <text>陪诊服务费</text>
          <text>¥{{ baseCost }}</text>
        </view>
        <view class="cost-row" v-if="extraCost > 0">
          <text>额外服务费</text>
          <text>¥{{ extraCost }}</text>
        </view>
        <view class="cost-row">
          <text>平台服务费</text>
          <text>¥{{ platformFee }}</text>
        </view>
        <view class="cost-total">
          <text>总费用</text>
          <text>¥{{ totalCost }}</text>
        </view>
      </view>

      <view class="action-row">
        <button class="btn-secondary" :disabled="draftSubmitting" @tap="saveDraft">{{ draftSubmitting ? '保存中...' : '暂存草稿' }}</button>
        <button class="btn-primary" :disabled="submitting" @tap="submitAppointment">{{ submitting ? '提交中...' : '确认预约' }}</button>
      </view>
    </view>
  </view>
</template>

<script>
import {
  hospitalApi,
  departmentApi,
  doctorApi,
  companionApi,
  orderApi,
  userApi
} from '@/utils/api.js';
import { formatDateTime } from '@/utils/format.js';

export default {
  data() {
    return {
      steps: [{ title: '医院' }, { title: '信息' }],
      hospitals: [],
      departments: [],
      doctors: [],
      selectedHospital: null,
      selectedDepartment: null,
      selectedDoctor: null,
      selectedCompanion: null,
      forceCompanionIdFromRoute: null,
      searchKeyword: '',
      appointmentStartTime: '',
      duration: 2,
      durationOptions: [
        { label: '2小时', value: 2, price: 200 },
        { label: '半天', value: 4, price: 400 },
        { label: '全天', value: 8, price: 800 }
      ],
      visitRole: 'self',
      visitRoleOptions: [
        { label: '本人就诊', value: 'self' },
        { label: '家人就诊', value: 'family' },
        { label: '长辈就诊', value: 'elder' }
      ],
      contactPhone: '',
      meetingPoint: '',
      requirements: [],
      customRequirementInput: '',
      customRequirements: [],
      requirementOptions: [
        { text: '轮椅陪同', value: 'wheelchair' },
        { text: '代取报告', value: 'report' },
        { text: '代缴费', value: 'payment' }
      ],
      remarks: '',
      baseCost: 200,
      extraCost: 0,
      platformFee: 20,
      totalCost: 220,
      submitting: false,
      draftSubmitting: false,
      userInfo: null,
      draftId: null
    };
  },
  computed: {
    stepIndex() {
      return this.selectedHospital ? 1 : 0;
    },
    filteredHospitals() {
      if (!this.searchKeyword) return this.hospitals;
      const keyword = this.searchKeyword.trim();
      return this.hospitals.filter((item) => item.hospitalName.includes(keyword));
    },
    filteredDepartments() {
      if (!this.selectedHospital) return [];
      if (!this.searchKeyword) return this.departments;
      const keyword = this.searchKeyword.trim();
      return this.departments.filter((item) => item.departmentName.includes(keyword));
    },
    filteredDoctors() {
      let list = this.doctors;
      if (this.selectedDepartment?.id) {
        list = list.filter((item) => Number(item.departmentId) === Number(this.selectedDepartment.id));
      }
      if (!this.searchKeyword) return list;
      const keyword = this.searchKeyword.trim();
      return list.filter((item) =>
        item.doctorName?.includes(keyword)
        || item.doctorTitle?.includes(keyword)
        || item.title?.includes(keyword)
        || item.specialties?.includes(keyword)
        || item.specialty?.includes(keyword)
        || this.getDoctorDepartmentName(item).includes(keyword)
      );
    },
    appointmentEndTime() {
      if (!this.appointmentStartTime) return null;
      const start = new Date(this.appointmentStartTime);
      if (Number.isNaN(start.getTime())) return null;
      return new Date(start.getTime() + this.duration * 60 * 60 * 1000);
    },
    appointmentEndTimeText() {
      return this.appointmentEndTime ? formatDateTime(this.appointmentEndTime) : '';
    },
    selectedRangeText() {
      const start = this.appointmentStartTime ? new Date(this.appointmentStartTime) : null;
      const end = this.appointmentEndTime;
      if (!start || !end || Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) return '';
      return `${formatDateTime(start)} - ${formatDateTime(end)}`;
    },
    minAppointmentTime() {
      const now = new Date();
      now.setMinutes(0, 0, 0);
      now.setHours(now.getHours() + 1);
      return this.formatPickerDateTime(now);
    },
    maxAppointmentTime() {
      const max = new Date();
      max.setMonth(max.getMonth() + 3);
      max.setHours(20, 0, 0, 0);
      return this.formatPickerDateTime(max);
    }
  },
  async onLoad(options) {
    const loggedIn = await this.ensureLogin();
    if (!loggedIn) return;
    await this.loadHospitals();
    if (options?.duration) {
      this.duration = Number(options.duration) || this.duration;
    }
    if (options?.hospitalId) {
      const hospital = this.hospitals.find((item) => item.id === Number(options.hospitalId));
      if (hospital) {
        await this.selectHospital(hospital);
        if (options?.departmentId) {
          const targetDepartment = this.departments.find((item) => Number(item.id) === Number(options.departmentId));
          if (targetDepartment) {
            this.selectDepartment(targetDepartment);
          }
        }
        if (options?.doctorId) {
          const targetDoctor = this.doctors.find((item) => Number(item.id) === Number(options.doctorId));
          if (targetDoctor) {
            this.selectDoctor(targetDoctor);
          }
        }
      }
    }
    if (options?.companionId) {
      this.forceCompanionIdFromRoute = Number(options.companionId) || null;
      await this.loadSelectedCompanion(options.companionId);
    }
    if (options?.draftId) {
      this.draftId = Number(options.draftId) || null;
    }
    this.tryRestoreDraft();
    this.calculateCost();
  },
  methods: {
    isCompanionRole(user) {
      return user?.userRole === 'companion';
    },
    parseCount(value) {
      const count = Number(value);
      return Number.isFinite(count) && count >= 0 ? count : 0;
    },
    hasCountValue(value) {
      return value !== undefined && value !== null && value !== '';
    },
    hasHospitalDepartmentCount(item) {
      return !!item && (this.hasCountValue(item.departmentCount) || this.hasCountValue(item.departmentNum) || this.hasCountValue(item.deptCount));
    },
    hasHospitalDoctorCount(item) {
      return !!item && (this.hasCountValue(item.doctorCount) || this.hasCountValue(item.doctorNum) || this.hasCountValue(item.doctorTotal));
    },
    getHospitalDepartmentCount(item) {
      if (!item) return 0;
      if (this.hasCountValue(item.departmentCount)) return this.parseCount(item.departmentCount);
      if (this.hasCountValue(item.departmentNum)) return this.parseCount(item.departmentNum);
      if (this.hasCountValue(item.deptCount)) return this.parseCount(item.deptCount);
      return 0;
    },
    getHospitalDoctorCount(item) {
      if (!item) return 0;
      if (this.hasCountValue(item.doctorCount)) return this.parseCount(item.doctorCount);
      if (this.hasCountValue(item.doctorNum)) return this.parseCount(item.doctorNum);
      if (this.hasCountValue(item.doctorTotal)) return this.parseCount(item.doctorTotal);
      return 0;
    },
    getDepartmentDoctorCount(item) {
      if (!item) return 0;
      if (item.doctorCount !== undefined && item.doctorCount !== null) return this.parseCount(item.doctorCount);
      if (item.doctorNum !== undefined && item.doctorNum !== null) return this.parseCount(item.doctorNum);
      return 0;
    },
    getDoctorDepartmentName(item) {
      if (!item) return '未指定科室';
      const matched = this.departments.find((department) => Number(department.id) === Number(item.departmentId));
      return matched?.departmentName || item.departmentName || '未指定科室';
    },
    async ensureLogin() {
      const stored = uni.getStorageSync('userInfo');
      if (!stored?.id) {
        uni.showToast({ title: '请先登录', icon: 'none' });
        uni.navigateTo({ url: '/pages/login/index' });
        return false;
      }
      if (this.isCompanionRole(stored)) {
        uni.showToast({ title: '陪诊员端不支持发单预约', icon: 'none' });
        uni.switchTab({ url: '/pages/order/index' });
        return false;
      }
      this.userInfo = stored;
      this.contactPhone = stored.userPhone || this.contactPhone;
      try {
        const refreshed = await userApi.fetchCurrentUser();
        if (refreshed) {
          if (this.isCompanionRole(refreshed)) {
            uni.showToast({ title: '陪诊员端不支持发单预约', icon: 'none' });
            uni.switchTab({ url: '/pages/order/index' });
            return false;
          }
          this.userInfo = refreshed;
          uni.setStorageSync('userInfo', refreshed);
          this.contactPhone = refreshed.userPhone || this.contactPhone;
        }
      } catch (error) {
        console.warn('刷新用户信息失败', error);
      }
      return true;
    },
    async loadHospitals() {
      try {
        const [hospitalPage, departmentPage, doctorPage] = await Promise.all([
          hospitalApi.list({ current: 1, size: 100 }),
          departmentApi.list({ current: 1, size: 1000 }),
          doctorApi.list({ current: 1, size: 1000 })
        ]);
        const hospitals = hospitalPage.records || [];
        const departments = departmentPage.records || [];
        const doctors = doctorPage.records || [];
        const departmentCountMap = new Map();
        const doctorCountMap = new Map();

        departments.forEach((item) => {
          const key = Number(item.hospitalId);
          if (!Number.isFinite(key)) return;
          departmentCountMap.set(key, (departmentCountMap.get(key) || 0) + 1);
        });

        doctors.forEach((item) => {
          const key = Number(item.hospitalId);
          if (!Number.isFinite(key)) return;
          doctorCountMap.set(key, (doctorCountMap.get(key) || 0) + 1);
        });

        this.hospitals = hospitals.map((hospital) => ({
          ...hospital,
          departmentCount: departmentCountMap.get(Number(hospital.id)) || this.getHospitalDepartmentCount(hospital),
          doctorCount: doctorCountMap.get(Number(hospital.id)) || this.getHospitalDoctorCount(hospital)
        }));
      } catch (error) {
        console.error('加载医院列表失败', error);
      }
    },
    async fillDepartmentDoctorCount(departments) {
      if (!Array.isArray(departments) || !departments.length) return;
      await Promise.all(
        departments.map(async (department) => {
          if (!department?.id || this.getDepartmentDoctorCount(department) > 0) return;
          try {
            const page = await doctorApi.list({ current: 1, size: 1, departmentId: department.id });
            department.doctorCount = this.parseCount(page?.total ?? page?.records?.length ?? 0);
          } catch (error) {
            department.doctorCount = this.getDepartmentDoctorCount(department);
          }
        })
      );
      this.departments = [...departments];
    },
    async loadDepartments(hospitalId) {
      try {
        const page = await departmentApi.list({ current: 1, size: 100, hospitalId });
        const records = page.records || [];
        this.departments = records.map((item) => ({
          ...item,
          doctorCount: this.hasCountValue(item.doctorCount) || this.hasCountValue(item.doctorNum)
            ? this.parseCount(item.doctorCount ?? item.doctorNum)
            : null
        }));
        await this.fillDepartmentDoctorCount(this.departments);
      } catch (error) {
        console.error('加载科室失败', error);
        this.departments = [];
      }
    },
    async loadDoctors(params = {}) {
      try {
        const page = await doctorApi.list({ current: 1, size: 100, ...params });
        this.doctors = (page.records || []).map((item) => ({
          ...item,
          title: item.title || item.doctorTitle || '',
          specialty: item.specialty || item.specialties || ''
        }));
      } catch (error) {
        console.error('加载医生失败', error);
        this.doctors = [];
      }
    },
    applySearch() {
      if (!this.searchKeyword) return;
      const currentCount = this.selectedHospital
        ? this.filteredDepartments.length + this.filteredDoctors.length
        : this.filteredHospitals.length;
      if (currentCount === 0) {
        uni.showToast({ title: '未找到匹配项，请换个关键词', icon: 'none' });
      }
    },
    clearSearch() {
      this.searchKeyword = '';
    },
    async selectHospital(hospital) {
      this.selectedHospital = hospital;
      this.selectedDepartment = null;
      this.selectedDoctor = null;
      this.searchKeyword = '';
      await Promise.all([
        this.loadDepartments(hospital.id),
        this.loadDoctors({ hospitalId: hospital.id })
      ]);
    },
    clearHospitalSelection() {
      this.selectedHospital = null;
      this.selectedDepartment = null;
      this.selectedDoctor = null;
      this.departments = [];
      this.doctors = [];
      this.searchKeyword = '';
    },
    selectDepartment(department) {
      this.selectedDepartment = department;
      if (this.selectedDoctor && Number(this.selectedDoctor.departmentId) !== Number(department.id)) {
        this.selectedDoctor = null;
      }
    },
    selectDoctor(doctor) {
      this.selectedDoctor = doctor;
      if (!this.selectedDepartment || Number(this.selectedDepartment.id) !== Number(doctor.departmentId)) {
        const matchedDepartment = this.departments.find((item) => Number(item.id) === Number(doctor.departmentId));
        if (matchedDepartment) {
          this.selectedDepartment = matchedDepartment;
        }
      }
    },
    clearDoctorSelection() {
      this.selectedDoctor = null;
      uni.showToast({ title: '已设为不指定医生', icon: 'none' });
    },
    displayCompanionName(item) {
      return item?.companionName || item?.userName || '陪诊员';
    },
    async loadSelectedCompanion(id) {
      const companionId = Number(id);
      if (!companionId) return;
      try {
        const companion = await companionApi.detail(companionId);
        if (companion?.id) {
          this.selectedCompanion = companion;
          return;
        }
      } catch (error) {
        // ignore
      }
      this.selectedCompanion = { id: companionId, companionName: `陪诊员 #${companionId}` };
    },
    goChooseCompanion() {
      this.persistLocalDraft();
      uni.navigateTo({ url: '/pages/companion/list' });
    },
    clearSelectedCompanion() {
      if (!this.selectedCompanion) {
        uni.showToast({ title: '当前已是大厅抢单', icon: 'none' });
        return;
      }
      this.selectedCompanion = null;
      uni.showToast({ title: '已设为大厅抢单', icon: 'none' });
    },
    selectDuration(value) {
      this.duration = value;
      this.calculateCost();
    },
    onRequirementChange() {
      let extraCost = 0;
      if (this.requirements.includes('wheelchair')) extraCost += 50;
      if (this.requirements.includes('report')) extraCost += 30;
      if (this.requirements.includes('payment')) extraCost += 20;
      this.extraCost = extraCost;
      this.calculateCost();
    },
    calculateCost() {
      const current = this.durationOptions.find((item) => item.value === this.duration);
      this.baseCost = current ? current.price : 200;
      this.platformFee = Math.floor(this.baseCost * 0.1);
      this.totalCost = this.baseCost + this.extraCost + this.platformFee;
    },
    tryRestoreDraft() {
      const draft = uni.getStorageSync('medpal_appointment_draft');
      if (!draft || typeof draft !== 'object') return;
      const hasDraft = Boolean(
        draft.selectedHospital
        || draft.selectedDepartment
        || draft.selectedDoctor
        || draft.selectedCompanion
        || draft.appointmentStartTime
        || draft.contactPhone
        || draft.meetingPoint
        || draft.remarks
      );
      if (!hasDraft) return;
      if (this.forceCompanionIdFromRoute || this.draftId) {
        this.applyDraft(draft);
        return;
      }
      uni.showModal({
        title: '发现预约草稿',
        content: '是否恢复上次未提交的预约信息？',
        success: (res) => {
          if (res.confirm) {
            this.applyDraft(draft);
          }
        }
      });
    },
    async applyDraft(draft) {
      this.draftId = draft.draftId || this.draftId;
      this.selectedHospital = draft.selectedHospital || null;
      this.selectedDepartment = draft.selectedDepartment || null;
      this.selectedDoctor = draft.selectedDoctor || null;
      if (!this.forceCompanionIdFromRoute) {
        this.selectedCompanion = draft.selectedCompanion || this.selectedCompanion;
      }
      this.appointmentStartTime = draft.appointmentStartTime
        || draft.appointmentDate
        || (Array.isArray(draft.appointmentTimeRange) ? draft.appointmentTimeRange[0] : '')
        || '';
      this.duration = draft.duration || this.duration;
      this.requirements = Array.isArray(draft.requirements) ? draft.requirements : [];
      this.customRequirements = Array.isArray(draft.customRequirements) ? draft.customRequirements : [];
      this.visitRole = draft.visitRole || this.visitRole;
      this.contactPhone = draft.contactPhone || this.contactPhone;
      this.meetingPoint = draft.meetingPoint || '';
      this.remarks = draft.remarks || '';
      this.onRequirementChange();
      if (this.selectedHospital?.id) {
        await Promise.all([
          this.loadDepartments(this.selectedHospital.id),
          this.loadDoctors({ hospitalId: this.selectedHospital.id })
        ]);
      }
    },
    persistLocalDraft() {
      uni.setStorageSync('medpal_appointment_draft', {
        draftId: this.draftId,
        selectedHospital: this.selectedHospital,
        selectedDepartment: this.selectedDepartment,
        selectedDoctor: this.selectedDoctor,
        selectedCompanion: this.selectedCompanion,
        appointmentStartTime: this.appointmentStartTime,
        duration: this.duration,
        requirements: this.requirements,
        customRequirements: this.customRequirements,
        visitRole: this.visitRole,
        contactPhone: this.contactPhone,
        meetingPoint: this.meetingPoint,
        remarks: this.remarks
      });
    },
    formatPickerDateTime(value) {
      const date = new Date(value);
      if (Number.isNaN(date.getTime())) return '';
      const pad = (num) => String(num).padStart(2, '0');
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
    },
    formatApiDateTime(value) {
      if (!value) return null;
      const date = new Date(value);
      if (Number.isNaN(date.getTime())) return null;
      const pad = (num) => String(num).padStart(2, '0');
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:00`;
    },
    getVisitRoleLabel() {
      const current = this.visitRoleOptions.find((item) => item.value === this.visitRole);
      return current?.label || '本人就诊';
    },
    getRequirementLabels() {
      return this.requirementOptions.filter((item) => this.requirements.includes(item.value)).map((item) => item.text);
    },
    addCustomRequirement() {
      const text = (this.customRequirementInput || '').trim();
      if (!text) return;
      if (this.customRequirements.includes(text)) {
        uni.showToast({ title: '该自定义需求已添加', icon: 'none' });
        return;
      }
      this.customRequirements.push(text);
      this.customRequirementInput = '';
    },
    removeCustomRequirement(index) {
      this.customRequirements.splice(index, 1);
    },
    buildSpecificNeeds(rangeText) {
      const sections = [
        `就诊人：${this.getVisitRoleLabel()}`,
        `时间区间：${rangeText}`,
        `联系手机号：${this.contactPhone}`
      ];
      if (this.selectedDepartment?.departmentName) {
        sections.push(`科室偏好：${this.selectedDepartment.departmentName}`);
      }
      if (this.selectedDoctor?.doctorName) {
        sections.push(`医生偏好：${this.selectedDoctor.doctorName}`);
      } else {
        sections.push('医生偏好：不指定');
      }
      if (this.selectedCompanion?.id) {
        sections.push(`指定陪诊员：${this.displayCompanionName(this.selectedCompanion)}`);
      } else {
        sections.push('接单方式：大厅抢单');
      }
      if (this.meetingPoint) sections.push(`汇合地点：${this.meetingPoint}`);
      const requirementLabels = this.getRequirementLabels();
      if (requirementLabels.length) sections.push(`额外需求：${requirementLabels.join('、')}`);
      if (this.customRequirements.length) sections.push(`自定义需求：${this.customRequirements.join('、')}`);
      if (this.remarks) sections.push(`补充说明：${this.remarks}`);
      return sections.join('；');
    },
    buildOrderPayload() {
      const start = this.appointmentStartTime ? new Date(this.appointmentStartTime) : null;
      const validStart = start && !Number.isNaN(start.getTime()) ? start : null;
      const end = validStart ? this.appointmentEndTime : null;
      const rangeText = validStart && end ? `${formatDateTime(validStart)} 至 ${formatDateTime(end)}` : '';
      return {
        id: this.draftId || undefined,
        hospitalId: this.selectedHospital?.id || null,
        departmentId: this.selectedDepartment?.id || this.selectedDoctor?.departmentId || null,
        doctorId: this.selectedDoctor?.id || null,
        companionId: this.selectedCompanion?.id || null,
        appointmentDate: this.formatApiDateTime(validStart),
        duration: `${this.duration}h`,
        specificNeeds: this.buildSpecificNeeds(rangeText),
        totalPrice: this.totalCost,
        serviceFee: this.baseCost,
        extraFee: this.extraCost,
        platformFee: this.platformFee
      };
    },
    async saveDraft() {
      if (this.draftSubmitting) return;
      if (!this.selectedHospital) {
        uni.showToast({ title: '请先选择医院', icon: 'none' });
        return;
      }
      this.persistLocalDraft();
      this.draftSubmitting = true;
      try {
        const draft = await orderApi.saveDraft(this.buildOrderPayload());
        this.draftId = draft?.id || this.draftId;
        this.persistLocalDraft();
        uni.showToast({ title: '已保存草稿', icon: 'success' });
      } catch (error) {
        uni.showToast({ title: error.message || '草稿保存失败', icon: 'none' });
      } finally {
        this.draftSubmitting = false;
      }
    },
    async submitAppointment() {
      if (this.submitting) return;
      if (!this.selectedHospital) {
        return uni.showToast({ title: '请先选择医院', icon: 'none' });
      }
      if (!this.selectedDepartment && !this.selectedDoctor) {
        return uni.showToast({ title: '请至少选择科室或医生', icon: 'none' });
      }
      const rangeStart = this.appointmentStartTime ? new Date(this.appointmentStartTime) : null;
      const rangeEnd = this.appointmentEndTime;
      if (!rangeStart || !rangeEnd || Number.isNaN(rangeStart.getTime()) || Number.isNaN(rangeEnd.getTime())) {
        return uni.showToast({ title: '请选择开始时间', icon: 'none' });
      }
      if (rangeStart.getTime() <= Date.now()) {
        return uni.showToast({ title: '开始时间需晚于当前时间', icon: 'none' });
      }
      if (!this.contactPhone || this.contactPhone.length !== 11) {
        return uni.showToast({ title: '请填写11位联系手机号', icon: 'none' });
      }
      const user = this.userInfo || uni.getStorageSync('userInfo');
      if (!user?.id) {
        uni.showToast({ title: '请先登录', icon: 'none' });
        return uni.navigateTo({ url: '/pages/login/index' });
      }
      this.submitting = true;
      try {
        const payload = this.buildOrderPayload();
        delete payload.id;
        const order = await orderApi.create(payload);
        uni.removeStorageSync('medpal_appointment_draft');
        uni.showToast({ title: '预约成功', icon: 'success' });
        setTimeout(() => {
          uni.redirectTo({ url: `/pages/order/detail?id=${order.id}` });
        }, 800);
      } catch (error) {
        uni.showToast({ title: error.message || '预约失败', icon: 'none' });
      } finally {
        this.submitting = false;
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

.section {
  margin-top: 24rpx;
}

.search-card {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 16rpx;
  box-shadow: 0 12rpx 30rpx rgba(33, 56, 100, 0.08);
}

.step-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10rpx;
}

.back-step-btn {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: rgba(47, 101, 249, 0.1);
  color: #2f65f9;
  font-size: 22rpx;
}

.step-hint {
  font-size: 22rpx;
  color: #7b8291;
}

.filters {
  display: flex;
  gap: 12rpx;
  margin-top: 12rpx;
  flex-wrap: wrap;
}

.filter-chip {
  padding: 6rpx 18rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  background: rgba(47, 101, 249, 0.1);
  color: #2f65f9;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1c2b4a;
  margin-bottom: 14rpx;
}

.section-subtitle-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.section-subtitle {
  font-size: 24rpx;
  font-weight: 600;
  color: #1c2b4a;
}

.section-subhint {
  font-size: 21rpx;
  color: #8e98ab;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.compact-list {
  gap: 10rpx;
}

.list-card {
  background: #ffffff;
  border-radius: 18rpx;
  padding: 18rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
  border: 1rpx solid transparent;
}

.list-card.active,
.compact-card.active {
  border-color: rgba(47, 101, 249, 0.36);
  background: rgba(47, 101, 249, 0.04);
}

.list-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.list-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #1c2b4a;
}

.list-sub {
  font-size: 22rpx;
  color: #7b8291;
}

.list-meta {
  margin-top: 8rpx;
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
}

.meta-item {
  font-size: 21rpx;
  color: #4d5c7a;
}

.meta-dot {
  color: #b4bccb;
  font-size: 18rpx;
}

.list-badge {
  font-size: 22rpx;
  color: #2f65f9;
}

.hospital-picked-card,
.choice-summary {
  background: #ffffff;
  border-radius: 18rpx;
  padding: 18rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.choice-summary {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12rpx;
  margin-top: 12rpx;
}

.summary-item {
  background: #f7f9ff;
  border-radius: 14rpx;
  padding: 14rpx;
}

.summary-label {
  display: block;
  font-size: 20rpx;
  color: #7b8291;
}

.summary-value {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #1c2b4a;
  font-weight: 600;
}

.picked-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.picked-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #7b8291;
}

.split-section {
  margin-top: 16rpx;
}

.empty-inline {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 20rpx;
  color: #8e98ab;
  font-size: 22rpx;
}

.doctor-skip-wrap {
  margin-top: 16rpx;
}

.doctor-skip-btn {
  width: 100%;
}

.form-card {
  background: #ffffff;
  border-radius: 18rpx;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  box-shadow: 0 12rpx 30rpx rgba(33, 56, 100, 0.08);
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.form-label {
  font-size: 24rpx;
  color: #1c2b4a;
  font-weight: 600;
}

.form-hint {
  font-size: 20rpx;
  color: #7b8291;
}

.readonly-box {
  min-height: 84rpx;
  display: flex;
  align-items: center;
  padding: 0 20rpx;
  border-radius: 16rpx;
  background: #f4f6fb;
  color: #1c2b4a;
  font-size: 24rpx;
}

.companion-select-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1rpx solid #e1e6f2;
  background: #f8faff;
  border-radius: 16rpx;
  padding: 16rpx;
}

.companion-select-main {
  flex: 1;
  min-width: 0;
  margin-right: 12rpx;
}

.companion-select-title {
  display: block;
  font-size: 24rpx;
  color: #1c2b4a;
  font-weight: 600;
}

.companion-select-sub {
  display: block;
  margin-top: 6rpx;
  font-size: 21rpx;
  color: #7b8291;
}

.companion-action-row {
  display: flex;
  gap: 12rpx;
}

.companion-action-btn {
  flex: 1;
  margin: 0;
}

.companion-action-btn-active {
  color: #2f65f9;
  border-color: rgba(47, 101, 249, 0.3);
  background: rgba(47, 101, 249, 0.08);
}

.visit-role-list {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}

.visit-role-item {
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  color: #4d5c7a;
  background: #f4f6fb;
  border: 1rpx solid transparent;
}

.visit-role-item.active {
  color: #2f65f9;
  background: rgba(47, 101, 249, 0.1);
  border-color: #2f65f9;
}

.custom-requirement-row {
  display: flex;
  gap: 10rpx;
  align-items: center;
}

.btn-add-requirement {
  flex-shrink: 0;
  padding: 0 22rpx;
  height: 64rpx;
  line-height: 64rpx;
  border-radius: 999rpx;
  background: rgba(47, 101, 249, 0.14);
  color: #2f65f9;
  font-size: 22rpx;
  border: 1rpx solid rgba(47, 101, 249, 0.25);
}

.custom-requirement-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.custom-requirement-chip {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #f4f6fb;
  border: 1rpx solid #e1e6f2;
}

.custom-requirement-text {
  font-size: 21rpx;
  color: #4d5c7a;
  max-width: 360rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.duration-list {
  display: flex;
  gap: 12rpx;
}

.duration-card {
  flex: 1;
  background: #f4f6fb;
  border-radius: 16rpx;
  padding: 14rpx;
  text-align: center;
  border: 1rpx solid transparent;
}

.duration-card.active {
  background: rgba(47, 101, 249, 0.12);
  border-color: #2f65f9;
}

.duration-label {
  display: block;
  font-size: 24rpx;
  font-weight: 600;
  color: #1c2b4a;
}

.duration-price {
  font-size: 22rpx;
  color: #2f65f9;
}

.cost-card {
  margin-top: 18rpx;
  background: #ffffff;
  border-radius: 18rpx;
  padding: 20rpx;
  box-shadow: 0 12rpx 30rpx rgba(33, 56, 100, 0.08);
}

.cost-row {
  display: flex;
  justify-content: space-between;
  font-size: 24rpx;
  color: #5f6f94;
  margin-bottom: 8rpx;
}

.cost-total {
  display: flex;
  justify-content: space-between;
  font-size: 26rpx;
  font-weight: 700;
  color: #1c2b4a;
}

.action-row {
  display: flex;
  gap: 12rpx;
  margin-top: 20rpx;
}

.btn-primary {
  flex: 1;
  background: #2f65f9;
  color: #fff;
  border-radius: 999rpx;
  font-size: 24rpx;
  padding: 16rpx 0;
}

.btn-secondary {
  flex: 1;
  background: #ffffff;
  color: #2f65f9;
  border-radius: 999rpx;
  font-size: 24rpx;
  padding: 16rpx 0;
  border: 1rpx solid rgba(47, 101, 249, 0.3);
}

.btn-ghost {
  flex: 1;
  background: #f4f6fb;
  color: #5f6f94;
  border-radius: 999rpx;
  font-size: 24rpx;
  padding: 16rpx 0;
  border: 1rpx solid #e1e6f2;
}
</style>
