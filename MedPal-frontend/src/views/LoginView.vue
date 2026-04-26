<template>
  <div class="login-wrapper">
    <div class="login-container">
      <!-- 左侧品牌区域 -->
      <div class="login-brand">
        <div class="brand-content">
          <div class="brand-logo">
            <a-avatar size="80" style="background-color: #1890ff">
              <template #icon>
                <shop-outlined />
              </template>
            </a-avatar>
          </div>
          <h1 class="brand-title">MedPal</h1>
          <p class="brand-subtitle">医疗陪伴服务平台</p>
          
          <div class="brand-features">
            <div class="feature-item">
              <check-circle-outlined />
              <span>专业陪诊服务</span>
            </div>
            <div class="feature-item">
              <check-circle-outlined />
              <span>智能订单管理</span>
            </div>
            <div class="feature-item">
              <check-circle-outlined />
              <span>完善的财务体系</span>
            </div>
            <div class="feature-item">
              <check-circle-outlined />
              <span>实时数据分析</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单区域 -->
      <div class="login-form-wrapper">
        <div class="form-container">
          <h2 class="form-title">管理员登录</h2>
          <p class="form-subtitle">欢迎登录MedPal管理系统</p>

          <!-- 登录方式切换 -->
          <a-segmented
            v-model:value="activeKey"
            :options="[
              { label: '账号登录', value: 'account' },
              { label: '手机号登录', value: 'phone' }
            ]"
            block
            style="margin-bottom: 24px"
          />

          <!-- 账号登录表单 -->
          <a-form
            v-if="activeKey === 'account'"
            :model="loginForm"
            @finish="handleLogin"
            layout="vertical"
            class="login-form"
          >
            <a-form-item
              name="userAccount"
              :rules="[{ required: true, message: '请输入账号' }]"
            >
              <a-input
                v-model:value="loginForm.userAccount"
                placeholder="请输入账号"
                size="large"
              >
                <template #prefix>
                  <user-outlined />
                </template>
              </a-input>
            </a-form-item>
            <a-form-item
              name="userPassword"
              :rules="[{ required: true, message: '请输入密码' }]"
            >
              <a-input-password
                v-model:value="loginForm.userPassword"
                placeholder="请输入密码"
                size="large"
              >
                <template #prefix>
                  <lock-outlined />
                </template>
              </a-input-password>
            </a-form-item>
            <a-form-item>
              <a-button
                type="primary"
                html-type="submit"
                block
                size="large"
                :loading="loading"
              >
                登录
              </a-button>
            </a-form-item>
          </a-form>

          <!-- 手机号登录表单 -->
          <a-form
            v-if="activeKey === 'phone'"
            :model="loginForm"
            @finish="handleLogin"
            layout="vertical"
            class="login-form"
          >
            <a-form-item
              name="userPhone"
              :rules="[{ required: true, message: '请输入手机号' }]"
            >
              <a-input
                v-model:value="loginForm.userPhone"
                placeholder="请输入手机号"
                size="large"
              >
                <template #prefix>
                  <phone-outlined />
                </template>
              </a-input>
            </a-form-item>
            <a-form-item
              name="userPassword"
              :rules="[{ required: true, message: '请输入密码' }]"
            >
              <a-input-password
                v-model:value="loginForm.userPassword"
                placeholder="请输入密码"
                size="large"
              >
                <template #prefix>
                  <lock-outlined />
                </template>
              </a-input-password>
            </a-form-item>
            <a-form-item>
              <a-button
                type="primary"
                html-type="submit"
                block
                size="large"
                :loading="loading"
              >
                登录
              </a-button>
            </a-form-item>
          </a-form>

          <!-- 底部链接 -->
          <a-divider />
          <div class="form-footer">
            <a-button type="link" @click="handleForgotPassword">忘记密码</a-button>
          </div>
        </div>

        <a-modal
          v-model:open="resetPasswordVisible"
          title="找回密码"
          ok-text="确认重置"
          cancel-text="取消"
          :confirm-loading="resetLoading"
          @ok="submitResetPassword"
          @cancel="closeResetPassword"
        >
          <a-form layout="vertical">
            <a-form-item label="手机号" required>
              <a-input
                v-model:value="resetPasswordForm.userPhone"
                placeholder="请输入注册手机号"
                maxlength="11"
              >
                <template #prefix>
                  <phone-outlined />
                </template>
              </a-input>
            </a-form-item>
            <a-form-item label="新密码" required>
              <a-input-password
                v-model:value="resetPasswordForm.newPassword"
                placeholder="请输入新密码"
              >
                <template #prefix>
                  <lock-outlined />
                </template>
              </a-input-password>
            </a-form-item>
            <a-form-item label="确认新密码" required>
              <a-input-password
                v-model:value="resetPasswordForm.checkPassword"
                placeholder="请再次输入新密码"
              >
                <template #prefix>
                  <lock-outlined />
                </template>
              </a-input-password>
            </a-form-item>
          </a-form>
        </a-modal>

        <!-- 页脚 -->
        <div class="login-footer">
          <a-divider style="margin: 12px 0" />
          <p>&copy; 2024 MedPal医疗陪伴服务平台</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { message } from 'ant-design-vue';
