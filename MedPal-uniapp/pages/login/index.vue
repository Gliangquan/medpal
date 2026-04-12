<template>
  <view class="page-container flex-center">
    <view class="card" style="width: 100%; max-width: 600rpx;">
      <view class="page-header">
        <text class="title">用户管理</text>
        <text class="subtitle">{{ showRegister ? '注册' : '登录' }}</text>
      </view>

      <view v-if="!showRegister" class="form-wrapper">
        <uni-segmented-control
          :current="loginTypeIndex"
          :values="['账号登录', '手机登录']"
          style-type="button"
          active-color="#2f65f9"
          @clickItem="switchLoginType"
        />

        <view class="form">
          <view class="input-item" v-if="loginType === 'account'">
            <uni-easyinput v-model="form.userAccount" placeholder="请输入账号" />
          </view>
          <view class="input-item" v-if="loginType === 'phone'">
            <uni-easyinput v-model="form.userPhone" type="number" placeholder="请输入手机号" />
          </view>
          <view class="input-item">
            <uni-easyinput v-model="form.userPassword" type="password" placeholder="请输入密码" />
          </view>

          <view class="btn-wrapper">
            <button class="btn-primary" :disabled="loading" @tap="handleLogin">
              {{ loading ? '登录中...' : '登录' }}
            </button>
          </view>
        </view>

        <view class="links">
          <text @tap="showRegister = true">注册账号</text>
          <text @tap="forgetPassword">忘记密码</text>
        </view>

        <view class="other-login">
          <uni-divider text="其他登录方式" />
          <view class="btn-wrapper">
            <button class="btn-ghost" @tap="wechatLogin">微信一键登录</button>
          </view>
        </view>
      </view>

      <view v-else class="form-wrapper">
        <view class="form">
          <view class="input-item">
            <text class="label">注册类型</text>
            <uni-segmented-control
              :current="registerTypeIndex"
              :values="['患者', '陪诊员']"
              style-type="button"
              active-color="#2f65f9"
              @clickItem="switchRegisterType"
            />
          </view>
          <view class="input-item">
            <uni-easyinput v-model="registerForm.userAccount" placeholder="请输入账号" />
          </view>
          <view class="input-item">
            <uni-easyinput v-model="registerForm.userName" placeholder="请输入昵称" />
          </view>
          <view class="input-item">
            <uni-easyinput v-model="registerForm.userPhone" type="number" placeholder="请输入手机号" />
          </view>
          <view class="input-item">
            <uni-easyinput v-model="registerForm.userPassword" type="password" placeholder="请输入密码" />
          </view>
          <view class="input-item">
            <uni-easyinput v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码" />
          </view>

          <view v-if="registerForm.userRole === 'companion'" class="input-item">
            <text class="label">身份证号</text>
            <uni-easyinput v-model="registerForm.idCard" placeholder="请输入身份证号" />
          </view>

          <view class="btn-wrapper">
            <button class="btn-primary" :disabled="registering" @tap="handleRegister">
              {{ registering ? '注册中...' : '注册' }}
            </button>
          </view>
        </view>

        <view class="links">
          <text @tap="showRegister = false">返回登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { userApi } from '@/utils/api.js';
import { setToken } from '@/utils/request.js';

