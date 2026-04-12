<template>
  <div class="page-container">
    <a-page-header
      title="科室管理"
      sub-title="管理医院科室信息"
      @back="() => $router.back()"
    />

    <a-card :bordered="false" style="margin-top: 24px">
      <!-- 搜索和筛选 -->
      <a-row :gutter="16" style="margin-bottom: 24px">
        <a-col :xs="24" :sm="12" :md="6">
          <a-input-search
            v-model:value="searchText"
            placeholder="搜索科室名称"
            @search="handleSearch"
            allow-clear
          />
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-button type="primary" @click="showAddModal">新增科室</a-button>
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
          <template v-if="column.key === 'hospitalName'">
            {{ getHospitalName(record.hospitalId) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="showEditModal(record)">编辑</a-button>
              <a-popconfirm
                title="确定删除该科室吗？"
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
      :title="isEdit ? '编辑科室' : '新增科室'"
      @ok="handleSubmit"
      width="600px"
    >
      <a-form :model="formData" layout="vertical">
        <a-form-item label="医院" required>
          <a-select
            v-model:value="formData.hospitalId"
            placeholder="请选择医院"
            :loading="hospitalsLoading"
          >
            <a-select-option
              v-for="hospital in hospitals"
              :key="hospital.id"
              :value="hospital.id"
            >
              {{ hospital.hospitalName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="科室名称" required>
          <a-input v-model:value="formData.departmentName" placeholder="请输入科室名称" />
        </a-form-item>
        <a-form-item label="科室描述">
          <a-textarea v-model:value="formData.description" placeholder="请输入科室描述" :rows="4" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { listDepartments, createDepartment, updateDepartment, deleteDepartment, listHospitals } from '../../api';

const columns = [
  { title: '科室名称', dataIndex: 'departmentName', key: 'departmentName', width: 150 },
  { title: '科室代码', dataIndex: 'departmentCode', key: 'departmentCode', width: 150 },
  { title: '医院', dataIndex: 'hospitalId', key: 'hospitalName', width: 150 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' },
];

const tableData = ref([]);
const loading = ref(false);
const searchText = ref('');
const modalVisible = ref(false);
const isEdit = ref(false);
const hospitals = ref([]);
const hospitalsLoading = ref(false);

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
  departmentName: '',
  hospitalId: null,
  description: '',
});

const getHospitalName = (hospitalId: any) => {
  const hospital = hospitals.value.find(h => h.id === hospitalId || h.id === parseInt(hospitalId));
  return hospital ? hospital.hospitalName : '-';
};

const fetchHospitals = async () => {
  hospitalsLoading.value = true;
  try {
    const response = await listHospitals(1, 1000);
    if (response.code === 0) {
      hospitals.value = response.data.records || [];
    }
  } catch (error) {
    console.error('获取医院列表失败', error);
  } finally {
    hospitalsLoading.value = false;
  }
};

const handleSearch = () => {
  pagination.current = 1;
  fetchData();
};

const fetchData = async () => {
  loading.value = true;
  try {
    const response = await listDepartments(
      pagination.current,
      pagination.pageSize,
      undefined,
      searchText.value.trim() || undefined,
    );

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

const showAddModal = () => {
  isEdit.value = false;
  formData.id = null;
  formData.departmentName = '';
  formData.hospitalId = null;
  formData.description = '';
  modalVisible.value = true;
};

const showEditModal = (record: any) => {
  isEdit.value = true;
  Object.assign(formData, record);
  modalVisible.value = true;
};

const handleSubmit = async () => {
  if (!formData.hospitalId) {
    message.error('请选择医院');
    return;
  }
  if (!formData.departmentName) {
    message.error('请输入科室名称');
    return;
  }

  try {
    let response;
    if (isEdit.value) {
      response = await updateDepartment(formData);
    } else {
      response = await createDepartment(formData);
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
    const response = await deleteDepartment(record.id);
    if (response.code === 0) {
      message.success(`删除科室 ${record.departmentName} 成功`);
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
  fetchHospitals();
  fetchData();
});
</script>

<style scoped>
.page-container {
  padding: 24px;
}
</style>
