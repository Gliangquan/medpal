<template>
  <div class="page-container">
    <a-page-header
      title="订单管理"
      sub-title="管理平台所有陪诊订单"
      @back="() => $router.back()"
    />

    <a-card :bordered="false" style="margin-top: 24px">
      <!-- 搜索和筛选 -->
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
            placeholder="筛选订单状态"
            @change="handleSearch"
            style="width: 100%"
            allow-clear
          >
            <a-select-option value="pending">待接单</a-select-option>
            <a-select-option value="confirmed">已接单</a-select-option>
            <a-select-option value="serving">服务中</a-select-option>
            <a-select-option value="completion_pending">待用户确认</a-select-option>
            <a-select-option value="completed">已完成</a-select-option>
            <a-select-option value="cancelled">已取消</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-button @click="fetchData" :loading="loading">刷新</a-button>
        </a-col>
      </a-row>

      <!-- 统计信息 -->
      <a-row :gutter="16" style="margin-bottom: 24px">
        <a-col :xs="12" :sm="6">
          <a-statistic title="总订单数" :value="pagination.total" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="待接单" :value="statusStats.pending" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="已完成" :value="statusStats.completed" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="已取消" :value="statusStats.cancelled" />
        </a-col>
      </a-row>

      <!-- 表格 -->
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        :scroll="{ x: 1600 }"
        :row-key="record => record.id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.orderStatus)">
              {{ getStatusText(record.orderStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'paymentStatus'">
            <a-tag :color="getPaymentStatusColor(record.paymentStatus)">
              {{ getPaymentStatusText(record.paymentStatus) }}
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
      title="订单详情"
      :footer="null"
      width="900px"
    >
      <a-descriptions :column="2" bordered v-if="selectedOrder">
        <a-descriptions-item label="订单号">{{ selectedOrder.orderNo }}</a-descriptions-item>
        <a-descriptions-item label="用户ID">{{ selectedOrder.userId }}</a-descriptions-item>
        <a-descriptions-item label="陪诊员ID">{{ selectedOrder.companionId || '-' }}</a-descriptions-item>
        <a-descriptions-item label="医院ID">{{ selectedOrder.hospitalId }}</a-descriptions-item>
        <a-descriptions-item label="科室ID">{{ selectedOrder.departmentId || '-' }}</a-descriptions-item>
        <a-descriptions-item label="医生ID">{{ selectedOrder.doctorId || '-' }}</a-descriptions-item>
        <a-descriptions-item label="预约日期">{{ selectedOrder.appointmentDate }}</a-descriptions-item>
        <a-descriptions-item label="陪诊时长">{{ selectedOrder.duration }}</a-descriptions-item>
        <a-descriptions-item label="服务费">¥{{ selectedOrder.serviceFee || 0 }}</a-descriptions-item>
        <a-descriptions-item label="平台费">¥{{ selectedOrder.platformFee || 0 }}</a-descriptions-item>
        <a-descriptions-item label="总金额">¥{{ selectedOrder.totalPrice || 0 }}</a-descriptions-item>
        <a-descriptions-item label="订单状态">
          <a-tag :color="getStatusColor(selectedOrder.orderStatus)">
            {{ getStatusText(selectedOrder.orderStatus) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="支付状态">
          <a-tag :color="getPaymentStatusColor(selectedOrder.paymentStatus)">
            {{ getPaymentStatusText(selectedOrder.paymentStatus) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="特殊需求" :span="2">{{ selectedOrder.specificNeeds || '-' }}</a-descriptions-item>
        <a-descriptions-item label="创建时间" :span="2">{{ selectedOrder.createTime }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { listOrders } from '../../api';

const columns = [
  { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', width: 150 },
  { title: '用户ID', dataIndex: 'userId', key: 'userId', width: 80 },
  { title: '陪诊员ID', dataIndex: 'companionId', key: 'companionId', width: 100 },
  { title: '医院ID', dataIndex: 'hospitalId', key: 'hospitalId', width: 80 },
  { title: '预约日期', dataIndex: 'appointmentDate', key: 'appointmentDate', width: 150 },
  { title: '金额', dataIndex: 'totalPrice', key: 'totalPrice', width: 100 },
  { title: '订单状态', dataIndex: 'orderStatus', key: 'status', width: 100 },
  { title: '支付状态', dataIndex: 'paymentStatus', key: 'paymentStatus', width: 100 },
  { title: '操作', key: 'action', width: 80, fixed: 'right' },
];

const tableData = ref([]);
const loading = ref(false);
const searchText = ref('');
const filterStatus = ref('');
const detailModalVisible = ref(false);
const selectedOrder = ref(null);

const statusStats = ref({
  pending: 0,
  completed: 0,
  cancelled: 0,
});

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
    pending: 'orange',
    confirmed: 'blue',
    serving: 'cyan',
    completion_pending: 'purple',
    in_progress: 'cyan',
    completed: 'green',
    cancelled: 'red',
  };
  return colors[status] || 'default';
};

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    pending: '待接单',
    confirmed: '已接单',
    serving: '服务中',
    completion_pending: '待用户确认',
    in_progress: '服务中',
    completed: '已完成',
    cancelled: '已取消',
  };
  return texts[status] || status;
};

const getPaymentStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    paid: 'green',
    pending: 'orange',
    unpaid: 'orange',
    failed: 'red',
    refunded: 'blue',
  };
  return colors[status] || 'default';
};

const getPaymentStatusText = (status: string) => {
  const texts: Record<string, string> = {
    paid: '已支付',
    pending: '待支付',
    unpaid: '未支付',
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
    // 管理员获取所有订单，不传 userId
    const response = await listOrders(
      pagination.current,
      pagination.pageSize,
      undefined,
      filterStatus.value as any
    );
    
    if (response.code === 0) {
      tableData.value = response.data.records || [];
      pagination.total = response.data.total || 0;
      
      // 计算统计信息
      calculateStats(tableData.value);
    } else {
      message.error(response.message || '获取数据失败');
    }
  } catch (error) {
    console.error('获取订单列表失败:', error);
    message.error('获取数据失败');
  } finally {
    loading.value = false;
  }
};

const calculateStats = (orders: any[]) => {
  statusStats.value = {
    pending: orders.filter(o => o.orderStatus === 'pending').length,
    completed: orders.filter(o => o.orderStatus === 'completed').length,
    cancelled: orders.filter(o => o.orderStatus === 'cancelled').length,
  };
};

const showDetailModal = (record: any) => {
  selectedOrder.value = record;
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