export default {
  data() {
    return {
      showRegister: false,
      loginType: 'account',
      registerType: 'patient',
      form: {
        userAccount: '',
        userPhone: '',
        userPassword: ''
      },
      registerForm: {
        userAccount: '',
        userName: '',
        userPhone: '',
        userPassword: '',
        confirmPassword: '',
        userRole: 'patient',
        idCard: ''
      },
      loading: false,
      registering: false
    };
  },
  computed: {
    loginTypeIndex() {
      return this.loginType === 'account' ? 0 : 1;
    },
    registerTypeIndex() {
      return this.registerType === 'patient' ? 0 : 1;
    }
  },
  onLoad() {
    const user = uni.getStorageSync('userInfo');
    if (user && user.id) {
      this.goHome();
    }
  },
  methods: {
    switchLoginType(e) {
      this.loginType = e.currentIndex === 0 ? 'account' : 'phone';
    },
    switchRegisterType(e) {
      this.registerType = e.currentIndex === 0 ? 'patient' : 'companion';
      this.registerForm.userRole = this.registerType;
    },
    async handleLogin() {
      if (this.loading) return;

      const { userAccount, userPhone, userPassword } = this.form;
      if (this.loginType === 'account' && !userAccount) {
        return uni.showToast({ title: '请输入账号', icon: 'none' });
      }
      if (this.loginType === 'phone' && !userPhone) {
        return uni.showToast({ title: '请输入手机号', icon: 'none' });
      }
      if (!userPassword) {
        return uni.showToast({ title: '请输入密码', icon: 'none' });
      }

      this.loading = true;
      try {
        const payload = {
          loginType: this.loginType,
          userAccount: this.loginType === 'account' ? userAccount : undefined,
          userPhone: this.loginType === 'phone' ? userPhone : undefined,
          userPassword
        };
        const user = await userApi.login(payload);
        setToken(user.token);
        uni.setStorageSync('userInfo', user);
        uni.showToast({ title: '登录成功', icon: 'success' });
        setTimeout(() => {
          this.goHome();
        }, 800);
      } catch (error) {
        uni.showToast({ title: error.message || '登录失败', icon: 'none' });
      } finally {
        this.loading = false;
      }
    },
    async handleRegister() {
      if (this.registering) return;
      const { userAccount, userName, userPhone, userPassword, confirmPassword, userRole, idCard } = this.registerForm;
      if (!userAccount || !userName || !userPhone || !userPassword || !confirmPassword) {
        return uni.showToast({ title: '请填写完整信息', icon: 'none' });
      }
      if (userPassword !== confirmPassword) {
        return uni.showToast({ title: '两次密码不一致', icon: 'none' });
      }
      if (userRole === 'companion' && !idCard) {
        return uni.showToast({ title: '陪诊员需要填写身份证号', icon: 'none' });
      }
      this.registering = true;
      try {
        await userApi.register({
          userAccount,
          userName,
          userPhone,
          userPassword,
          checkPassword: confirmPassword,
          userRole,
          idCard: userRole === 'companion' ? idCard : undefined
        });
        uni.showToast({ title: '注册成功，请登录', icon: 'success' });
        this.showRegister = false;
      } catch (error) {
        uni.showToast({ title: error.message || '注册失败', icon: 'none' });
      } finally {
        this.registering = false;
      }
    },
    forgetPassword() {
      uni.showModal({
        title: '忘记密码',
        content: '当前版本暂未接入自助找回密码，请联系管理员或客服为您重置密码。',
        showCancel: false
      });
    },
    async wechatLogin() {
      if (this.loading) return;

      // #ifndef MP-WEIXIN
      uni.showToast({ title: '请在微信小程序中使用', icon: 'none' });
      return;
      // #endif

      this.loading = true;
      try {
        const loginRes = await new Promise((resolve, reject) => {
          uni.login({
            provider: 'weixin',
            success: resolve,
            fail: reject
          });
        });

        if (!loginRes || !loginRes.code) {
          throw new Error('获取微信登录凭证失败');
        }

        const user = await userApi.wechatLogin({
          code: loginRes.code
        });
        setToken(user.token);
        uni.setStorageSync('userInfo', user);
        uni.showToast({ title: '登录成功', icon: 'success' });
        setTimeout(() => {
          this.goHome();
        }, 800);
      } catch (error) {
        uni.showToast({ title: error.message || '微信登录失败', icon: 'none' });
      } finally {
        this.loading = false;
      }
    },
    goHome() {
      uni.switchTab({
        url: '/pages/index/index',
        fail: () => {
          uni.reLaunch({ url: '/pages/index/index' });
        }
      });
    }
  }
};
</script>

<style lang="scss">
@import "@/styles/common.scss";

.other-login {
  margin-top: $spacing-lg;
}
</style>
