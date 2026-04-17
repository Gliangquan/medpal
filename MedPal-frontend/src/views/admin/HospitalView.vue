<template>
  <div class="page-container">
    <a-page-header
      title="医院管理"
      sub-title="管理平台合作医院信息"
      @back="() => $router.back()"
    />

    <a-card :bordered="false" style="margin-top: 24px">
      <!-- 搜索和操作 -->
      <a-row :gutter="16" style="margin-bottom: 24px">
        <a-col :xs="24" :sm="12" :md="6">
          <a-input-search
            v-model:value="searchText"
            placeholder="搜索医院名称"
            @search="handleSearch"
            allow-clear
          />
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-button type="primary" @click="showAddModal">新增医院</a-button>
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
          <template v-if="column.key === 'hospitalLevel'">
            <a-tag color="blue">{{ record.hospitalLevel || record.level || '-' }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="showEditModal(record)">编辑</a-button>
              <a-popconfirm
                title="确定删除该医院吗？"
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

    <!-- 编辑模态框 -->
    <a-modal
      v-model:visible="modalVisible"
      :title="isEdit ? '编辑医院' : '新增医院'"
      @ok="handleSubmit"
      width="600px"
    >
      <a-form :model="formData" layout="vertical">
        <a-form-item label="医院名称" required>
          <a-input v-model:value="formData.hospitalName" placeholder="请输入医院名称" />
        </a-form-item>
        <a-form-item label="医院等级">
          <a-input v-model:value="formData.hospitalLevel" placeholder="请输入医院等级" />
        </a-form-item>
        <a-form-item label="地址">
          <a-input v-model:value="formData.address" placeholder="请输入医院地址" />
        </a-form-item>
        <a-form-item label="联系电话">
          <a-input v-model:value="formData.phone" placeholder="请输入联系电话" />
        </a-form-item>
        <a-form-item label="医院描述">
          <a-textarea v-model:value="formData.introduction" placeholder="请输入医院描述" :rows="4" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { listHospitals, searchHospitals, createHospital, updateHospital, deleteHospital } from '../../api';

const columns = [
  { title: '医院名称', dataIndex: 'hospitalName', key: 'hospitalName', width: 200 },
  { title: '医院等级', dataIndex: 'hospitalLevel', key: 'hospitalLevel', width: 120 },
  { title: '地址', dataIndex: 'address', key: 'address', width: 250 },
  { title: '联系电话', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '简介', dataIndex: 'introduction', key: 'introduction', width: 260, ellipsis: true },
  { title: '操作', key: 'action', width: 120, fixed: 'right' },
];

const tableData = ref([]);
const loading = ref(false);
const searchText = ref('');
const modalVisible = ref(false);
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
  hospitalName: '',
  hospitalLevel: '',
  address: '',
  phone: '',
  introduction: '',
});

const handleSearch = () => {
  pagination.current = 1;
  fetchData();
};

const fetchData = async () => {
  loading.value = true;
  try {
    const keyword = searchText.value.trim();
    if (keyword) {
      const response = await searchHospitals(keyword);
      if (response.code === 0) {
        tableData.value = (response.data || []).map((item: any) => ({
          ...item,
          hospitalLevel: item.hospitalLevel || item.level || '',
          introduction: item.introduction || item.description || ''
        }));
        pagination.total = tableData.value.length;
      } else {
        message.error(response.message || '获取数据失败');
      }
      return;
    }

    const response = await listHospitals(pagination.current, pagination.pageSize);
    if (response.code === 0) {
      tableData.value = (response.data.records || []).map((item: any) => ({
        ...item,
        hospitalLevel: item.hospitalLevel || item.level || '',
        introduction: item.introduction || item.description || ''
      }));
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

const showAddModal = () => {
  isEdit.value = false;
  formData.id = null;
  formData.hospitalName = '';
  formData.hospitalLevel = '';
  formData.address = '';
  formData.phone = '';
  formData.introduction = '';
  modalVisible.value = true;
};

const showEditModal = (record: any) => {
  isEdit.value = true;
  Object.assign(formData, record);
  modalVisible.value = true;
};

const handleSubmit = async () => {
  try {
    let response;
    if (isEdit.value) {
      response = await updateHospital(formData);
    } else {
      response = await createHospital(formData);
    }
    
    if (response.code === 0) {
      message.success(isEdit.value ? '编辑成功' : '新增成功');
      modalVisible.value = false;
      fetchData();
    } else {
      message.error(response.message || '操作失败');
    }
  } catch (error) {
    message.error('操作失败');
  }
};

const handleDelete = async (record: any) => {
  try {
    const response = await deleteHospital(record.id);
    if (response.code === 0) {
      message.success(`删除医院 ${record.hospitalName} 成功`);
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
