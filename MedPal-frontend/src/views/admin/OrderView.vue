<template>
  <div class="page-container">
    <a-page-header
      title="订单管理"
      sub-title="查看订单全流程与关键关联信息"
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

      <a-row :gutter="16" style="margin-bottom: 24px">
        <a-col :xs="12" :sm="6">
          <a-statistic title="总订单数" :value="pagination.total" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="待接单" :value="statusStats.pending" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="待确认完成" :value="statusStats.completion_pending" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="已完成" :value="statusStats.completed" />
        </a-col>
      </a-row>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        :scroll="{ x: 1800 }"
        :row-key="record => record.id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'orderStatus'">
            <a-tag :color="getStatusColor(record.orderStatus)">
              {{ getStatusText(record.orderStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'paymentStatus'">
            <a-tag :color="getPaymentStatusColor(record.paymentStatus)">
              {{ getPaymentStatusText(record.paymentStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'totalPrice'">
            ¥{{ record.totalPrice || 0 }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space wrap>
              <a-button type="link" size="small" @click="showDetailModal(record)">详情</a-button>
              <a-button
                v-if="record.orderStatus === 'completion_pending'"
                type="link"
                size="small"
                @click="confirmComplete(record)"
              >确认完成</a-button>
              <a-button
                v-if="record.orderStatus === 'pending'"
                type="link"
                size="small"
                danger
                @click="cancelOrderAction(record)"
              >取消</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:visible="detailModalVisible"
      title="订单详情"
      :footer="null"
      width="960px"
    >
      <div v-if="selectedOrder">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="订单号">{{ selectedOrder.orderNo || '-' }}</a-descriptions-item>
          <a-descriptions-item label="订单状态">
            <a-tag :color="getStatusColor(selectedOrder.orderStatus)">
              {{ getStatusText(selectedOrder.orderStatus) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="患者 ID">{{ selectedOrder.userId || '-' }}</a-descriptions-item>
          <a-descriptions-item label="陪诊员">{{ selectedOrder.companionName || selectedOrder.companionId || '大厅待接单' }}</a-descriptions-item>
          <a-descriptions-item label="医院">{{ selectedOrder.hospitalName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="科室">{{ selectedOrder.departmentName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="医生">{{ selectedOrder.doctorName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="预约时间">{{ selectedOrder.appointmentDate || '-' }}</a-descriptions-item>
          <a-descriptions-item label="陪诊时长">{{ selectedOrder.duration || '-' }}</a-descriptions-item>
          <a-descriptions-item label="患者联系电话">{{ selectedOrder.patientPhone || '-' }}</a-descriptions-item>
          <a-descriptions-item label="陪诊员联系电话">{{ selectedOrder.companionPhone || '-' }}</a-descriptions-item>
          <a-descriptions-item label="支付状态">
            <a-tag :color="getPaymentStatusColor(selectedOrder.paymentStatus)">
              {{ getPaymentStatusText(selectedOrder.paymentStatus) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="总金额">¥{{ selectedOrder.totalPrice || 0 }}</a-descriptions-item>
          <a-descriptions-item label="服务费">¥{{ selectedOrder.serviceFee || 0 }}</a-descriptions-item>
          <a-descriptions-item label="额外费用">¥{{ selectedOrder.extraFee || 0 }}</a-descriptions-item>
          <a-descriptions-item label="平台费">¥{{ selectedOrder.platformFee || 0 }}</a-descriptions-item>
          <a-descriptions-item label="特殊需求" :span="2">{{ selectedOrder.specificNeeds || '-' }}</a-descriptions-item>
          <a-descriptions-item label="取消原因" :span="2">{{ selectedOrder.cancelReason || '-' }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ selectedOrder.createTime || '-' }}</a-descriptions-item>
          <a-descriptions-item label="更新时间">{{ selectedOrder.updateTime || '-' }}</a-descriptions-item>
        </a-descriptions>

        <div style="margin-top: 20px; display: flex; gap: 12px; flex-wrap: wrap;">
          <a-button
            v-if="selectedOrder.orderStatus === 'completion_pending'"
            type="primary"
            @click="confirmComplete(selectedOrder)"
          >确认完成</a-button>
          <a-button
            v-if="selectedOrder.orderStatus === 'pending'"
            danger
            @click="cancelOrderAction(selectedOrder)"
          >取消订单</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { cancelOrder, confirmCompleteOrder, getOrder, listOrders } from '../../api';

const columns = [
  { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', width: 180 },
  { title: '医院', dataIndex: 'hospitalName', key: 'hospitalName', width: 180 },
  { title: '科室', dataIndex: 'departmentName', key: 'departmentName', width: 140 },
  { title: '医生', dataIndex: 'doctorName', key: 'doctorName', width: 140 },
  { title: '陪诊员', dataIndex: 'companionName', key: 'companionName', width: 140 },
  { title: '预约时间', dataIndex: 'appointmentDate', key: 'appointmentDate', width: 180 },
  { title: '总金额', dataIndex: 'totalPrice', key: 'totalPrice', width: 100 },
  { title: '订单状态', dataIndex: 'orderStatus', key: 'orderStatus', width: 120 },
  { title: '支付状态', dataIndex: 'paymentStatus', key: 'paymentStatus', width: 120 },
  { title: '操作', key: 'action', width: 180, fixed: 'right' },
];

const tableData = ref<any[]>([]);
const loading = ref(false);
const searchText = ref('');
const filterStatus = ref<string | undefined>();
const detailModalVisible = ref(false);
const selectedOrder = ref<any>(null);

const statusStats = ref({
  pending: 0,
  completion_pending: 0,
  completed: 0,
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
    completed: 'green',
    cancelled: 'red',
  };
  return colors[status] || 'default';
};

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    draft: '草稿',
    pending: '待接单',
    confirmed: '已接单',
    serving: '服务中',
    completion_pending: '待用户确认',
    completed: '已完成',
    cancelled: '已取消',
  };
  return texts[status] || status;
};

const getPaymentStatusColor = (status?: string) => {
  const colors: Record<string, string> = {
    paid: 'green',
    processing: 'blue',
    pending: 'orange',
    unpaid: 'orange',
    failed: 'red',
    refunded: 'cyan',
  };
  return colors[status || ''] || 'default';
};

const getPaymentStatusText = (status?: string) => {
  const texts: Record<string, string> = {
    paid: '已支付',
    processing: '支付中',
    pending: '待支付',
    unpaid: '未支付',
    failed: '支付失败',
    refunded: '已退款',
  };
  return texts[status || ''] || status || '-';
};

const normalizeOrder = (item: any) => ({
  ...item,
  hospitalName: item.hospitalName || '-',
  departmentName: item.departmentName || '-',
  doctorName: item.doctorName || '-',
  companionName: item.companionName || (item.companionId ? `陪诊员 #${item.companionId}` : '大厅待接单'),
  appointmentDate: item.appointmentDate || '-',
});

const handleSearch = () => {
  pagination.current = 1;
  fetchData();
};

const fetchData = async () => {
  loading.value = true;
  try {
    const response = await listOrders(
      pagination.current,
      pagination.pageSize,
      undefined,
      filterStatus.value,
      searchText.value.trim() || undefined,
    );

    if (response.code === 0) {
      tableData.value = (response.data.records || []).map(normalizeOrder);
      pagination.total = response.data.total || 0;
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
    pending: orders.filter((o) => o.orderStatus === 'pending').length,
    completion_pending: orders.filter((o) => o.orderStatus === 'completion_pending').length,
    completed: orders.filter((o) => o.orderStatus === 'completed').length,
  };
};

const showDetailModal = async (record: any) => {
  try {
    const response = await getOrder(record.id);
    if (response.code === 0) {
      selectedOrder.value = normalizeOrder(response.data || record);
      detailModalVisible.value = true;
    } else {
      message.error(response.message || '获取订单详情失败');
    }
  } catch (error) {
    message.error('获取订单详情失败');
  }
};

const confirmComplete = (record: any) => {
  Modal.confirm({
    title: '确认完成订单',
    content: `确定将订单 ${record.orderNo || '#' + record.id} 标记为已完成吗？`,
    async onOk() {
      try {
        const response = await confirmCompleteOrder(record.id);
        if (response.code === 0) {
          message.success('订单已完成');
          detailModalVisible.value = false;
          fetchData();
        } else {
          message.error(response.message || '确认完成失败');
        }
      } catch (error) {
        message.error('确认完成失败');
      }
    },
  });
};

const cancelOrderAction = (record: any) => {
  Modal.confirm({
    title: '取消订单',
    content: `确定取消订单 ${record.orderNo || '#' + record.id} 吗？`,
    async onOk() {
      try {
        const response = await cancelOrder(record.id, '管理员取消');
        if (response.code === 0) {
          message.success('订单已取消');
          detailModalVisible.value = false;
          fetchData();
        } else {
          message.error(response.message || '取消失败');
        }
      } catch (error) {
        message.error('取消失败');
      }
    },
  });
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
