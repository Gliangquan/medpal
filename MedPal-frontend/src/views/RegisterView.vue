<template>
  <a-layout class="register-layout">
    <a-layout-content>
      <div class="register-container">
        <a-card class="register-card">
          <h1>用户注册</h1>

          <!-- 注册表单 -->
          <a-form
            :model="registerForm"
            @finish="handleRegister"
          >
            <!-- 用户名 -->
            <a-form-item
              label="用户名"
              name="username"
              :rules="[{ required: true, message: '请输入用户名' }]"
            >
              <a-input
                v-model:value="registerForm.username"
                placeholder="请输入用户名"
              />
            </a-form-item>

            <!-- 手机号 -->
            <a-form-item
              label="手机号"
              name="phone"
              :rules="[
                { required: true, message: '请输入手机号' },
                { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号' }
              ]"
            >
              <a-input
                v-model:value="registerForm.phone"
                placeholder="请输入手机号"
              />
            </a-form-item>

            <!-- 密码 -->
            <a-form-item
              label="密码"
              name="password"
              :rules="[
                { required: true, message: '请输入密码' },
                { min: 6, message: '密码至少6位' }
              ]"
            >
              <a-input-password
                v-model:value="registerForm.password"
                placeholder="请输入密码"
              />
            </a-form-item>

            <!-- 确认密码 -->
            <a-form-item
              label="确认密码"
              name="confirmPassword"
              :rules="[
                { required: true, message: '请确认密码' },
                { validator: validateConfirmPassword }
              ]"
            >
              <a-input-password
                v-model:value="registerForm.confirmPassword"
                placeholder="请确认密码"
              />
            </a-form-item>

            <!-- 注册按钮 -->
            <a-form-item>
              <a-button
                type="primary"
                html-type="submit"
                block
                :loading="loading"
              >
                注册
              </a-button>
            </a-form-item>
          </a-form>

          <!-- 跳转登录链接 -->
          <div class="action-links">
            <span>已有账号？</span>
            <a-button type="link" @click="handleLogin">去登录</a-button>
          </div>
        </a-card>
      </div>
    </a-layout-content>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { message } from 'ant-design-vue';
import { userRegister } from '../api';
import type { UserRegisterRequest } from '../api';

const registerForm = reactive({
  username: '',
  phone: '',
  password: '',
  confirmPassword: '',
});

const loading = ref(false);

// 自定义验证：确认密码
const validateConfirmPassword = async (_rule: any, value: string) => {
  if (value !== registerForm.password) {
    return Promise.reject('两次输入的密码不一致');
  }
  return Promise.resolve();
};

// 注册方法
const handleRegister = async () => {
  try {
    loading.value = true;

    const params: UserRegisterRequest = {
      userAccount: registerForm.username,
      userPhone: registerForm.phone,
      userPassword: registerForm.password,
      checkPassword: registerForm.confirmPassword,
    };

    const response = await userRegister(params);
    if (response.code === 0) {
      message.success('注册成功');
      window.location.href = '/login';
    } else {
      message.error(response.message || '注册失败');
    }
  } catch (error) {
    message.error('注册失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 跳转登录页面
const handleLogin = () => {
  window.location.href = '/login';
};
</script>

<style scoped>
.register-layout {
  height: 100vh;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.register-container {
  max-width: 400px;
  width: 100%;
}

.register-card {
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.register-card h1 {
  text-align: center;
  margin-bottom: 24px;
  font-size: 24px;
  color: #1890ff;
}

.action-links {
  text-align: center;
  margin-top: 16px;
}
</style>