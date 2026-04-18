import { createRouter, createWebHistory } from 'vue-router';
import { getLoginUser } from '../api';

import HomeView from '../views/HomeView.vue';

import Login from '../views/LoginView.vue';
import Register from '../views/RegisterView.vue';
import LayoutView from '../views/LayoutView.vue';
import ProfileView from '../views/ProfileView.vue';
import ChatView from '../views/ChatView.vue';

// Admin views
import UserView from '../views/admin/UserView.vue';
import HospitalView from '../views/admin/HospitalView.vue';
import DepartmentView from '../views/admin/DepartmentView.vue';
import DoctorView from '../views/admin/DoctorView.vue';
import OrderView from '../views/admin/OrderView.vue';
import PaymentView from '../views/admin/PaymentView.vue';
import EvaluationView from '../views/admin/EvaluationView.vue';
import FinanceView from '../views/admin/FinanceView.vue';
import StatisticsView from '../views/admin/StatisticsView.vue';
import ContentView from '../views/admin/ContentView.vue';
import SystemSettingsView from '../views/admin/SystemSettingsView.vue';
import CompanionCertificationView from '../views/admin/CompanionCertificationView.vue';




// 定义路由
const routes = [
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  {
    path: '/',
    component: LayoutView,
    children: [
      { path: '/', component: HomeView },
      { path: '/profile', component: ProfileView, meta: { requiresAuth: true } },
      { path: '/chat', component: ChatView, meta: { requiresAuth: true } },
    ],
  },
  {
    path: '/admin',
    component: LayoutView,
    redirect: '/admin/users',
    children: [
      { path: 'users', component: UserView, meta: { requiresAuth: true } },
      { path: 'hospitals', component: HospitalView, meta: { requiresAuth: true } },
      { path: 'departments', component: DepartmentView, meta: { requiresAuth: true } },
      { path: 'doctors', component: DoctorView, meta: { requiresAuth: true } },
      { path: 'orders', component: OrderView, meta: { requiresAuth: true } },
      { path: 'payments', component: PaymentView, meta: { requiresAuth: true } },
      { path: 'evaluations', component: EvaluationView, meta: { requiresAuth: true } },
      { path: 'finance', component: FinanceView, meta: { requiresAuth: true } },
      { path: 'companion-certifications', component: CompanionCertificationView, meta: { requiresAuth: true } },
      { path: 'statistics', component: StatisticsView, meta: { requiresAuth: true } },
      { path: 'content', component: ContentView, meta: { requiresAuth: true } },
      { path: 'settings', component: SystemSettingsView, meta: { requiresAuth: true } },
    ],
  },
];

// 创建路由实例
const router = createRouter({
  history: createWebHistory(), // 使用 history 模式
  routes,
});

// 导航守卫：权限校验
router.beforeEach(async (to, from, next) => {
  // 如果已经在登录页面，直接放行
  if (to.path === '/login' || to.path === '/register') {
    next();
    return;
  }

  // 需要认证的页面
  if (to.meta.requiresAuth || to.path === '/' || to.path.startsWith('/admin')) {
    try {
      const res = await getLoginUser();
      if (res.code === 0) {
        const isAdmin = res?.data?.userRole === 'admin';
        if (to.path.startsWith('/admin') && !isAdmin) {
          next('/');
          return;
        }
        // 用户已登录，允许访问
        next();
      } else {
        // 用户未登录或登录失效，重定向到登录页
        next('/login');
      }
    } catch (error) {
      // 请求失败，重定向到登录页
      next('/login');
    }
  } else {
    next();
  }
});

export default router;
