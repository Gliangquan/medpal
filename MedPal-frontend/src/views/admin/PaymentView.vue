<template>
  <div class="page-container">
    <a-page-header
      title="支付管理"
      sub-title="管理平台所有支付记录"
      @back="() => $router.back()"
    />

    <a-card :bordered="false" style="margin-top: 24px">
      <!-- 搜索和筛选 -->
      <a-row :gutter="16" style="margin-bottom: 24px">
        <a-col :xs="24" :sm="12" :md="6">
          <a-input-search
            v-model:value="searchText"
            placeholder="搜索支付号"
            @search="handleSearch"
            allow-clear
          />
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-select
            v-model:value="filterStatus"
            placeholder="筛选支付状态"
            @change="handleSearch"
            style="width: 100%"
            allow-clear
          >
            <a-select-option value="paid">已支付</a-select-option>
            <a-select-option value="unpaid">未支付</a-select-option>
            <a-select-option value="processing">处理中</a-select-option>
            <a-select-option value="failed">支付失败</a-select-option>
            <a-select-option value="refunded">已退款</a-select-option>
          </a-select>
        </a-col>
      </a-row>

      <!-- 表格 -->
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
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.paymentStatus)">
              {{ getStatusText(record.paymentStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="showDetailModal(record)">详情</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 详情模态框 -->
    <a-modal
      v-model:visible="detailModalVisible"
      title="支付详情"
      :footer="null"
      width="700px"
    >
      <a-descriptions :column="2" bordered v-if="selectedPayment">
        <a-descriptions-item label="支付号">{{ selectedPayment.paymentNo }}</a-descriptions-item>
        <a-descriptions-item label="订单号">{{ selectedPayment.orderNo }}</a-descriptions-item>
        <a-descriptions-item label="用户ID">{{ selectedPayment.userId }}</a-descriptions-item>
        <a-descriptions-item label="支付金额">¥{{ selectedPayment.amount }}</a-descriptions-item>
        <a-descriptions-item label="支付渠道">{{ selectedPayment.paymentChannel }}</a-descriptions-item>
        <a-descriptions-item label="支付状态">
          <a-tag :color="getStatusColor(selectedPayment.paymentStatus)">
            {{ getStatusText(selectedPayment.paymentStatus) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="交易ID">{{ selectedPayment.transactionId || '-' }}</a-descriptions-item>
        <a-descriptions-item label="支付时间">{{ selectedPayment.paidTime || '-' }}</a-descriptions-item>
        <a-descriptions-item label="创建时间" :span="2">{{ selectedPayment.createTime }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { listPayments, type PaymentVO } from '../../api';

const columns = [
  { title: '支付号', dataIndex: 'paymentNo', key: 'paymentNo', width: 150 },
  { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', width: 150 },
  { title: '用户ID', dataIndex: 'userId', key: 'userId', width: 100 },
  { title: '金额', dataIndex: 'amount', key: 'amount', width: 100 },
  { title: '支付渠道', dataIndex: 'paymentChannel', key: 'paymentChannel', width: 100 },
  { title: '状态', dataIndex: 'paymentStatus', key: 'status', width: 100 },
  { title: '支付时间', dataIndex: 'paidTime', key: 'paidTime', width: 150 },
  { title: '操作', key: 'action', width: 100, fixed: 'right' },
];

const tableData = ref<PaymentVO[]>([]);
const loading = ref(false);
const searchText = ref('');
const filterStatus = ref('');
const detailModalVisible = ref(false);
const selectedPayment = ref<PaymentVO | null>(null);

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
    paid: 'green',
    unpaid: 'orange',
    pending: 'orange',
    processing: 'blue',
    failed: 'red',
    refunded: 'purple',
  };
  return colors[status] || 'default';
};

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    paid: '已支付',
    unpaid: '未支付',
    pending: '待支付',
    processing: '处理中',
    failed: '支付失败',
    refunded: '已退款',
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
    const response = await listPayments(
      pagination.current,
      pagination.pageSize,
      searchText.value || undefined,
      (filterStatus.value || undefined) as any
    );

    tableData.value = response.data.records || [];
    pagination.total = response.data.total || 0;
  } catch (error) {
    message.error('获取数据失败');
  } finally {
    loading.value = false;
  }
};

const showDetailModal = (record: PaymentVO) => {
  selectedPayment.value = record;
  detailModalVisible.value = true;
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
