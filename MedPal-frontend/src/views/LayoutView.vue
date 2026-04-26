<template>
  <a-layout style="min-height: 100vh">
    <!-- 左侧侧边栏 -->
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      :width="220"
      :collapsed-width="80"
      class="sidebar"
    >
      <div class="logo">
        <div v-if="!collapsed" class="logo-content">
          <div class="logo-icon">🏥</div>
          <span class="logo-text">MedPal</span>
        </div>
        <div v-else class="logo-icon-collapsed">🏥</div>
      </div>

      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="inline"
        :inline-collapsed="collapsed"
        @click="handleMenuClick"
        class="sidebar-menu"
      >
        <!-- 首页 -->
        <a-menu-item key="/" class="menu-item">
          <template #icon>
            <home-outlined />
          </template>
          <span>首页</span>
        </a-menu-item>
        <a-menu-item key="/profile" class="menu-item">
          <template #icon>
            <user-outlined />
          </template>
          <span>个人中心</span>
        </a-menu-item>
        <a-menu-item v-if="!isAdmin" key="/chat" class="menu-item">
          <template #icon>
            <message-outlined />
          </template>
          <span>消息中心</span>
        </a-menu-item>

        <template v-if="isAdmin">
          <!-- 用户管理 -->
          <a-menu-item key="/admin/users" class="menu-item">
            <template #icon>
              <user-outlined />
            </template>
            <span>用户管理</span>
          </a-menu-item>

          <!-- 陪诊认证审核 -->
          <a-menu-item key="/admin/companion-certifications" class="menu-item">
            <template #icon>
              <team-outlined />
            </template>
            <span>陪诊认证审核</span>
          </a-menu-item>

          <!-- 医疗资源管理 -->
          <a-sub-menu key="/medical" class="menu-item">
            <template #icon>
              <shop-outlined />
            </template>
            <template #title>医疗资源</template>
            <a-menu-item key="/admin/hospitals">医院管理</a-menu-item>
            <a-menu-item key="/admin/departments">科室管理</a-menu-item>
            <a-menu-item key="/admin/doctors">医生管理</a-menu-item>
          </a-sub-menu>

          <!-- 订单管理 -->
          <a-menu-item key="/admin/orders" class="menu-item">
            <template #icon>
              <file-text-outlined />
            </template>
            <span>订单管理</span>
          </a-menu-item>

          <!-- 支付管理 -->
          <a-menu-item key="/admin/payments" class="menu-item">
            <template #icon>
              <dollar-outlined />
            </template>
            <span>支付管理</span>
          </a-menu-item>

          <!-- 评价管理 -->
          <a-menu-item key="/admin/evaluations" class="menu-item">
            <template #icon>
              <star-outlined />
            </template>
            <span>评价管理</span>
          </a-menu-item>

          <!-- 财务管理 -->
          <a-menu-item key="/admin/finance" class="menu-item">
            <template #icon>
              <dollar-outlined />
            </template>
            <span>财务管理</span>
          </a-menu-item>

          <!-- 统计分析 -->
          <a-menu-item key="/admin/statistics" class="menu-item">
            <template #icon>
              <bar-chart-outlined />
            </template>
            <span>统计分析</span>
          </a-menu-item>

          <!-- 内容管理 -->
          <a-menu-item key="/admin/content" class="menu-item">
            <template #icon>
              <file-outlined />
            </template>
            <span>内容管理</span>
          </a-menu-item>

          <!-- 培训课程 -->
          <a-menu-item key="/admin/training" class="menu-item">
            <template #icon>
              <file-outlined />
            </template>
            <span>培训课程</span>
          </a-menu-item>

          <!-- 系统设置 -->
          <a-menu-item key="/admin/settings" class="menu-item">
            <template #icon>
              <setting-outlined />
            </template>
            <span>系统设置</span>
          </a-menu-item>
        </template>
      </a-menu>
    </a-layout-sider>

    <!-- 右侧内容区域 -->
    <a-layout class="main-layout">
      <!-- 顶部导航栏 -->
      <a-layout-header class="header">
        <div class="header-left">
          <menu-unfold-outlined
            v-if="collapsed"
            class="trigger"
            @click="() => (collapsed = !collapsed)"
          />
          <menu-fold-outlined v-else class="trigger" @click="() => (collapsed = !collapsed)" />
        </div>

        <div class="header-right">
          <!-- 用户信息和下拉菜单 -->
          <a-dropdown>
            <template #overlay>
              <a-menu>
                <a-menu-item key="profile" @click="handleProfile">
                  <user-outlined />
                  <span>个人信息</span>
                </a-menu-item>
                <a-divider style="margin: 4px 0" />
                <a-menu-item key="logout" @click="handleLogout">
                  <logout-outlined />
                  <span>退出登录</span>
                </a-menu-item>
              </a-menu>
            </template>
            <div class="user-info">
              <a-avatar :size="36" :src="userAvatar" style="background-color: #1890ff">
                <template #icon>
                  <user-outlined />
                </template>
              </a-avatar>
              <span class="user-name">{{ userName }}</span>
              <down-outlined style="font-size: 12px; margin-left: 4px" />
            </div>
          </a-dropdown>
        </div>
      </a-layout-header>

      <!-- 内容区域 -->
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { message } from 'ant-design-vue';
import {
  UserOutlined,
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  LogoutOutlined,
  HomeOutlined,
  TeamOutlined,
  ShopOutlined,
  FileTextOutlined,
  DollarOutlined,
  BarChartOutlined,
  SettingOutlined,
  FileOutlined,
  DownOutlined,
  StarOutlined,
  MessageOutlined,
} from '@ant-design/icons-vue';

