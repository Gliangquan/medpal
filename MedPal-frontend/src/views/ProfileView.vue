<template>
  <div class="profile-container">
    <a-page-header
      title="个人中心"
      sub-title="管理您的个人信息"
      @back="() => $router.back()"
    />

    <a-row :gutter="24" style="margin-top: 24px">
      <!-- 左侧：个人信息卡片 -->
      <a-col :xs="24" :sm="24" :md="8">
        <a-card :bordered="false" class="profile-card">
          <div class="avatar-section">
            <a-avatar
              :size="120"
              :src="userInfo.userAvatar"
              style="background-color: #1890ff"
            >
              <template #icon>
                <user-outlined />
              </template>
            </a-avatar>
            <div class="user-basic">
              <h2>{{ userInfo.userName }}</h2>
              <p class="role-tag">
                <a-tag :color="getRoleColor(userInfo.userRole)">
                  {{ getRoleLabel(userInfo.userRole) }}
                </a-tag>
              </p>
              <p class="account">@{{ userInfo.userAccount }}</p>
            </div>
          </div>

          <a-divider />

          <div class="info-section">
            <div class="info-item">
              <span class="label">手机号</span>
              <span class="value">{{ userInfo.userPhone || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">邮箱</span>
              <span class="value">{{ userInfo.userEmail || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">注册时间</span>
              <span class="value">{{ formatDate(userInfo.createTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">最后更新</span>
              <span class="value">{{ formatDate(userInfo.updateTime) }}</span>
            </div>
          </div>

          <a-button type="primary" block style="margin-top: 20px" @click="showEditModal">
            编辑信息
          </a-button>
        </a-card>
      </a-col>

      <!-- 右侧：详细信息 -->
      <a-col :xs="24" :sm="24" :md="16">
        <a-card :bordered="false" title="详细信息" class="details-card">
          <a-descriptions :column="2" bordered>
            <a-descriptions-item label="账号">
              {{ userInfo.userAccount }}
            </a-descriptions-item>
            <a-descriptions-item label="昵称">
              {{ userInfo.userName }}
            </a-descriptions-item>
            <a-descriptions-item label="手机号">
              {{ userInfo.userPhone || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="邮箱">
              {{ userInfo.userEmail || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="角色">
              <a-tag :color="getRoleColor(userInfo.userRole)">
                {{ getRoleLabel(userInfo.userRole) }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="简介" :span="2">
              {{ userInfo.userProfile || '-' }}
            </a-descriptions-item>
          </a-descriptions>

          <!-- 陪诊员特定信息 -->
          <template v-if="userInfo.userRole === 'companion'">
            <a-divider />
            <h3>陪诊员信息</h3>
            <a-descriptions :column="2" bordered>
              <a-descriptions-item label="评分">
                <a-rate :value="userInfo.rating" disabled allow-half />
                <span style="margin-left: 10px">{{ userInfo.rating || 0 }} 分</span>
              </a-descriptions-item>
              <a-descriptions-item label="服务次数">
                {{ userInfo.serviceCount || 0 }} 次
              </a-descriptions-item>
              <a-descriptions-item label="总收入">
                ¥{{ userInfo.totalIncome || 0 }}
              </a-descriptions-item>
              <a-descriptions-item label="工作年限">
                {{ userInfo.workYears || '-' }} 年
              </a-descriptions-item>
              <a-descriptions-item label="专长" :span="2">
                {{ userInfo.specialties || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="服务区域" :span="2">
                {{ userInfo.serviceArea || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="实名认证状态">
                <a-tag :color="getStatusColor(userInfo.realNameStatus)">
                  {{ getStatusLabel(userInfo.realNameStatus) }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="资质认证状态">
                <a-tag :color="getStatusColor(userInfo.qualificationStatus)">
                  {{ getStatusLabel(userInfo.qualificationStatus) }}
                </a-tag>
              </a-descriptions-item>
            </a-descriptions>
          </template>

          <!-- 患者特定信息 -->
          <template v-if="userInfo.userRole === 'user' || userInfo.userRole === 'patient'">
            <a-divider />
            <h3>患者信息</h3>
            <a-descriptions :column="2" bordered>
              <a-descriptions-item label="年龄">
                {{ userInfo.age || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="性别">
                {{ userInfo.gender === 'male' ? '男' : userInfo.gender === 'female' ? '女' : '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="病史" :span="2">
                {{ userInfo.medicalHistory || '-' }}
              </a-descriptions-item>
            </a-descriptions>
          </template>
        </a-card>
      </a-col>
    </a-row>

    <!-- 编辑信息弹窗 -->
    <a-modal
      v-model:visible="isEditModalVisible"
      title="编辑个人信息"
      width="600px"
      @ok="handleEdit"
    >
      <a-form :model="editForm" layout="vertical">
        <a-form-item label="昵称" required>
          <a-input v-model:value="editForm.userName" placeholder="请输入昵称" />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model:value="editForm.userPhone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="邮箱">
          <a-input v-model:value="editForm.userEmail" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item label="头像">
          <a-input v-model:value="editForm.userAvatar" placeholder="请输入头像URL" />
        </a-form-item>
        <a-form-item label="简介">
          <a-textarea v-model:value="editForm.userProfile" placeholder="请输入个人简介" :rows="4" />
        </a-form-item>

        <!-- 陪诊员特定字段 -->
        <template v-if="userInfo.userRole === 'companion'">
          <a-form-item label="工作年限">
            <a-input-number v-model:value="editForm.workYears" placeholder="请输入工作年限" />
          </a-form-item>
          <a-form-item label="专长">
            <a-textarea v-model:value="editForm.specialties" placeholder="请输入专长" :rows="3" />
          </a-form-item>
          <a-form-item label="服务区域">
            <a-input v-model:value="editForm.serviceArea" placeholder="请输入服务区域" />
          </a-form-item>
        </template>

        <!-- 患者特定字段 -->
        <template v-if="userInfo.userRole === 'user' || userInfo.userRole === 'patient'">
          <a-form-item label="年龄">
            <a-input-number v-model:value="editForm.age" placeholder="请输入年龄" />
          </a-form-item>
          <a-form-item label="性别">
            <a-select v-model:value="editForm.gender" placeholder="请选择性别">
              <a-select-option value="male">男</a-select-option>
              <a-select-option value="female">女</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="病史">
            <a-textarea v-model:value="editForm.medicalHistory" placeholder="请输入病史" :rows="3" />
          </a-form-item>
        </template>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { UserOutlined } from '@ant-design/icons-vue';
import { getLoginUser, updateMyUser } from '../api';
import dayjs from 'dayjs';

const userInfo = ref({
  id: null,
  userAccount: '',
  userName: '',
  userRole: '',
  userPhone: '',
  userEmail: '',
  userAvatar: '',
  userProfile: '',
  createTime: '',
  updateTime: '',
  // 陪诊员字段
  rating: 0,
  serviceCount: 0,
  totalIncome: 0,
  workYears: null,
  specialties: '',
  serviceArea: '',
  realNameStatus: '',
  qualificationStatus: '',
  // 患者字段
  age: null,
  gender: '',
  medicalHistory: '',
});

const isEditModalVisible = ref(false);

const editForm = reactive({
  userName: '',
  userPhone: '',
  userEmail: '',
  userAvatar: '',
  userProfile: '',
  workYears: null,
  specialties: '',
  serviceArea: '',
  age: null,
  gender: '',
  medicalHistory: '',
});

const getRoleLabel = (role: string) => {
  const map: Record<string, string> = {
    'user': '患者',
    'patient': '患者',
    'companion': '陪诊员',
    'admin': '管理员',
  };
  return map[role] || role;
};

const getRoleColor = (role: string) => {
  const map: Record<string, string> = {
    'user': 'blue',
    'patient': 'blue',
    'companion': 'orange',
    'admin': 'red',
  };
  return map[role] || 'default';
};

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '已拒绝',
  };
  return map[status] || status;
};

const getStatusColor = (status: string) => {
  const map: Record<string, string> = {
    'pending': 'orange',
    'approved': 'green',
    'rejected': 'red',
  };
  return map[status] || 'default';
};

const formatDate = (date: string) => {
  if (!date) return '-';
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss');
};

const fetchUserInfo = async () => {
  try {
    const res = await getLoginUser();
    if (res.code === 0) {
      userInfo.value = res.data;
      Object.assign(editForm, res.data);
    } else {
      message.error(res.message || '获取个人信息失败');
    }
  } catch (error) {
    message.error('获取个人信息失败');
  }
};

const showEditModal = () => {
  isEditModalVisible.value = true;
};

const handleEdit = async () => {
  try {
    const res = await updateMyUser(editForm);
    if (res.code === 0) {
      message.success('更新成功');
      isEditModalVisible.value = false;
      fetchUserInfo();
    } else {
      message.error(res.message || '更新失败');
    }
  } catch (error) {
    message.error('更新失败');
  }
};

onMounted(() => {
  fetchUserInfo();
});
</script>

<style scoped>
.profile-container {
  padding: 24px;
}

.profile-card {
  text-align: center;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.user-basic {
  width: 100%;
}

.user-basic h2 {
  margin: 0;
  font-size: 20px;
  color: #262626;
}

.role-tag {
  margin: 8px 0;
}

.account {
  margin: 0;
  color: #8c8c8c;
  font-size: 14px;
}

.info-section {
  text-align: left;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  color: #8c8c8c;
  font-weight: 500;
}

.info-item .value {
  color: #262626;
  font-weight: 500;
}

.details-card {
  height: 100%;
}

:deep(.ant-descriptions-item-label) {
  font-weight: 500;
}

h3 {
  margin-top: 16px;
  margin-bottom: 16px;
  color: #262626;
  font-size: 16px;
}

@media (max-width: 768px) {
  .profile-container {
    padding: 12px;
  }

  .avatar-section {
    flex-direction: row;
    text-align: left;
  }

  .user-basic {
    text-align: left;
  }
}
</style>
