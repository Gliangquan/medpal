<template>
  <div class="page-container">
    <a-page-header
      title="统计分析"
      sub-title="平台运营数据统计"
      @back="() => $router.back()"
    />

    <a-row :gutter="16" style="margin-bottom: 24px">
      <a-col :xs="24" :sm="12" :md="6">
        <a-statistic title="总订单数" :value="overview.totalOrders" />
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-statistic title="总用户数" :value="overview.totalUsers" />
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-statistic title="陪诊员数" :value="overview.totalCompanions" />
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-statistic title="平均评分" :value="overview.avgRating" suffix="分" :precision="2" />
      </a-col>
    </a-row>

    <a-card :bordered="false">
      <a-row :gutter="16" style="margin-bottom: 24px">
        <a-col :xs="24" :sm="12" :md="8">
          <a-range-picker
            v-model:value="dateRange"
            style="width: 100%"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </a-col>
        <a-col :xs="24" :sm="12" :md="16" style="display: flex; gap: 12px; flex-wrap: wrap;">
          <a-statistic title="区间完成订单" :value="orderStats.completedOrders" />
          <a-statistic title="区间收入" :value="revenueStats.totalRevenue" prefix="¥" :precision="2" />
          <a-statistic title="活跃陪诊员" :value="companionStats.activeCompanions" />
        </a-col>
      </a-row>

      <a-table
        :columns="tableColumns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        :scroll="{ x: 1000 }"
        :row-key="record => record.id"
      />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import request from '../../api/request';

const tableColumns = [
  { title: '日期', dataIndex: 'date', key: 'date', width: 120 },
  { title: '新增订单', dataIndex: 'newOrders', key: 'newOrders', width: 100 },
  { title: '完成订单', dataIndex: 'completedOrders', key: 'completedOrders', width: 100 },
  { title: '新增用户', dataIndex: 'newUsers', key: 'newUsers', width: 100 },
  { title: '日收入', dataIndex: 'dailyIncome', key: 'dailyIncome', width: 120 },
  { title: '平均评分', dataIndex: 'avgRating', key: 'avgRating', width: 100 },
];

const tableData = ref<any[]>([]);
const loading = ref(false);
const dateRange = ref<[string, string]>([
  dayjs().subtract(30, 'days').format('YYYY-MM-DD'),
  dayjs().format('YYYY-MM-DD'),
]);

const overview = reactive({
  totalOrders: 0,
  totalUsers: 0,
  totalCompanions: 0,
  avgRating: 0,
});

const orderStats = reactive({
  completedOrders: 0,
});

const revenueStats = reactive({
  totalRevenue: 0,
});

const companionStats = reactive({
  activeCompanions: 0,
});

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  pageSizeOptions: ['10', '20', '50', '100'],
});

const fetchDashboard = async () => {
  const response = await request.get('/statistics/dashboard');
  Object.assign(overview, response.data || {});
};

const fetchRangeStats = async () => {
  const [startDate, endDate] = dateRange.value;
  const [userRes, orderRes, revenueRes, companionRes] = await Promise.all([
    request.get('/statistics/user', { params: { startDate, endDate } }),
    request.get('/statistics/order', { params: { startDate, endDate } }),
    request.get('/statistics/revenue', { params: { startDate, endDate } }),
    request.get('/statistics/companion', { params: { startDate, endDate } }),
  ]);

  const userStats = userRes.data || {};
  Object.assign(overview, {
    totalUsers: userStats.totalUsers || 0,
    totalOrders: userStats.totalOrders || 0,
    totalCompanions: userStats.totalCompanions || 0,
    avgRating: userStats.avgRating || 0,
  });
  Object.assign(orderStats, orderRes.data || {});
  Object.assign(revenueStats, revenueRes.data || {});
  Object.assign(companionStats, companionRes.data || {});
  tableData.value = userStats.trend || [];
  pagination.total = tableData.value.length;
};

const fetchData = async () => {
  loading.value = true;
  try {
    await fetchDashboard();
    await fetchRangeStats();
  } catch (error: any) {
    message.error(error?.message || '获取统计数据失败');
  } finally {
    loading.value = false;
  }
};

const handleDateChange = () => {
  pagination.current = 1;
  fetchData();
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
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
