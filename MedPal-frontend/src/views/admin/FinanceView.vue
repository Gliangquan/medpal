<template>
  <div class="page-container">
    <a-page-header
      title="财务管理"
      sub-title="管理平台财务和结算"
      @back="() => $router.back()"
    />

    <!-- 统计卡片 -->
    <a-row :gutter="16" style="margin-bottom: 24px">
      <a-col :xs="24" :sm="12" :md="6">
        <a-statistic title="总收入" :value="totalIncome" prefix="¥" />
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-statistic title="待结算金额" :value="pendingAmount" prefix="¥" />
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-statistic title="已结算金额" :value="settledAmount" prefix="¥" />
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-statistic title="平台手续费" :value="platformFee" prefix="¥" />
      </a-col>
    </a-row>

    <a-card :bordered="false">
      <!-- 搜索和筛选 -->
      <a-row :gutter="16" style="margin-bottom: 24px">
        <a-col :xs="24" :sm="12" :md="6">
          <a-input-search
            v-model:value="searchText"
            placeholder="搜索陪诊员名称"
            @search="handleSearch"
            allow-clear
          />
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-select
            v-model:value="filterStatus"
            placeholder="筛选结算状态"
            @change="handleSearch"
            style="width: 100%"
            allow-clear
          >
            <a-select-option value="pending">待结算</a-select-option>
            <a-select-option value="settled">已结算</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-button type="primary" @click="handleBatchSettle">批量结算</a-button>
        </a-col>
      </a-row>

      <!-- 表格 -->
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        :scroll="{ x: 1300 }"
        :row-key="record => record.id"
        :row-selection="rowSelection"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'settled' ? 'green' : 'orange'">
              {{ record.status === 'settled' ? '已结算' : '待结算' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                v-if="record.status === 'pending'"
                type="link"
                size="small"
                @click="handleSettle(record)"
              >
                结算
              </a-button>
              <a-button type="link" size="small" @click="showDetailModal(record)">详情</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 详情模态框 -->
    <a-modal
      v-model:visible="detailModalVisible"
      title="结算详情"
      :footer="null"
      width="800px"
    >
      <a-descriptions :column="2" bordered v-if="selectedRecord">
        <a-descriptions-item label="陪诊员">{{ selectedRecord.companionName }}</a-descriptions-item>
        <a-descriptions-item label="手机号">{{ selectedRecord.phone }}</a-descriptions-item>
        <a-descriptions-item label="服务订单数">{{ selectedRecord.orderCount }}</a-descriptions-item>
        <a-descriptions-item label="服务总时长">{{ selectedRecord.totalHours }}小时</a-descriptions-item>
        <a-descriptions-item label="应结算金额">¥{{ selectedRecord.amount }}</a-descriptions-item>
        <a-descriptions-item label="平台手续费">¥{{ selectedRecord.fee }}</a-descriptions-item>
        <a-descriptions-item label="实际到账">¥{{ selectedRecord.actualAmount }}</a-descriptions-item>
        <a-descriptions-item label="结算状态">
          <a-tag :color="selectedRecord.status === 'settled' ? 'green' : 'orange'">
            {{ selectedRecord.status === 'settled' ? '已结算' : '待结算' }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { batchProcessFinanceSettlements, getFinanceSummary, listFinanceSettlements, processFinanceSettlement } from '../../api';

const columns = [
  { title: '陪诊员', dataIndex: 'companionName', key: 'companionName', width: 120 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 120 },
  { title: '订单数', dataIndex: 'orderCount', key: 'orderCount', width: 80 },
  { title: '服务时长', dataIndex: 'totalHours', key: 'totalHours', width: 100 },
  { title: '应结算金额', dataIndex: 'amount', key: 'amount', width: 120 },
  { title: '平台手续费', dataIndex: 'fee', key: 'fee', width: 120 },
  { title: '实际到账', dataIndex: 'actualAmount', key: 'actualAmount', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' },
];

const tableData = ref<any[]>([]);
const loading = ref(false);
const searchText = ref('');
const filterStatus = ref('');
const detailModalVisible = ref(false);
const selectedRecord = ref<any>(null);
const selectedRowKeys = ref<any[]>([]);

const totalIncome = ref(0);
const pendingAmount = ref(0);
const settledAmount = ref(0);
const platformFee = ref(0);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  pageSizeOptions: ['10', '20', '50', '100'],
});

const rowSelection = reactive({
  selectedRowKeys: selectedRowKeys,
  onChange: (keys: any) => {
    selectedRowKeys.value = keys;
  },
});

const handleSearch = () => {
  pagination.current = 1;
  fetchData();
};

const fetchSummary = async () => {
  const summary = await getFinanceSummary();
  totalIncome.value = Number(summary.data?.totalIncome || 0);
  pendingAmount.value = Number(summary.data?.pendingAmount || 0);
  settledAmount.value = Number(summary.data?.settledAmount || 0);
  platformFee.value = Number(summary.data?.platformFee || 0);
};

const fetchData = async () => {
  loading.value = true;
  try {
    await fetchSummary();
    const page = await listFinanceSettlements(
      pagination.current,
      pagination.pageSize,
      filterStatus.value || undefined,
      searchText.value.trim() || undefined
    );
    tableData.value = page.data?.records || [];
    pagination.total = page.data?.total || 0;
  } catch (error) {
    message.error('获取数据失败');
  } finally {
    loading.value = false;
  }
};

const showDetailModal = (record: any) => {
  selectedRecord.value = record;
  detailModalVisible.value = true;
};

const handleSettle = async (record: any) => {
  try {
    await processFinanceSettlement(Number(record.id));
    message.success(`结算陪诊员 ${record.companionName} 成功`);
    detailModalVisible.value = false;
    fetchData();
  } catch (error: any) {
    message.error(error?.message || '结算失败');
  }
};

const handleBatchSettle = async () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择要结算的陪诊员');
    return;
  }
  try {
    await batchProcessFinanceSettlements(selectedRowKeys.value.join(','));
    message.success(`批量结算 ${selectedRowKeys.value.length} 条记录成功`);
    selectedRowKeys.value = [];
    fetchData();
  } catch (error: any) {
    message.error(error?.message || '批量结算失败');
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
