<template>
  <div class="page-container">
    <a-page-header
      title="通知中心"
      sub-title="查看当前管理员账号收到的系统与订单通知"
      @back="() => $router.back()"
    />

    <a-card :bordered="false" style="margin-top: 24px">
      <a-row :gutter="16" style="margin-bottom: 24px">
        <a-col :xs="24" :sm="12" :md="6">
          <a-select
            v-model:value="filterStatus"
            placeholder="筛选通知状态"
            style="width: 100%"
            allow-clear
            @change="handleSearch"
          >
            <a-select-option value="unread">未读</a-select-option>
            <a-select-option value="read">已读</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="12" :md="18" style="display: flex; justify-content: flex-end">
          <a-space>
            <a-statistic title="未读通知" :value="unreadCount" />
            <a-button @click="fetchData" :loading="loading">刷新</a-button>
            <a-button type="primary" :disabled="!unreadCount" @click="handleMarkAllRead">全部已读</a-button>
          </a-space>
        </a-col>
      </a-row>

      <a-list :data-source="list" :loading="loading" item-layout="horizontal">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-list-item-meta>
              <template #title>
                <div class="notice-title-row">
                  <span>{{ item.title }}</span>
                  <a-tag :color="item.status === 'read' ? 'default' : 'blue'">
                    {{ item.status === 'read' ? '已读' : '未读' }}
                  </a-tag>
                </div>
              </template>
              <template #description>
                <div>
                  <div>{{ item.content }}</div>
                  <div class="notice-meta">
                    <span>类型：{{ item.type || '-' }}</span>
                    <span>创建时间：{{ item.createTime || '-' }}</span>
                  </div>
                </div>
              </template>
            </a-list-item-meta>
            <template #actions>
              <a-button v-if="item.status !== 'read'" type="link" @click="handleRead(item.id)">标记已读</a-button>
            </template>
          </a-list-item>
        </template>
      </a-list>

      <a-pagination
        style="margin-top: 24px; text-align: right"
        :current="pagination.current"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        :show-size-changer="true"
        :show-quick-jumper="true"
        @change="handlePageChange"
        @showSizeChange="handlePageChange"
      />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import { getLoginUser, getUnreadCount, listNotifications, markAllAsRead, markNotificationAsRead, type LoginUserVO, type Notification } from '../../api';

const loading = ref(false);
const list = ref<Notification[]>([]);
const unreadCount = ref(0);
const loginUser = ref<LoginUserVO | null>(null);
const filterStatus = ref<string>('');

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

const ensureLoginUser = async () => {
  if (loginUser.value?.id) return loginUser.value;
  const response = await getLoginUser();
  loginUser.value = response.data;
  return loginUser.value;
};

const fetchUnreadCount = async () => {
  const user = await ensureLoginUser();
  if (!user?.id) return;
  const response = await getUnreadCount(user.id);
  unreadCount.value = Number(response.data || 0);
};

const fetchData = async () => {
  loading.value = true;
  try {
    const user = await ensureLoginUser();
    if (!user?.id) {
      throw new Error('未获取到当前用户');
    }
    const response = await listNotifications(
      user.id,
      (filterStatus.value || undefined) as any,
      pagination.current,
      pagination.pageSize
    );
    list.value = response.data.records || [];
    pagination.total = response.data.total || 0;
    await fetchUnreadCount();
  } catch (error: any) {
    message.error(error?.message || '加载通知失败');
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.current = 1;
  fetchData();
};

const handleRead = async (id: number) => {
  try {
    await markNotificationAsRead(id);
    message.success('已标记为已读');
    await fetchData();
  } catch (error: any) {
    message.error(error?.message || '操作失败');
  }
};

const handleMarkAllRead = async () => {
  try {
    const user = await ensureLoginUser();
    if (!user?.id) return;
    await markAllAsRead(user.id);
    message.success('全部已读');
    await fetchData();
  } catch (error: any) {
    message.error(error?.message || '操作失败');
  }
};

const handlePageChange = (page: number, pageSize: number) => {
  pagination.current = page;
  pagination.pageSize = pageSize;
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

.notice-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.notice-meta {
  margin-top: 8px;
  display: flex;
  gap: 16px;
  color: #8c8c8c;
  font-size: 12px;
}
</style>
