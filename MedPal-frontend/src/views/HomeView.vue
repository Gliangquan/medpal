<template>
  <div class="home-page">
    <div v-if="loading" class="loading-wrap">
      <a-spin size="large" />
    </div>

    <template v-else-if="isAdmin">
      <section class="hero-card">
        <div>
          <p class="hero-eyebrow">MedPal 管理后台</p>
          <h1 class="hero-title">欢迎回来，{{ loginUser?.userName || '管理员' }}</h1>
          <p class="hero-desc">这里汇总平台核心运营数据与常用入口，方便你快速处理审核、订单与数据巡检。</p>
        </div>
        <div class="hero-side">
          <div class="hero-badge">{{ unreadCount }} 条未读消息</div>
          <div class="hero-range">统计周期：近 30 天</div>
        </div>
      </section>

      <a-row :gutter="16" class="stats-row">
        <a-col :xs="24" :sm="12" :xl="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic title="平台用户数" :value="overview.totalUsers" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :xl="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic title="陪诊员数" :value="overview.totalCompanions" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :xl="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic title="订单总量" :value="overview.totalOrders" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :xl="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic title="累计收入" :value="overview.totalRevenue" prefix="¥" :precision="2" />
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" class="content-row">
        <a-col :xs="24" :xl="16">
          <a-card title="快捷入口" :bordered="false" class="panel-card">
            <div class="quick-grid">
              <button
                v-for="item in quickEntries"
                :key="item.path"
                type="button"
                class="quick-item"
                @click="go(item.path)"
              >
                <div class="quick-title">{{ item.title }}</div>
                <div class="quick-desc">{{ item.description }}</div>
              </button>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="8">
          <a-card title="运营提醒" :bordered="false" class="panel-card">
            <div class="summary-list">
              <div class="summary-item">
                <span>待接单订单</span>
                <strong>{{ orderStats.pendingOrders }}</strong>
              </div>
              <div class="summary-item">
                <span>待用户确认完成</span>
                <strong>{{ orderStats.completionPendingOrders }}</strong>
              </div>
              <div class="summary-item">
                <span>近 30 天完成订单</span>
                <strong>{{ orderStats.completedOrders }}</strong>
              </div>
              <div class="summary-item">
                <span>近 30 天取消订单</span>
                <strong>{{ orderStats.cancelledOrders }}</strong>
              </div>
              <div class="summary-item">
                <span>已审核陪诊员</span>
                <strong>{{ companionStats.approvedCompanions }}</strong>
              </div>
              <div class="summary-item">
                <span>近 30 天活跃陪诊员</span>
                <strong>{{ companionStats.activeCompanions }}</strong>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" class="content-row">
        <a-col :xs="24" :xl="12">
          <a-card title="服务质量概览" :bordered="false" class="panel-card">
            <div class="quality-card">
              <div class="quality-score">{{ overview.avgRating.toFixed(2) }}</div>
              <div class="quality-meta">
                <div class="quality-title">平台平均评分</div>
                <div class="quality-desc">基于已发布评价实时计算，可前往评价管理查看详情。</div>
                <a-button type="primary" @click="go('/admin/evaluations')">查看评价管理</a-button>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="12">
          <a-card title="处理建议" :bordered="false" class="panel-card">
            <ul class="todo-list">
              <li>优先检查陪诊认证审核，避免新陪诊员无法接单。</li>
              <li>关注待用户确认完成订单，避免完成流转长时间卡住。</li>
              <li>如收入与支付数据异常，可交叉核对统计分析与支付管理页面。</li>
              <li>定期巡检医院 / 科室 / 医生资源，确保预约与搜索链路完整。</li>
            </ul>
          </a-card>
        </a-col>
      </a-row>
    </template>

    <template v-else>
      <div class="fallback-card">
        <h1>WELCOME</h1>
        <p>当前页面用于管理后台首页展示，请使用管理员账号登录查看完整看板。</p>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import dayjs from 'dayjs';
import { message } from 'ant-design-vue';
import { getLoginUser } from '../api';
import request from '../api/request';

const router = useRouter();
const loading = ref(false);
const unreadCount = ref(0);
const loginUser = ref<any>(null);

const overview = reactive({
  totalUsers: 0,
  totalCompanions: 0,
  totalOrders: 0,
  totalRevenue: 0,
  avgRating: 0,
});

const orderStats = reactive({
  pendingOrders: 0,
  completionPendingOrders: 0,
  completedOrders: 0,
  cancelledOrders: 0,
});

