<template>
  <view class="page">
    <view v-if="!isCompanionUser" class="empty-box">
      <uni-icons type="info" size="40" color="#9aa4b8"></uni-icons>
      <text class="empty-text">仅陪诊员可进入认证中心</text>
      <button class="btn-secondary" @tap="goBack">返回</button>
    </view>

    <view v-else>
      <view class="status-grid">
        <view class="status-card">
          <text class="status-label">实名认证</text>
          <text class="status-value">{{ realNameStatusText }}</text>
          <text class="status-tip">身份证号 + 正反面照片</text>
        </view>
        <view class="status-card">
          <text class="status-label">资质认证</text>
          <text class="status-value">{{ qualificationStatusText }}</text>
          <text class="status-tip">资质类型 + 证书照片</text>
        </view>
      </view>

      <uni-section title="实名认证资料" type="line"></uni-section>
      <view class="form-card">
        <view class="form-item">
          <text class="form-label">身份证号</text>
          <uni-easyinput
            v-model="form.idCard"
            placeholder="请输入18位身份证号"
            maxlength="18"
            :disabled="realNameApproved"
          />
        </view>
        <view class="form-item">
          <text class="form-label">身份证正面</text>
          <view v-if="realNameApproved" class="readonly-file">
            <image
              v-if="idCardFrontFiles.length"
              class="readonly-file-image"
              :src="idCardFrontFiles[0].url"
              mode="aspectFill"
            />
            <text v-else class="readonly-file-text">已通过认证，未读取到图片</text>
            <text class="readonly-lock-text">已通过，不能修改</text>
          </view>
          <uni-file-picker
            v-else
            v-model="idCardFrontFiles"
            limit="1"
            file-mediatype="image"
            @select="handlePick($event, 'idCardFront', 'id_card')"
            @delete="clearFile('idCardFront')"
          />
        </view>
        <view class="form-item">
          <text class="form-label">身份证反面</text>
          <view v-if="realNameApproved" class="readonly-file">
            <image
              v-if="idCardBackFiles.length"
              class="readonly-file-image"
              :src="idCardBackFiles[0].url"
              mode="aspectFill"
            />
            <text v-else class="readonly-file-text">已通过认证，未读取到图片</text>
            <text class="readonly-lock-text">已通过，不能修改</text>
          </view>
          <uni-file-picker
            v-else
            v-model="idCardBackFiles"
            limit="1"
            file-mediatype="image"
            @select="handlePick($event, 'idCardBack', 'id_card')"
            @delete="clearFile('idCardBack')"
          />
        </view>
      </view>

      <uni-section title="资质认证资料" type="line"></uni-section>
      <view class="form-card">
        <view class="form-item">
          <text class="form-label">资质类型</text>
          <uni-data-select
            v-model="form.qualificationType"
            :localdata="qualificationOptions"
            :disabled="qualificationApproved"
          />
        </view>
        <view class="form-item">
          <text class="form-label">资质证书图片</text>
          <view v-if="qualificationApproved" class="readonly-file">
            <image
              v-if="qualificationFiles.length"
              class="readonly-file-image"
              :src="qualificationFiles[0].url"
              mode="aspectFill"
            />
            <text v-else class="readonly-file-text">已通过认证，未读取到图片</text>
            <text class="readonly-lock-text">已通过，不能修改</text>
          </view>
          <uni-file-picker
            v-else
            v-model="qualificationFiles"
            limit="1"
            file-mediatype="image"
            @select="handlePick($event, 'qualificationCert', 'qualification')"
            @delete="clearFile('qualificationCert')"
          />
        </view>
        <view class="form-item">
          <text class="form-label">工作年限（年）</text>
          <uni-easyinput
            v-model="form.workYears"
            type="number"
            placeholder="例如 3"
            :disabled="qualificationApproved"
          />
        </view>
        <view class="form-item">
          <text class="form-label">擅长方向</text>
          <uni-easyinput
            v-model="form.specialties"
            placeholder="例如：门诊取号、老年陪同、取报告"
            :disabled="qualificationApproved"
          />
        </view>
        <view class="form-item">
          <text class="form-label">服务区域</text>
          <uni-easyinput
            v-model="form.serviceArea"
            placeholder="例如：上海市徐汇区/浦东新区"
            :disabled="qualificationApproved"
          />
        </view>
      </view>

      <view class="tips">
        <text>提交后将进入人工审核。实名认证与资质认证均通过后，你才可正常接单。</text>
      </view>

      <view class="btn-row">
        <button class="btn-secondary" @tap="goBack">返回</button>
        <button v-if="!allApproved" class="btn-primary" :disabled="submitting || uploadingAny" @tap="submitCertification">
          {{ uploadingAny ? '上传中...' : '提交审核' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import { fileApi, userApi } from '@/utils/api.js';
import { BASE_URL } from '@/utils/request.js';
import { certStatusText, isCompanionRole, normalizeCertStatus } from '@/utils/permission.js';

export default {
  data() {
    return {
      userInfo: null,
      pageInited: false,
      submitting: false,
      uploading: {
        idCardFront: false,
        idCardBack: false,
        qualificationCert: false
      },
      form: {
        idCard: '',
        idCardFront: '',
        idCardBack: '',
        qualificationType: '',
        qualificationCert: '',
        workYears: '',
        specialties: '',
        serviceArea: ''
      },
      idCardFrontFiles: [],
      idCardBackFiles: [],
      qualificationFiles: [],
      qualificationOptions: [
        { text: '护理相关证书', value: 'nurse' },
        { text: '医护从业证书', value: 'medical' },
        { text: '陪诊培训证书', value: 'escort' },
        { text: '其他', value: 'other' }
      ]
    };
  },
  computed: {
    isCompanionUser() {
      return isCompanionRole(this.userInfo);
    },
    realNameStatusText() {
      return certStatusText(this.userInfo?.realNameStatus);
    },
    qualificationStatusText() {
      return certStatusText(this.userInfo?.qualificationStatus);
    },
    realNameApproved() {
      return normalizeCertStatus(this.userInfo?.realNameStatus) === 'approved';
    },
    qualificationApproved() {
      return normalizeCertStatus(this.userInfo?.qualificationStatus) === 'approved';
    },
    allApproved() {
      return this.realNameApproved && this.qualificationApproved;
    },
    uploadingAny() {
      return Object.values(this.uploading).some(Boolean);
    }
  },
  async onLoad() {
    await this.loadUser({ resetForm: true });
    this.pageInited = true;
  },
  onShow() {
    if (!this.pageInited) return;
    const cached = uni.getStorageSync('userInfo');
    if (cached?.id) {
      this.userInfo = cached;
    }
  },
  methods: {
    normalizeFileUrl(url) {
      if (!url) return '';
      if (/^https?:\/\//.test(url)) return url;
      const host = BASE_URL.replace(/\/api$/, '');
      if (url.startsWith('/')) return `${host}${url}`;
      return `${BASE_URL}/${url}`;
    },
    setFileList(field, rawUrl) {
      const url = this.normalizeFileUrl(rawUrl);
      const file = url ? [{ url, name: field }] : [];
      if (field === 'idCardFront') this.idCardFrontFiles = file;
      if (field === 'idCardBack') this.idCardBackFiles = file;
      if (field === 'qualificationCert') this.qualificationFiles = file;
    },
    isFieldLocked(field) {
      const realNameFields = ['idCard', 'idCardFront', 'idCardBack'];
      const qualificationFields = ['qualificationType', 'qualificationCert', 'workYears', 'specialties', 'serviceArea'];
      if (realNameFields.includes(field)) return this.realNameApproved;
      if (qualificationFields.includes(field)) return this.qualificationApproved;
      return false;
    },
    async loadUser(options = {}) {
      const { resetForm = false } = options;
      const cached = uni.getStorageSync('userInfo');
      this.userInfo = cached || null;
      try {
        const fresh = await userApi.fetchCurrentUser();
        if (fresh) {
          this.userInfo = fresh;
          uni.setStorageSync('userInfo', fresh);
        }
      } catch (error) {
        // ignore
      }

      if (!this.isCompanionUser) return;
      if (resetForm) {
        this.form = {
          idCard: this.userInfo?.idCard || '',
          idCardFront: this.userInfo?.idCardFront || '',
          idCardBack: this.userInfo?.idCardBack || '',
          qualificationType: this.userInfo?.qualificationType || '',
          qualificationCert: this.userInfo?.qualificationCert || '',
          workYears: this.userInfo?.workYears ? String(this.userInfo.workYears) : '',
          specialties: this.userInfo?.specialties || '',
          serviceArea: this.userInfo?.serviceArea || ''
        };
        this.setFileList('idCardFront', this.form.idCardFront);
        this.setFileList('idCardBack', this.form.idCardBack);
        this.setFileList('qualificationCert', this.form.qualificationCert);
      }
    },
    async handlePick(e, field, biz) {
      if (this.isFieldLocked(field)) {
        uni.showToast({ title: '该认证已通过，不能修改', icon: 'none' });
        return;
      }
      const tempFile = e?.tempFiles?.[0];
      if (!tempFile?.path) return;
      this.uploading[field] = true;
      try {
        const url = await fileApi.upload(tempFile.path, biz);
        this.form[field] = url;
        this.setFileList(field, url);
        uni.showToast({ title: '上传成功', icon: 'success' });
      } catch (error) {
        uni.showToast({ title: error.message || '上传失败', icon: 'none' });
      } finally {
        this.uploading[field] = false;
      }
    },
    clearFile(field) {
      if (this.isFieldLocked(field)) {
        uni.showToast({ title: '该认证已通过，不能修改', icon: 'none' });
        return;
      }
      this.form[field] = '';
      this.setFileList(field, '');
    },
    validateForm() {
      if (this.allApproved) {
        uni.showToast({ title: '认证已全部通过，无需重复提交', icon: 'none' });
        return false;
      }
      if (!this.realNameApproved) {
        const idCard = (this.form.idCard || '').trim();
        if (!/^(\d{15}|\d{17}[0-9Xx])$/.test(idCard)) {
          uni.showToast({ title: '请填写有效身份证号', icon: 'none' });
          return false;
        }
        if (!this.form.idCardFront) {
          uni.showToast({ title: '请上传身份证正面', icon: 'none' });
          return false;
        }
        if (!this.form.idCardBack) {
          uni.showToast({ title: '请上传身份证反面', icon: 'none' });
          return false;
        }
      }
      if (!this.qualificationApproved) {
        if (!this.form.qualificationType) {
          uni.showToast({ title: '请选择资质类型', icon: 'none' });
          return false;
        }
        if (!this.form.qualificationCert) {
          uni.showToast({ title: '请上传资质证书图片', icon: 'none' });
          return false;
        }
        if (!this.form.workYears) {
          uni.showToast({ title: '请填写工作年限', icon: 'none' });
          return false;
        }
        if (!this.form.specialties.trim()) {
          uni.showToast({ title: '请填写擅长方向', icon: 'none' });
          return false;
        }
        if (!this.form.serviceArea.trim()) {
          uni.showToast({ title: '请填写服务区域', icon: 'none' });
          return false;
        }
      }
      return true;
    },
    async submitCertification() {
      if (this.submitting || this.uploadingAny) return;
      if (!this.isCompanionUser) {
        uni.showToast({ title: '仅陪诊员可提交认证', icon: 'none' });
        return;
      }
      if (this.allApproved) {
        uni.showToast({ title: '认证已通过，无需提交', icon: 'none' });
        return;
      }
      if (!this.validateForm()) return;

      this.submitting = true;
      try {
        await userApi.submitCompanionCertification({
          idCard: this.form.idCard.trim(),
          idCardFront: this.form.idCardFront,
          idCardBack: this.form.idCardBack,
          qualificationType: this.form.qualificationType,
          qualificationCert: this.form.qualificationCert,
          workYears: Number(this.form.workYears),
          specialties: this.form.specialties.trim(),
          serviceArea: this.form.serviceArea.trim()
        });

        const fresh = await userApi.fetchCurrentUser();
        if (fresh) {
          this.userInfo = fresh;
          uni.setStorageSync('userInfo', fresh);
        }
        uni.showToast({ title: '提交成功，等待审核', icon: 'success' });
      } catch (error) {
        uni.showToast({ title: error.message || '提交失败', icon: 'none' });
      } finally {
        this.submitting = false;
      }
    },
    goBack() {
      const pages = getCurrentPages();
      if (pages.length > 1) {
        uni.navigateBack();
      } else {
        uni.switchTab({ url: '/pages/profile/index' });
      }
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

.status-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14rpx;
  margin-bottom: 16rpx;
}

.status-card {
  background: #ffffff;
  border-radius: 18rpx;
  padding: 18rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.status-label {
  font-size: 22rpx;
  color: #7b8291;
}

.status-value {
  display: block;
  margin-top: 8rpx;
  font-size: 28rpx;
  color: #1c2b4a;
  font-weight: 700;
}

.status-tip {
  display: block;
  margin-top: 8rpx;
  color: #8e98ab;
  font-size: 21rpx;
}

.form-card {
  background: #ffffff;
  border-radius: 18rpx;
  padding: 18rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.form-item {
  margin-bottom: 16rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-label {
  display: block;
  margin-bottom: 8rpx;
  font-size: 23rpx;
  color: #2d3950;
}

.readonly-file {
  position: relative;
  min-height: 180rpx;
  border-radius: 14rpx;
  border: 1rpx dashed #c8d2e6;
  background: #f8faff;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.readonly-file-image {
  width: 100%;
  height: 180rpx;
}

.readonly-file-text {
  color: #8e98ab;
  font-size: 22rpx;
}

.readonly-lock-text {
  position: absolute;
  right: 10rpx;
  bottom: 10rpx;
  padding: 4rpx 10rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
  color: #2f65f9;
  background: rgba(47, 101, 249, 0.12);
}

.tips {
  margin-top: 16rpx;
  font-size: 21rpx;
  color: #7b8291;
}

.btn-row {
  margin-top: 22rpx;
  display: flex;
  gap: 12rpx;
}

.btn-primary,
.btn-secondary {
  flex: 1;
  border-radius: 999rpx;
}

.btn-primary {
  background: #2f65f9;
  color: #ffffff;
}

.btn-secondary {
  background: #ffffff;
  color: #2f65f9;
  border: 1rpx solid rgba(47, 101, 249, 0.35);
}

.empty-box {
  margin-top: 160rpx;
  text-align: center;
  color: #7b8291;
}

.empty-text {
  display: block;
  margin: 16rpx 0;
  font-size: 24rpx;
}
</style>
