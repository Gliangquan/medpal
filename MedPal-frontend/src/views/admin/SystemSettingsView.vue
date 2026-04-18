<template>
  <div class="page-container">
    <a-page-header
      title="系统设置"
      sub-title="管理平台系统配置"
      @back="() => $router.back()"
    />

    <a-card :bordered="false">
      <a-tabs v-model:activeKey="activeTab">
        <!-- 基础信息设置 -->
        <a-tab-pane key="basic" tab="基础信息">
          <a-form :model="basicSettings" layout="vertical" style="max-width: 600px">
            <a-form-item label="平台名称">
              <a-input v-model:value="basicSettings.platformName" />
            </a-form-item>
            <a-form-item label="平台简介">
              <a-textarea v-model:value="basicSettings.platformDesc" :rows="4" />
            </a-form-item>
            <a-form-item label="服务热线">
              <a-input v-model:value="basicSettings.hotline" />
            </a-form-item>
            <a-form-item label="客服邮箱">
              <a-input v-model:value="basicSettings.email" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="handleSaveBasic">保存设置</a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <!-- 价格配置 -->
        <a-tab-pane key="pricing" tab="价格配置">
          <a-form :model="pricingSettings" layout="vertical" style="max-width: 600px">
            <a-form-item label="2小时陪诊费用（元）">
              <a-input-number v-model:value="pricingSettings.price2h" :min="0" />
            </a-form-item>
            <a-form-item label="半天陪诊费用（元）">
              <a-input-number v-model:value="pricingSettings.price4h" :min="0" />
            </a-form-item>
            <a-form-item label="全天陪诊费用（元）">
              <a-input-number v-model:value="pricingSettings.price8h" :min="0" />
            </a-form-item>
            <a-form-item label="平台手续费比例（%）">
              <a-input-number v-model:value="pricingSettings.platformFeeRate" :min="0" :max="100" />
            </a-form-item>
            <a-form-item label="结算周期（天）">
              <a-input-number v-model:value="pricingSettings.settlementCycle" :min="1" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="handleSavePricing">保存设置</a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <!-- 功能开关 -->
        <a-tab-pane key="features" tab="功能开关">
          <a-form :model="featureSettings" layout="vertical" style="max-width: 600px">
            <a-form-item label="紧急求助功能">
              <a-switch v-model:checked="featureSettings.emergencyHelp" />
            </a-form-item>
            <a-form-item label="服务推荐功能">
              <a-switch v-model:checked="featureSettings.serviceRecommendation" />
            </a-form-item>
            <a-form-item label="科普推送功能">
              <a-switch v-model:checked="featureSettings.sciencePush" />
            </a-form-item>
            <a-form-item label="用户评价功能">
              <a-switch v-model:checked="featureSettings.userReview" />
            </a-form-item>
            <a-form-item label="陪诊员社区功能">
              <a-switch v-model:checked="featureSettings.companionCommunity" />
            </a-form-item>
            <a-form-item label="在线支付功能">
              <a-switch v-model:checked="featureSettings.onlinePayment" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="handleSaveFeatures">保存设置</a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <!-- 权限管理 -->
        <a-tab-pane key="permissions" tab="权限管理">
          <a-row :gutter="16" style="margin-bottom: 16px">
            <a-col :xs="24" :sm="12" :md="6">
              <a-button type="primary" @click="showAddAdminModal">新增管理员</a-button>
            </a-col>
          </a-row>

          <a-table
            :columns="adminColumns"
            :data-source="adminData"
            :loading="loading"
            :pagination="adminPagination"
            @change="handleAdminTableChange"
            :scroll="{ x: 1000 }"
            :row-key="record => record.id"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'role'">
                <a-tag color="blue">{{ record.role }}</a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="showEditAdminModal(record)">
                    编辑
                  </a-button>
                  <a-popconfirm
                    title="确定删除该管理员吗？"
                    ok-text="确定"
                    cancel-text="取消"
                    @confirm="handleDeleteAdmin(record)"
                  >
                    <a-button type="link" size="small" danger>删除</a-button>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 管理员编辑模态框 -->
    <a-modal
      v-model:visible="adminModalVisible"
      :title="isEditAdmin ? '编辑管理员' : '新增管理员'"
      @ok="handleAdminSubmit"
      width="600px"
    >
      <a-form :model="adminForm" layout="vertical">
        <a-form-item label="管理员名称" required>
          <a-input v-model:value="adminForm.name" placeholder="请输入管理员名称" />
        </a-form-item>
        <a-form-item label="登录账号" required>
          <a-input v-model:value="adminForm.username" placeholder="请输入登录账号" />
        </a-form-item>
        <a-form-item label="初始密码" v-if="!isEditAdmin" required>
          <a-input-password v-model:value="adminForm.password" placeholder="请输入初始密码（至少6位）" />
        </a-form-item>
        <a-form-item label="角色" required>
          <a-select v-model:value="adminForm.role" placeholder="请选择角色" disabled>
            <a-select-option value="admin">管理员</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { addUser, deleteUser, listUserVOByPage, updateUser } from '../../api';