const companionStats = reactive({
  approvedCompanions: 0,
  activeCompanions: 0,
});

const quickEntries = [
  { path: '/admin/companion-certifications', title: '陪诊认证审核', description: '处理实名认证与资质认证申请' },
  { path: '/admin/orders', title: '订单管理', description: '查看订单状态、详情与流转情况' },
  { path: '/admin/users', title: '用户管理', description: '搜索用户、核对角色与手机号信息' },
  { path: '/admin/statistics', title: '统计分析', description: '查看运营、收入与趋势数据' },
  { path: '/admin/payments', title: '支付管理', description: '排查支付状态与支付记录' },
  { path: '/admin/hospitals', title: '医疗资源', description: '维护医院、科室、医生资料' },
];

const isAdmin = computed(() => loginUser.value?.userRole === 'admin');

const go = (path: string) => {
  router.push(path);
};

const fetchData = async () => {
  loading.value = true;
  try {
    const loginRes = await getLoginUser();
    if (loginRes.code !== 0 || !loginRes.data) {
      throw new Error(loginRes.message || '获取登录信息失败');
    }
    loginUser.value = loginRes.data;

    if (!isAdmin.value) {
      return;
    }

    const endDate = dayjs().format('YYYY-MM-DD');
    const startDate = dayjs().subtract(29, 'day').format('YYYY-MM-DD');

    const [dashboardRes, orderRes, companionRes, unreadRes] = await Promise.all([
      request.get('/statistics/dashboard'),
      request.get('/statistics/order', { params: { startDate, endDate } }),
      request.get('/statistics/companion', { params: { startDate, endDate } }),
      request.get('/notification/unread-count', { params: { userId: loginUser.value.id } }),
    ]);

    Object.assign(overview, dashboardRes.data || {});
    Object.assign(orderStats, orderRes.data || {});
    Object.assign(companionStats, companionRes.data || {});
    unreadCount.value = Number(unreadRes.data || 0);
  } catch (error: any) {
    message.error(error?.message || '加载首页数据失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.home-page {
  min-height: calc(100vh - 64px);
  padding: 24px;
  background: #f5f7fa;
}

.loading-wrap {
  min-height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-card {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 28px 32px;
  border-radius: 20px;
  color: #fff;
  background: linear-gradient(135deg, #1d4ed8 0%, #3b82f6 45%, #60a5fa 100%);
  box-shadow: 0 12px 30px rgba(29, 78, 216, 0.18);
}

.hero-eyebrow {
  margin: 0 0 8px;
  opacity: 0.82;
  font-size: 14px;
}

.hero-title {
  margin: 0;
  font-size: 30px;
  line-height: 1.2;
}

.hero-desc {
  max-width: 720px;
  margin: 12px 0 0;
  color: rgba(255, 255, 255, 0.9);
}

.hero-side {
  min-width: 180px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: space-between;
}

.hero-badge,
.hero-range {
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(8px);
}

.stats-row,
.content-row {
  margin-top: 16px;
}

.stat-card,
.panel-card {
  border-radius: 18px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.quick-item {
  text-align: left;
  padding: 18px;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  background: #f8fafc;
  cursor: pointer;
  transition: all 0.2s ease;
}

.quick-item:hover {
  border-color: #3b82f6;
  background: #eff6ff;
  transform: translateY(-2px);
}

.quick-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.quick-desc {
  margin-top: 8px;
  color: #6b7280;
  line-height: 1.6;
}

.summary-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  border-radius: 12px;
  background: #f8fafc;
}

.summary-item span {
  color: #4b5563;
}

.summary-item strong {
  font-size: 18px;
  color: #111827;
}

.quality-card {
  display: flex;
  align-items: center;
  gap: 20px;
}

.quality-score {
  flex: 0 0 120px;
  height: 120px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34px;
  font-weight: 700;
  color: #1d4ed8;
  background: radial-gradient(circle at center, #dbeafe 0%, #eff6ff 70%);
}

.quality-meta {
  flex: 1;
}

.quality-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.quality-desc {
  margin: 10px 0 16px;
  color: #6b7280;
  line-height: 1.7;
}

.todo-list {
  margin: 0;
  padding-left: 18px;
  color: #4b5563;
}

.todo-list li + li {
  margin-top: 12px;
}

.fallback-card {
  min-height: 320px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

@media (max-width: 992px) {
  .hero-card {
    flex-direction: column;
  }

  .hero-side {
    align-items: flex-start;
  }

  .quick-grid {
    grid-template-columns: 1fr;
  }

  .quality-card {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
