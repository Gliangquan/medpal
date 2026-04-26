<template>
  <div class="page-container">
    <a-page-header
      title="评价管理"
      sub-title="管理平台所有用户评价"
      @back="() => $router.back()"
    />

    <a-card :bordered="false" style="margin-top: 24px">
      <a-row :gutter="16" style="margin-bottom: 24px">
        <a-col :xs="24" :sm="12" :md="6">
          <a-input-search
            v-model:value="searchText"
            placeholder="搜索订单号"
            @search="handleSearch"
            allow-clear
          />
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-select
            v-model:value="filterStatus"
            placeholder="筛选评价状态"
            @change="handleSearch"
            style="width: 100%"
            allow-clear
          >
            <a-select-option value="published">已发布</a-select-option>
            <a-select-option value="draft">草稿</a-select-option>
            <a-select-option value="hidden">已隐藏</a-select-option>
          </a-select>
        </a-col>
      </a-row>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        :scroll="{ x: 1400 }"
        :row-key="record => record.id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'score'">
            <a-rate :value="record.averageScore" disabled allow-half />
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="showDetailModal(record)">详情</a-button>
              <a-popconfirm
                title="确定删除该评价吗？"
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

    <a-modal
      v-model:visible="detailModalVisible"
      title="评价详情"
      :footer="null"
      width="700px"
    >
      <a-descriptions :column="1" bordered v-if="selectedEvaluation">
        <a-descriptions-item label="订单ID">{{ selectedEvaluation.orderId }}</a-descriptions-item>
        <a-descriptions-item label="用户ID">{{ selectedEvaluation.userId }}</a-descriptions-item>
        <a-descriptions-item label="陪诊员ID">{{ selectedEvaluation.companionId }}</a-descriptions-item>
        <a-descriptions-item label="专业性评分">
          <a-rate :value="selectedEvaluation.professionalismScore" disabled />
        </a-descriptions-item>
        <a-descriptions-item label="态度评分">
          <a-rate :value="selectedEvaluation.attitudeScore" disabled />
        </a-descriptions-item>
        <a-descriptions-item label="效率评分">
          <a-rate :value="selectedEvaluation.efficiencyScore" disabled />
        </a-descriptions-item>
        <a-descriptions-item label="满意度评分">
          <a-rate :value="selectedEvaluation.satisfactionScore" disabled />
        </a-descriptions-item>
        <a-descriptions-item label="平均评分">
          <a-rate :value="selectedEvaluation.averageScore" disabled allow-half />
        </a-descriptions-item>
        <a-descriptions-item label="评价内容">
          {{ selectedEvaluation.evaluationText || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="评价状态">
          <a-tag :color="getStatusColor(selectedEvaluation.status)">
            {{ getStatusText(selectedEvaluation.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ selectedEvaluation.createTime }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { listEvaluations, deleteEvaluation } from '../../api';

const columns = [
  { title: '订单ID', dataIndex: 'orderId', key: 'orderId', width: 100 },
  { title: '用户ID', dataIndex: 'userId', key: 'userId', width: 100 },
  { title: '陪诊员ID', dataIndex: 'companionId', key: 'companionId', width: 100 },
  { title: '平均评分', dataIndex: 'averageScore', key: 'score', width: 120 },
  { title: '评价状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 150 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' },
];

const tableData = ref([]);
const loading = ref(false);
const searchText = ref('');
const filterStatus = ref('');
const detailModalVisible = ref(false);
const selectedEvaluation = ref(null);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  pageSizeOptions: ['10', '20', '50', '100'],
});

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    published: 'green',
    draft: 'orange',
    hidden: 'red',
  };
  return colors[status] || 'default';
};

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    published: '已发布',
    draft: '草稿',
    hidden: '已隐藏',
  };
  return texts[status] || status;
};

const handleSearch = () => {
  pagination.current = 1;
  fetchData();
};

const fetchData = async () => {
  loading.value = true;
  try {
    const orderIdText = searchText.value.trim();
    const response = await listEvaluations(
      pagination.current,
      pagination.pageSize,
      orderIdText ? Number(orderIdText) || undefined : undefined,
      undefined,
      undefined,
      filterStatus.value || undefined,
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

const showDetailModal = (record: any) => {
  selectedEvaluation.value = record;
  detailModalVisible.value = true;
};

const handleDelete = async (record: any) => {
  try {
    const response = await deleteEvaluation(record.id);
    if (response.code === 0) {
      message.success('删除评价成功');
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