const adminColumns = [
  { title: '管理员名称', dataIndex: 'name', key: 'name', width: 120 },
  { title: '登录账号', dataIndex: 'username', key: 'username', width: 150 },
  { title: '角色', dataIndex: 'role', key: 'role', width: 120 },
  { title: '创建时间', dataIndex: 'createdTime', key: 'createdTime', width: 180 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' },
];

const activeTab = ref('basic');
const loading = ref(false);
const adminModalVisible = ref(false);
const isEditAdmin = ref(false);
const adminData = ref([]);

const adminPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  pageSizeOptions: ['10', '20', '50', '100'],
});

const basicSettings = reactive({
  platformName: 'MedPal医疗陪伴服务平台',
  platformDesc: '专业的医疗陪伴服务平台，为患者提供优质的陪诊服务',
  hotline: '400-800-8888',
  email: 'service@medpal.com',
});

const pricingSettings = reactive({
  price2h: 200,
  price4h: 400,
  price8h: 800,
  platformFeeRate: 10,
  settlementCycle: 7,
});

const featureSettings = reactive({
  emergencyHelp: true,
  serviceRecommendation: true,
  sciencePush: true,
  userReview: true,
  companionCommunity: true,
  onlinePayment: true,
});

const adminForm = reactive({
  id: null,
  name: '',
  username: '',
  password: '12345678',
  role: 'admin',
});

const handleSaveBasic = () => {
  message.success('基础信息保存成功');
};

const handleSavePricing = () => {
  message.success('价格配置保存成功');
};

const handleSaveFeatures = () => {
  message.success('功能开关保存成功');
};

const fetchAdminData = async () => {
  loading.value = true;
  try {
    const response = await listUserVOByPage({
      current: adminPagination.current,
      pageSize: adminPagination.pageSize,
      userRole: 'admin'
    });
    if (response.code === 0) {
      adminData.value = (response.data.records || []).map((item: any) => ({
        id: item.id,
        name: item.userName || item.userAccount,
        username: item.userAccount,
        role: '管理员',
        createdTime: item.createTime || '-'
      }));
      adminPagination.total = response.data.total || 0;
    }
  } catch (error) {
    message.error('获取数据失败');
  } finally {
    loading.value = false;
  }
};

const showAddAdminModal = () => {
  isEditAdmin.value = false;
  adminForm.id = null;
  adminForm.name = '';
  adminForm.username = '';
  adminForm.password = '12345678';
  adminForm.role = 'admin';
  adminModalVisible.value = true;
};

const showEditAdminModal = (record: any) => {
  isEditAdmin.value = true;
  adminForm.id = record.id;
  adminForm.name = record.name;
  adminForm.username = record.username;
  adminForm.password = '12345678';
  adminForm.role = 'admin';
  adminModalVisible.value = true;
};

const handleAdminSubmit = async () => {
  if (!adminForm.name || !adminForm.username) {
    message.warning('请填写管理员名称和登录账号');
    return;
  }
  if (!isEditAdmin.value && (!adminForm.password || adminForm.password.length < 6)) {
    message.warning('请填写至少 6 位初始密码');
    return;
  }
  try {
    if (isEditAdmin.value && adminForm.id) {
      const response = await updateUser({
        id: adminForm.id,
        userName: adminForm.name,
        userRole: 'admin'
      });
      if (response.code === 0) {
        message.success('编辑成功');
      }
    } else {
      const response = await addUser({
        userName: adminForm.name,
        userAccount: adminForm.username,
        userPassword: adminForm.password,
        userRole: 'admin'
      });
      if (response.code === 0) {
        message.success('新增成功');
      }
    }
    adminModalVisible.value = false;
    fetchAdminData();
  } catch (error) {
    message.error('操作失败');
  }
};

const handleDeleteAdmin = async (record: any) => {
  try {
    const response = await deleteUser({ id: record.id });
    if (response.code === 0) {
      message.success(`删除管理员 ${record.name} 成功`);
      fetchAdminData();
    }
  } catch (error) {
    message.error('删除失败');
  }
};

const handleAdminTableChange = (pag: any) => {
  adminPagination.current = pag.current;
  adminPagination.pageSize = pag.pageSize;
};

onMounted(() => {
  fetchAdminData();
});
</script>

<style scoped>
.page-container {
  padding: 24px;
}
</style>