import { getLoginUser, userLogout } from '../api';

const router = useRouter();
const route = useRoute();

const selectedKeys = ref<string[]>([route.path]);
const collapsed = ref<boolean>(false);

const userAvatar = ref<string>('');
const userName = ref<string>('用户');
const isAdmin = ref<boolean>(false);

const fetchLoginUser = async () => {
  try {
    const response = await getLoginUser();
    if (response.code === 0) {
      userAvatar.value = response.data.userAvatar || '';
      userName.value = response.data.userName || '用户';
      isAdmin.value = response.data.userRole === 'admin';
      if (!isAdmin.value && route.path.startsWith('/admin')) {
        router.replace('/');
      }
    } else {
      message.error(response.message || '获取用户信息失败');
    }
  } catch (error) {
    message.error('获取用户信息失败');
  }
};

const handleMenuClick = ({ key }: { key: string }) => {
  if (key.startsWith('/admin') && !isAdmin.value) {
    message.warning('仅管理员可访问后台页面');
    return;
  }
  router.push(key);
};

const handleProfile = () => {
  router.push('/profile');
};

const handleLogout = async () => {
  try {
    const response = await userLogout();
    if (response.code === 0) {
      message.success('退出登录成功');
      localStorage.removeItem('user');
      window.location.href = '/login';
    } else {
      message.error(response.message || '退出登录失败');
    }
  } catch (error) {
    message.error('退出登录失败');
    localStorage.removeItem('user');
    window.location.href = '/login';
  }
};

onMounted(() => {
  fetchLoginUser();
});

watch(
  () => route.path,
  (path) => {
    selectedKeys.value = [path];
  },
  { immediate: true }
);
</script>

<style scoped>
:deep(.ant-layout) {
  background: #f5f7fa;
}

/* 侧边栏样式 */
.sidebar {
  background: linear-gradient(180deg, #1890ff 0%, #0050b3 100%);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 0 16px;
}

.logo-content {
  display: flex;
  align-items: center;
  gap: 8px;
  color: white;
  font-weight: bold;
  font-size: 18px;
}

.logo-icon {
  font-size: 24px;
}

.logo-icon-collapsed {
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sidebar-menu {
  background: transparent;
  border: none;
}

:deep(.sidebar-menu .ant-menu-item) {
  color: rgba(255, 255, 255, 0.85);
  background: transparent;
  margin: 4px 8px;
  border-radius: 4px;
}

:deep(.sidebar-menu .ant-menu-item:hover) {
  color: white;
  background: rgba(255, 255, 255, 0.1);
}

:deep(.sidebar-menu .ant-menu-item-selected) {
  color: white;
  background: rgba(255, 255, 255, 0.2);
}

:deep(.sidebar-menu .ant-menu-submenu-title) {
  color: rgba(255, 255, 255, 0.85);
  background: transparent;
  margin: 4px 8px;
  border-radius: 4px;
}

:deep(.sidebar-menu .ant-menu-submenu-title:hover) {
  color: white;
  background: rgba(255, 255, 255, 0.1);
}

:deep(.sidebar-menu .ant-menu-submenu-selected > .ant-menu-submenu-title) {
  color: white;
  background: rgba(255, 255, 255, 0.2);
}

:deep(.sidebar-menu .ant-menu-item-icon) {
  color: rgba(255, 255, 255, 0.85);
  font-size: 16px;
}

:deep(.sidebar-menu .ant-menu-submenu-title-icon) {
  color: rgba(255, 255, 255, 0.85);
}

/* 顶部栏样式 */
.header {
  background: white;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.trigger {
  font-size: 18px;
  line-height: 64px;
  cursor: pointer;
  transition: color 0.3s;
  color: #262626;
}

.trigger:hover {
  color: #1890ff;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.user-name {
  color: #262626;
  font-size: 14px;
  font-weight: 500;
}

/* 内容区域 */
.main-layout {
  background: #f5f7fa;
}

.content {
  background: #f5f7fa;
  min-height: calc(100vh - 64px);
  padding: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .logo-content {
    font-size: 16px;
  }

  .user-name {
    display: none;
  }

  .header {
    padding: 0 16px;
  }
}
</style>
