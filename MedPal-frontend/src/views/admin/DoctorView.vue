<template>
  <div class="page-container">
    <a-page-header
      title="医生管理"
      sub-title="管理医院医生信息"
      @back="() => $router.back()"
    />

    <a-card :bordered="false" style="margin-top: 24px">
      <!-- 搜索和筛选 -->
      <a-row :gutter="16" style="margin-bottom: 24px">
        <a-col :xs="24" :sm="12" :md="6">
          <a-input-search
            v-model:value="searchText"
            placeholder="搜索医生名称"
            @search="handleSearch"
            allow-clear
          />
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-button type="primary" @click="showAddModal">新增医生</a-button>
        </a-col>
      </a-row>

      <!-- 表格 -->
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        :scroll="{ x: 1500 }"
        :row-key="record => record.id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'hospitalName'">
            {{ getHospitalName(record.hospitalId) }}
          </template>
          <template v-else-if="column.key === 'departmentName'">
            {{ getDepartmentName(record.departmentId) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="showEditModal(record)">编辑</a-button>
              <a-popconfirm
                title="确定删除该医生吗？"
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
      :title="isEdit ? '编辑医生' : '新增医生'"
      @ok="handleSubmit"
      width="700px"
    >
      <a-form :model="formData" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="医院" required>
              <a-select
                v-model:value="formData.hospitalId"
                placeholder="请选择医院"
                :loading="hospitalsLoading"
                @change="handleHospitalChange"
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
          </a-col>
          <a-col :span="12">
            <a-form-item label="科室" required>
              <a-select
                v-model:value="formData.departmentId"
                placeholder="请选择科室"
                :loading="departmentsLoading"
              >
                <a-select-option
                  v-for="dept in filteredDepartments"
                  :key="dept.id"
                  :value="dept.id"
                >
                  {{ dept.departmentName }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="医生姓名" required>
          <a-input v-model:value="formData.doctorName" placeholder="请输入医生姓名" />
        </a-form-item>
        <a-form-item label="职称">
          <a-input v-model:value="formData.doctorTitle" placeholder="请输入职称，如：主任医师" />
        </a-form-item>
        <a-form-item label="专长">
          <a-textarea v-model:value="formData.specialties" placeholder="请输入医生专长" :rows="3" />
        </a-form-item>
        <a-form-item label="简介">
          <a-textarea v-model:value="formData.introduction" placeholder="请输入医生简介" :rows="3" />
        </a-form-item>
        <a-form-item label="门诊时间">
          <a-input v-model:value="formData.clinicTime" placeholder="请输入门诊时间，如：周一至周五 08:00-12:00" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { listDoctors, createDoctor, updateDoctor, deleteDoctor, listHospitals, listDepartments } from '../../api';

const columns = [
  { title: '医生姓名', dataIndex: 'doctorName', key: 'doctorName', width: 120 },
  { title: '职称', dataIndex: 'doctorTitle', key: 'doctorTitle', width: 120 },
  { title: '医院', dataIndex: 'hospitalId', key: 'hospitalName', width: 120 },
  { title: '科室', dataIndex: 'departmentId', key: 'departmentName', width: 120 },
  { title: '专长', dataIndex: 'specialties', key: 'specialties', width: 200, ellipsis: true },
  { title: '门诊时间', dataIndex: 'clinicTime', key: 'clinicTime', width: 150 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' },
];

const tableData = ref([]);
const loading = ref(false);
const searchText = ref('');
const modalVisible = ref(false);
const isEdit = ref(false);
const hospitals = ref([]);
const hospitalsLoading = ref(false);
const departments = ref([]);
const departmentsLoading = ref(false);

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
  doctorName: '',
  hospitalId: null,
  departmentId: null,
  doctorTitle: '',
  specialties: '',
  introduction: '',
  clinicTime: '',
});

const filteredDepartments = computed(() => {
  if (!formData.hospitalId) return [];
  return departments.value.filter(d => d.hospitalId === formData.hospitalId || d.hospitalId === parseInt(formData.hospitalId));
});

const getHospitalName = (hospitalId: any) => {
  const hospital = hospitals.value.find(h => h.id === hospitalId || h.id === parseInt(hospitalId));
  return hospital ? hospital.hospitalName : '-';
};

const getDepartmentName = (departmentId: any) => {
  const dept = departments.value.find(d => d.id === departmentId || d.id === parseInt(departmentId));
  return dept ? dept.departmentName : '-';
};

const handleHospitalChange = () => {
  formData.departmentId = null;
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

const fetchDepartments = async () => {
  departmentsLoading.value = true;
  try {
    const response = await listDepartments(1, 1000);
    if (response.code === 0) {
      departments.value = response.data.records || [];
    }
  } catch (error) {
    console.error('获取科室列表失败', error);
  } finally {
    departmentsLoading.value = false;
  }
};

const handleSearch = () => {
  pagination.current = 1;
  fetchData();
};

const fetchData = async () => {
  loading.value = true;
  try {
    const response = await listDoctors(
      pagination.current,
      pagination.pageSize,
      undefined,
      undefined,
      searchText.value.trim() || undefined,
    );

    if (response.code === 0) {
      tableData.value = (response.data.records || []).map((item: any) => ({
        ...item,
        doctorTitle: item.doctorTitle || item.title || '',
        specialties: item.specialties || item.specialty || ''
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
  formData.doctorName = '';
  formData.hospitalId = null;
  formData.departmentId = null;
  formData.doctorTitle = '';
  formData.specialties = '';
  formData.introduction = '';
  formData.clinicTime = '';
  modalVisible.value = true;
};

const showEditModal = (record: any) => {
  isEdit.value = true;
  Object.assign(formData, {
    ...record,
    doctorTitle: record.doctorTitle || record.title || '',
    specialties: record.specialties || record.specialty || ''
  });
  modalVisible.value = true;
};

const handleSubmit = async () => {
  if (!formData.hospitalId) {
    message.error('请选择医院');
    return;
  }
  if (!formData.departmentId) {
    message.error('请选择科室');
    return;
  }
  if (!formData.doctorName) {
    message.error('请输入医生姓名');
    return;
  }

  try {
    let response;
    if (isEdit.value) {
      response = await updateDoctor(formData);
    } else {
      response = await createDoctor(formData);
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
    const response = await deleteDoctor(record.id);
    if (response.code === 0) {
      message.success(`删除医生 ${record.doctorName} 成功`);
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
  fetchDepartments();
  fetchData();
});
</script>

<style scoped>
.page-container {
  padding: 24px;
}
</style>