import {
  UserOutlined,
  LockOutlined,
  PhoneOutlined,
  ShopOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons-vue';
import { resetPassword, userLogin } from '../api';
import type { UserLoginRequest, UserResetPasswordRequest } from '../api';

const activeKey = ref<'phone' | 'account'>('account');
const loginForm = ref({
  userPhone: '',
  userAccount: '',
  userPassword: '',
});
const loading = ref(false);
const resetLoading = ref(false);
const resetPasswordVisible = ref(false);
const resetPasswordForm = ref<UserResetPasswordRequest>({
  userPhone: '',
  newPassword: '',
  checkPassword: '',
});

// 登录方法
const handleLogin = async () => {
  try {
    loading.value = true;
    const params: UserLoginRequest = {
      loginType: activeKey.value,
      userPassword: loginForm.value.userPassword,
    };

    if (activeKey.value === 'phone') {
      params.userPhone = loginForm.value.userPhone;
    } else {
      params.userAccount = loginForm.value.userAccount;
    }

    const response = await userLogin(params);
    if (response.code === 0) {
      const loginUser = response.data;
      if (loginUser.userRole !== 'admin') {
        message.error('当前账号不是管理员，不能登录后台');
        return;
      }
      localStorage.setItem('user', JSON.stringify(loginUser));
      message.success('登录成功');
      window.location.href = '/admin/users';
    } else {
      message.error(response.message || '登录失败');
    }
  } catch (error) {
    message.error('登录失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 找回密码方法
const handleForgotPassword = () => {
  resetPasswordForm.value = {
    userPhone: loginForm.value.userPhone || '',
    newPassword: '',
    checkPassword: '',
  };
  resetPasswordVisible.value = true;
};

const closeResetPassword = () => {
  resetPasswordVisible.value = false;
};

const submitResetPassword = async () => {
  const { userPhone, newPassword, checkPassword } = resetPasswordForm.value;
  if (!userPhone) {
    message.warning('请输入手机号');
    return;
  }
  if (!/^1\d{10}$/.test(userPhone)) {
    message.warning('请输入正确的手机号');
    return;
  }
  if (!newPassword || newPassword.length < 6) {
    message.warning('新密码不能少于 6 位');
    return;
  }
  if (newPassword !== checkPassword) {
    message.warning('两次输入的新密码不一致');
    return;
  }

  try {
    resetLoading.value = true;
    await resetPassword({ userPhone, newPassword, checkPassword });
    message.success('密码重置成功，请使用新密码登录');
    activeKey.value = 'phone';
    loginForm.value.userPhone = userPhone;
    loginForm.value.userPassword = newPassword;
    resetPasswordVisible.value = false;
  } catch (error) {
    // message handled by interceptor
  } finally {
    resetLoading.value = false;
  }
};
</script>

<style scoped>
.login-wrapper {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-container {
  display: flex;
  width: 100%;
  max-width: 1000px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

/* 左侧品牌区域 */
.login-brand {
  flex: 1;
  background: linear-gradient(180deg, #fafbfc 0%, #f5f7fa 100%);
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #f0f0f0;
}

.brand-content {
  text-align: center;
  width: 100%;
}

.brand-logo {
  margin-bottom: 24px;
}

.brand-title {
  font-size: 28px;
  font-weight: 600;
  color: #262626;
  margin: 16px 0 8px 0;
}

.brand-subtitle {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0 0 40px 0;
}

.brand-features {
  text-align: left;
}

.feature-item {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  font-size: 14px;
  color: #595959;
}

.feature-item :deep(.anticon) {
  color: #1890ff;
  margin-right: 12px;
  font-size: 16px;
}

/* 右侧登录表单区域 */
.login-form-wrapper {
  flex: 1;
  padding: 60px 40px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.form-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-title {
  font-size: 24px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 8px 0;
}

.form-subtitle {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0 0 32px 0;
}

/* 登录表单 */
.login-form {
  margin-bottom: 16px;
}

.login-form :deep(.ant-form-item) {
  margin-bottom: 16px;
}

.login-form :deep(.ant-input),
.login-form :deep(.ant-input-password) {
  border-radius: 4px;
}

/* 底部链接 */
.form-footer {
  text-align: center;
}

.form-footer :deep(.ant-btn-link) {
  color: #1890ff;
  font-size: 14px;
  padding: 0 8px;
}

/* 页脚 */
.login-footer {
  text-align: center;
  color: #8c8c8c;
  font-size: 12px;
}

.login-footer p {
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }

  .login-brand {
    padding: 40px 20px;
    border-right: none;
    border-bottom: 1px solid #f0f0f0;
  }

  .brand-title {
    font-size: 20px;
  }

  .login-form-wrapper {
    padding: 40px 20px;
  }

  .form-title {
    font-size: 20px;
  }
}
</style>
