<template>
  <div class="page-container">
    <a-page-header
      title="用户管理"
      sub-title="管理平台所有注册用户"
      @back="() => $router.back()"
    />

    <a-card :bordered="false" style="margin-top: 24px">
      <!-- 搜索和筛选 -->
      <a-row :gutter="16" style="margin-bottom: 24px">
        <a-col :xs="24" :sm="12" :md="6">
          <a-input-search
            v-model:value="searchText"
            placeholder="搜索昵称 / 账号 / 手机号"
            @search="handleSearch"
            allow-clear
          />
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-button type="primary" @click="showAddModal">新增用户</a-button>
        </a-col>
      </a-row>

      <!-- 表格 -->
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        :scroll="{ x: 1200 }"
        :row-key="record => record.id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="showDetailModal(record)">
                详情
              </a-button>
              <a-button type="link" size="small" @click="showEditModal(record)">
                编辑
              </a-button>
              <a-popconfirm
                title="确定删除该用户吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record)"
              >
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 详情模态框 -->
    <a-modal
      v-model:visible="detailModalVisible"
      title="用户详情"
      :footer="null"
      width="700px"
    >
      <a-descriptions :column="2" bordered v-if="selectedUser">
        <a-descriptions-item label="用户名">
          {{ selectedUser.userAccount }}
        </a-descriptions-item>
        <a-descriptions-item label="昵称">
          {{ selectedUser.userName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="手机号">
          {{ selectedUser.userPhone || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="角色">
          {{ selectedUser.userRole }}
        </a-descriptions-item>
        <a-descriptions-item label="注册时间" :span="2">
          {{ selectedUser.createTime }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 编辑模态框 -->
    <a-modal
      v-model:visible="editModalVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      @ok="handleSubmit"
      width="600px"
    >
      <a-form :model="formData" layout="vertical">
        <a-form-item label="账号" required>
          <a-input v-model:value="formData.userAccount" placeholder="请输入账号" :disabled="isEdit" />
        </a-form-item>
        <a-form-item label="昵称">
          <a-input v-model:value="formData.userName" placeholder="请输入昵称" />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model:value="formData.userPhone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="角色" required>
          <a-select v-model:value="formData.userRole" placeholder="请选择角色">
            <a-select-option value="user">用户</a-select-option>
            <a-select-option value="admin">管理员</a-select-option>
            <a-select-option value="companion">陪诊员</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { listUserVOByPage, deleteUser, addUser, updateUser } from '../../api';

const columns = [
  { title: '用户ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '昵称', dataIndex: 'userName', key: 'userName', width: 120 },
  { title: '角色', dataIndex: 'userRole', key: 'userRole', width: 100 },
  { title: '注册时间', dataIndex: 'createTime', key: 'createTime', width: 150 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' },
];

const tableData = ref([]);
const loading = ref(false);
const searchText = ref('');
const detailModalVisible = ref(false);
const editModalVisible = ref(false);
const selectedUser = ref(null);
const isEdit = ref(false);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  pageSizeOptions: ['10', '20', '50', '100'],
});

const formData = reactive({
  id: null,
  userAccount: '',
  userName: '',
  userPhone: '',
  userRole: 'user',
});

const handleSearch = () => {
  pagination.current = 1;
  fetchData();
};

const fetchData = async () => {
  loading.value = true;
  try {
    const keyword = searchText.value?.trim();
    const response = await listUserVOByPage({
      current: pagination.current,
      pageSize: pagination.pageSize,
      keyword: keyword || undefined,
    });
    
    if (response.code === 0) {
      tableData.value = response.data.records || [];
      pagination.total = response.data.total || 0;
    } else {
      message.error(response.message || '获取数据失败');
    }
  } catch (error) {
    message.error('获取数据失败');
  } finally {
    loading.value = false;
  }
};

const showDetailModal = (record: any) => {
  selectedUser.value = record;
  detailModalVisible.value = true;
};

const showAddModal = () => {
  isEdit.value = false;
  formData.id = null;
  formData.userAccount = '';
  formData.userName = '';
  formData.userPhone = '';
  formData.userRole = 'user';
  editModalVisible.value = true;
};

const showEditModal = (record: any) => {
  isEdit.value = true;
  Object.assign(formData, record);
  editModalVisible.value = true;
};

const handleSubmit = async () => {
  try {
    if (isEdit.value) {
      const response = await updateUser({
        id: formData.id,
        userName: formData.userName,
        userPhone: formData.userPhone,
        userRole: formData.userRole,
      });
      if (response.code === 0) {
        message.success('编辑成功');
      } else {
        message.error(response.message || '编辑失败');
      }
    } else {
      const response = await addUser({
        userAccount: formData.userAccount,
        userName: formData.userName,
        userPhone: formData.userPhone,
        userRole: formData.userRole,
      });
      if (response.code === 0) {
        message.success('新增成功');
      } else {
        message.error(response.message || '新增失败');
      }
    }
    editModalVisible.value = false;
    fetchData();
  } catch (error) {
    message.error('操作失败');
  }
};

const handleDelete = async (record: any) => {
  try {
    const response = await deleteUser({ id: record.id });
    if (response.code === 0) {
      message.success(`删除用户 ${record.userAccount} 成功`);
      fetchData();
    } else {
      message.error(response.message || '删除失败');
    }
  } catch (error) {
    message.error('删除失败');
  }
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchData();
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.page-container {
  padding: 24px;
}
</style>
