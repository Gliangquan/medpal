<template>
  <div class="page-container">
    <a-page-header
      title="内容管理"
      sub-title="管理科普文章与平台公告，发布后将推送到 UniApp"
      @back="() => $router.back()"
    />

    <a-card :bordered="false" style="margin-top: 16px">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="article" tab="科普文章" />
        <a-tab-pane key="announcement" tab="平台公告" />
      </a-tabs>

      <a-row :gutter="12" style="margin-bottom: 16px">
        <a-col :xs="24" :sm="10" :md="8">
          <a-input-search
            v-model:value="keyword"
            placeholder="搜索标题"
            allow-clear
            @search="refreshList"
          />
        </a-col>
        <a-col :xs="24" :sm="14" :md="16">
          <a-space>
            <a-button type="primary" @click="openCreateModal">新增{{ activeTabLabel }}</a-button>
            <a-button @click="refreshList">刷新</a-button>
          </a-space>
        </a-col>
      </a-row>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-key="(record: ContentItem) => record.id as number"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'priority'">
            <a-tag :color="(record.priority || 0) >= 8 ? 'red' : 'blue'">
              {{ (record.priority || 0) >= 8 ? '紧急' : '普通' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'summary'">
            <span class="summary-cell">{{ record.summary || '-' }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openEditModal(record)">编辑</a-button>
              <a-button
                v-if="record.status !== 'published'"
                type="link"
                size="small"
                @click="handlePublish(record)"
              >
                发布
              </a-button>
              <a-button
                v-else
                type="link"
                size="small"
                @click="handleUnpublish(record)"
              >
                下架
              </a-button>
              <a-popconfirm
                title="确定删除该内容吗？"
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
      v-model:open="articleModalOpen"
      :title="articleForm.id ? '编辑科普文章' : '新增科普文章'"
      width="980px"
      :confirm-loading="submitting"
      @ok="submitArticle"
      @cancel="resetArticleForm"
    >
      <a-form layout="vertical">
        <a-form-item label="标题" required>
          <a-input v-model:value="articleForm.title" placeholder="请输入文章标题" />
        </a-form-item>
        <a-form-item label="摘要">
          <a-textarea v-model:value="articleForm.summary" :rows="2" placeholder="用于通知与列表展示" />
        </a-form-item>
        <a-form-item label="标签">
          <a-input v-model:value="articleForm.tags" placeholder="如：慢病管理,就医指南" />
        </a-form-item>
        <a-form-item label="正文（Markdown）" required>
          <a-row :gutter="12">
            <a-col :span="12">
              <a-textarea
                v-model:value="articleForm.content"
                :rows="14"
                placeholder="支持 Markdown 语法"
              />
            </a-col>
            <a-col :span="12">
              <div class="md-preview" v-html="markdownToHtml(articleForm.content || '')"></div>
            </a-col>
          </a-row>
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="articleForm.status">
            <a-radio value="draft">草稿</a-radio>
            <a-radio value="published">立即发布</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="announcementModalOpen"
      :title="announcementForm.id ? '编辑平台公告' : '新增平台公告'"
      width="980px"
      :confirm-loading="submitting"
      @ok="submitAnnouncement"
      @cancel="resetAnnouncementForm"
    >
      <a-form layout="vertical">
        <a-form-item label="标题" required>
          <a-input v-model:value="announcementForm.title" placeholder="请输入公告标题" />
        </a-form-item>
        <a-form-item label="摘要">
          <a-textarea v-model:value="announcementForm.summary" :rows="2" placeholder="建议简明扼要" />
        </a-form-item>
        <a-form-item label="优先级">
          <a-radio-group v-model:value="announcementForm.priority">
            <a-radio :value="1">普通</a-radio>
            <a-radio :value="10">紧急</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="推送对象">
          <a-checkbox-group v-model:value="announcementForm.targets">
            <a-checkbox value="users">患者端</a-checkbox>
            <a-checkbox value="companions">陪诊员端</a-checkbox>
          </a-checkbox-group>
        </a-form-item>
        <a-form-item label="正文（Markdown）" required>
          <a-row :gutter="12">
            <a-col :span="12">
              <a-textarea
                v-model:value="announcementForm.content"
                :rows="14"
                placeholder="支持 Markdown 语法"
              />
            </a-col>
            <a-col :span="12">
              <div class="md-preview" v-html="markdownToHtml(announcementForm.content || '')"></div>
            </a-col>
          </a-row>
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="announcementForm.status">
            <a-radio value="draft">草稿</a-radio>
            <a-radio value="published">立即发布</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import {
  type ContentItem,
  createContent,
  deleteContent,
  listContentByPage,
  publishContent,
  unpublishContent,
  updateContent,
} from '../../api';

const activeTab = ref<'article' | 'announcement'>('article');
const keyword = ref('');
const loading = ref(false);
const submitting = ref(false);
const tableData = ref<ContentItem[]>([]);
const articleModalOpen = ref(false);
const announcementModalOpen = ref(false);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
});

const articleForm = reactive<{
  id: number | null;
  title: string;
  summary: string;
  tags: string;
  content: string;
  status: 'draft' | 'published';
}>({
  id: null,
  title: '',
  summary: '',
  tags: '',
  content: '',
  status: 'draft',
});

const announcementForm = reactive<{
  id: number | null;
  title: string;
  summary: string;
  content: string;
  priority: number;
  targets: string[];
  status: 'draft' | 'published';
}>({
  id: null,
  title: '',
  summary: '',
  content: '',
  priority: 1,
  targets: ['users', 'companions'],
  status: 'draft',
});

const activeTabLabel = computed(() => (activeTab.value === 'article' ? '文章' : '公告'));

const columns = computed(() => {
  if (activeTab.value === 'announcement') {
    return [
      { title: '标题', dataIndex: 'title', key: 'title', width: 260 },
      { title: '摘要', dataIndex: 'summary', key: 'summary', width: 300 },
      { title: '优先级', dataIndex: 'priority', key: 'priority', width: 100 },
      { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
      { title: '发布时间', dataIndex: 'publishTime', key: 'publishTime', width: 180 },
      { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 },
      { title: '操作', key: 'action', width: 220, fixed: 'right' as const },
    ];
  }
  return [
    { title: '标题', dataIndex: 'title', key: 'title', width: 260 },
    { title: '摘要', dataIndex: 'summary', key: 'summary', width: 320 },
    { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
    { title: '阅读量', dataIndex: 'viewCount', key: 'viewCount', width: 100 },
    { title: '发布时间', dataIndex: 'publishTime', key: 'publishTime', width: 180 },
    { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 },
    { title: '操作', key: 'action', width: 220, fixed: 'right' as const },
  ];
});

const statusText = (status?: string) => {
  if (status === 'published') return '已发布';
  if (status === 'unpublished') return '已下架';
  return '草稿';
};

const statusColor = (status?: string) => {
  if (status === 'published') return 'green';
  if (status === 'unpublished') return 'orange';
  return 'default';
};

const resetArticleForm = () => {
  articleForm.id = null;
  articleForm.title = '';
  articleForm.summary = '';
  articleForm.tags = '';
  articleForm.content = '';
  articleForm.status = 'draft';
};

const resetAnnouncementForm = () => {
  announcementForm.id = null;
  announcementForm.title = '';
  announcementForm.summary = '';
  announcementForm.content = '';
  announcementForm.priority = 1;
  announcementForm.targets = ['users', 'companions'];
  announcementForm.status = 'draft';
};

const normalizeTargetsFromTags = (tags?: string) => {
  if (!tags) return ['users', 'companions'];
  const lower = tags.toLowerCase();
  const idx = Math.max(lower.indexOf('targets:'), lower.indexOf('targets='));
  const source = idx >= 0 ? lower.slice(idx + 8) : lower;
  const result: string[] = [];
  source.split(/[,;|\s]+/).forEach((token) => {
    if (!token) return;
    if (['users', 'user', 'patient'].includes(token) && !result.includes('users')) {
      result.push('users');
    }
    if (['companions', 'companion'].includes(token) && !result.includes('companions')) {
      result.push('companions');
    }
  });
  return result.length ? result : ['users', 'companions'];
};

const formatDateTime = (value?: string) => {
  if (!value) return '-';
  return dayjs(value).format('YYYY-MM-DD HH:mm');
};

const refreshList = async () => {
  loading.value = true;
  try {
    const res = await listContentByPage({
      current: pagination.current,
      size: pagination.pageSize,
      keyword: keyword.value || undefined,
      type: activeTab.value,
    });
    const page = res.data || res;
    const records = (page.records || []).map((item: ContentItem) => ({
      ...item,
      publishTime: formatDateTime(item.publishTime),
      updateTime: formatDateTime(item.updateTime),
    }));
    tableData.value = records;
    pagination.total = page.total || 0;
  } catch (error: any) {
    message.error(error?.message || '加载失败');
  } finally {
    loading.value = false;
  }
};

const handleTabChange = () => {
  keyword.value = '';
  pagination.current = 1;
  refreshList();
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  refreshList();
};

const openCreateModal = () => {
  if (activeTab.value === 'article') {
    resetArticleForm();
    articleModalOpen.value = true;
    return;
  }
  resetAnnouncementForm();
  announcementModalOpen.value = true;
};

const openEditModal = (record: ContentItem) => {
  if (activeTab.value === 'article') {
    articleForm.id = Number(record.id);
    articleForm.title = record.title || '';
    articleForm.summary = record.summary || '';
    articleForm.tags = record.tags || '';
    articleForm.content = record.content || '';
    articleForm.status = (record.status as 'draft' | 'published') || 'draft';
    articleModalOpen.value = true;
    return;
  }
  announcementForm.id = Number(record.id);
  announcementForm.title = record.title || '';
  announcementForm.summary = record.summary || '';
  announcementForm.content = record.content || '';
  announcementForm.priority = record.priority || 1;
  announcementForm.targets = normalizeTargetsFromTags(record.tags);
  announcementForm.status = (record.status as 'draft' | 'published') || 'draft';
  announcementModalOpen.value = true;
};

const submitArticle = async () => {
  if (!articleForm.title.trim()) {
    message.warning('请填写文章标题');
    return;
  }
  if (!articleForm.content.trim()) {
    message.warning('请填写文章正文');
    return;
  }
  submitting.value = true;
  try {
    const payload: ContentItem = {
      title: articleForm.title.trim(),
      type: 'article',
      summary: articleForm.summary.trim() || undefined,
      tags: articleForm.tags.trim() || undefined,
      content: articleForm.content,
      status: articleForm.status,
    };
    if (articleForm.id) {
      await updateContent(articleForm.id, payload);
      message.success('文章更新成功');
    } else {
      await createContent(payload);
      message.success(articleForm.status === 'published' ? '文章发布成功' : '文章保存成功');
    }
    articleModalOpen.value = false;
    resetArticleForm();
    refreshList();
  } catch (error: any) {
    message.error(error?.message || '操作失败');
  } finally {
    submitting.value = false;
  }
};

const submitAnnouncement = async () => {
  if (!announcementForm.title.trim()) {
    message.warning('请填写公告标题');
    return;
  }
  if (!announcementForm.content.trim()) {
    message.warning('请填写公告正文');
    return;
  }
  if (!announcementForm.targets.length) {
    message.warning('请至少选择一个推送对象');
    return;
  }
  submitting.value = true;
  try {
    const payload: ContentItem = {
      title: announcementForm.title.trim(),
      type: 'announcement',
      summary: announcementForm.summary.trim() || undefined,
      content: announcementForm.content,
      priority: announcementForm.priority,
      tags: `targets:${announcementForm.targets.join(',')}`,
      status: announcementForm.status,
    };
    if (announcementForm.id) {
      await updateContent(announcementForm.id, payload);
      message.success('公告更新成功');
    } else {
      await createContent(payload);
      message.success(announcementForm.status === 'published' ? '公告发布成功' : '公告保存成功');
    }
    announcementModalOpen.value = false;
    resetAnnouncementForm();
    refreshList();
  } catch (error: any) {
    message.error(error?.message || '操作失败');
  } finally {
    submitting.value = false;
  }
};

const handlePublish = async (record: ContentItem) => {
  if (!record.id) return;
  try {
    await publishContent(Number(record.id));
    message.success('发布成功，已推送到 UniApp 通知');
    refreshList();
  } catch (error: any) {
    message.error(error?.message || '发布失败');
  }
};

const handleUnpublish = async (record: ContentItem) => {
  if (!record.id) return;
  try {
    await unpublishContent(Number(record.id));
    message.success('下架成功');
    refreshList();
  } catch (error: any) {
    message.error(error?.message || '下架失败');
  }
};

const handleDelete = async (record: ContentItem) => {
  if (!record.id) return;
  try {
    await deleteContent(Number(record.id));
    message.success('删除成功');
    refreshList();
  } catch (error: any) {
    message.error(error?.message || '删除失败');
  }
};

const markdownToHtml = (md: string) => {
  const escape = (text: string) =>
    text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');

  const inline = (line: string) =>
    line
      .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
      .replace(/__(.+?)__/g, '<strong>$1</strong>')
      .replace(/\*(.+?)\*/g, '<em>$1</em>')
      .replace(/_(.+?)_/g, '<em>$1</em>')
      .replace(/`(.+?)`/g, '<code>$1</code>')
      .replace(/\[(.+?)\]\((.+?)\)/g, '<a href="$2" target="_blank">$1</a>');

  const lines = escape(md || '').split('\n');
  const result: string[] = [];
  let inList = false;
  lines.forEach((raw) => {
    const line = raw.trim();
    if (!line) {
      if (inList) {
        result.push('</ul>');
        inList = false;
      }
      return;
    }
    if (/^#{1,6}\s/.test(line)) {
      if (inList) {
        result.push('</ul>');
        inList = false;
      }
      const level = line.match(/^#+/)?.[0].length || 1;
      result.push(`<h${level}>${inline(line.replace(/^#{1,6}\s/, ''))}</h${level}>`);
      return;
    }
    if (/^[-*]\s+/.test(line)) {
      if (!inList) {
        result.push('<ul>');
        inList = true;
      }
      result.push(`<li>${inline(line.replace(/^[-*]\s+/, ''))}</li>`);
      return;
    }
    if (inList) {
      result.push('</ul>');
      inList = false;
    }
    result.push(`<p>${inline(line)}</p>`);
  });
  if (inList) {
    result.push('</ul>');
  }
  return result.join('');
};

onMounted(() => {
  refreshList();
});
</script>

<style scoped>
.page-container {
  padding: 24px;
}

.summary-cell {
  display: inline-block;
  max-width: 280px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.md-preview {
  min-height: 336px;
  max-height: 420px;
  overflow: auto;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 12px;
  background: #fafafa;
}

:deep(.md-preview h1),
:deep(.md-preview h2),
:deep(.md-preview h3),
:deep(.md-preview h4),
:deep(.md-preview h5),
:deep(.md-preview h6) {
  margin: 8px 0;
}

:deep(.md-preview p) {
  margin: 8px 0;
  line-height: 1.7;
}

:deep(.md-preview ul) {
  padding-left: 20px;
}

:deep(.md-preview code) {
  background: #f1f3f6;
  border-radius: 4px;
  padding: 2px 6px;
}
</style>
