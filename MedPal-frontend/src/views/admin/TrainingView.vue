<template>
  <div class="page-container">
    <a-page-header
      title="培训课程管理"
      sub-title="管理陪诊员培训学习内容，发布后 UniApp 端可查看"
      @back="() => $router.back()"
    />

    <a-card :bordered="false" style="margin-top: 16px">
      <a-row :gutter="12" style="margin-bottom: 16px">
        <a-col :xs="24" :sm="10" :md="8">
          <a-input-search
            v-model:value="keyword"
            placeholder="搜索课程标题"
            allow-clear
            @search="refreshList"
          />
        </a-col>
        <a-col :xs="24" :sm="6" :md="4">
          <a-select v-model:value="status" allow-clear placeholder="状态" @change="refreshList" style="width: 100%">
            <a-select-option :value="1">已发布</a-select-option>
            <a-select-option :value="0">已下架</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="8" :md="12">
          <a-space>
            <a-button type="primary" @click="openCreateModal">新增课程</a-button>
            <a-button @click="refreshList">刷新</a-button>
          </a-space>
        </a-col>
      </a-row>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-key="(record: TrainingCourseItem) => record.id as number"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'orange'">
              {{ record.status === 1 ? '已发布' : '已下架' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'description'">
            <span class="summary-cell">{{ record.description || '-' }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openEditModal(record)">编辑</a-button>
              <a-button v-if="record.status !== 1" type="link" size="small" @click="handlePublish(record)">发布</a-button>
              <a-button v-else type="link" size="small" @click="handleUnpublish(record)">下架</a-button>
              <a-popconfirm title="确定删除该课程吗？" ok-text="确定" cancel-text="取消" @confirm="handleDelete(record)">
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="modalOpen"
      :title="form.id ? '编辑培训课程' : '新增培训课程'"
      width="980px"
      :confirm-loading="submitting"
      @ok="submitForm"
      @cancel="resetForm"
    >
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="课程标题" required>
              <a-input v-model:value="form.title" placeholder="请输入课程标题" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="课程分类">
              <a-input v-model:value="form.category" placeholder="如：沟通技巧/应急处理" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="讲师">
              <a-input v-model:value="form.instructor" placeholder="请输入讲师姓名" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="难度">
              <a-input-number v-model:value="form.difficulty" :min="1" :max="5" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="时长（分钟）">
              <a-input-number v-model:value="form.duration" :min="1" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="课程简介">
          <a-textarea v-model:value="form.description" :rows="2" placeholder="用于课程列表简介展示" />
        </a-form-item>
        <a-form-item label="课程内容" required>
          <a-textarea v-model:value="form.content" :rows="14" placeholder="请输入课程正文内容，可直接粘贴段落" />
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="form.status">
            <a-radio :value="1">立即发布</a-radio>
            <a-radio :value="0">保存为下架</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import {
  type TrainingCourseItem,
  createTrainingCourse,
  deleteTrainingCourse,
  listTrainingCourses,
  publishTrainingCourse,
  unpublishTrainingCourse,
  updateTrainingCourse,
} from '../../api';

const loading = ref(false);
const submitting = ref(false);
const modalOpen = ref(false);
const keyword = ref('');
const status = ref<number | undefined>(undefined);
const tableData = ref<TrainingCourseItem[]>([]);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
});

const form = reactive<TrainingCourseItem>({
  id: undefined,
  title: '',
  category: '',
  description: '',
  content: '',
  difficulty: 2,
  duration: 30,
  instructor: '',
  status: 1,
});

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title', width: 220 },
  { title: '分类', dataIndex: 'category', key: 'category', width: 120 },
  { title: '简介', dataIndex: 'description', key: 'description', width: 280 },
  { title: '讲师', dataIndex: 'instructor', key: 'instructor', width: 140 },
  { title: '时长', dataIndex: 'duration', key: 'duration', width: 100 },
  { title: '难度', dataIndex: 'difficulty', key: 'difficulty', width: 90 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 },
  { title: '操作', key: 'action', width: 220, fixed: 'right' as const },
];

const formatDateTime = (value?: string) => {
  if (!value) return '-';
  return dayjs(value).format('YYYY-MM-DD HH:mm');
};

const resetForm = () => {
  form.id = undefined;
  form.title = '';
  form.category = '';
  form.description = '';
  form.content = '';
  form.difficulty = 2;
  form.duration = 30;
  form.instructor = '';
  form.status = 1;
};

const refreshList = async () => {
  loading.value = true;
  try {
    const res = await listTrainingCourses({
      current: pagination.current,
      size: pagination.pageSize,
      keyword: keyword.value || undefined,
      status: status.value,
    });
    const page = res.data || res;
    tableData.value = (page.records || []).map((item: TrainingCourseItem) => ({
      ...item,
      updateTime: formatDateTime(item.updateTime),
    }));
    pagination.total = page.total || 0;
  } catch (error: any) {
    message.error(error?.message || '加载失败');
  } finally {
    loading.value = false;
  }
};

const openCreateModal = () => {
  resetForm();
  modalOpen.value = true;
};

const openEditModal = (record: TrainingCourseItem) => {
  form.id = record.id;
  form.title = record.title || '';
  form.category = record.category || '';
  form.description = record.description || '';
  form.content = record.content || '';
  form.difficulty = record.difficulty || 2;
  form.duration = record.duration || 30;
  form.instructor = record.instructor || '';
  form.status = record.status ?? 1;
  modalOpen.value = true;
};

const submitForm = async () => {
  if (!form.title?.trim()) {
    message.warning('请填写课程标题');
    return;
  }
  if (!form.content?.trim()) {
    message.warning('请填写课程内容');
    return;
  }
  submitting.value = true;
  try {
    const payload: TrainingCourseItem = {
      title: form.title.trim(),
      category: form.category?.trim() || undefined,
      description: form.description?.trim() || undefined,
      content: form.content,
      difficulty: form.difficulty,
      duration: form.duration,
      instructor: form.instructor?.trim() || undefined,
      status: form.status ?? 1,
    };
    if (form.id) {
      await updateTrainingCourse(Number(form.id), payload);
      message.success('课程更新成功');
    } else {
      await createTrainingCourse(payload);
      message.success(payload.status === 1 ? '课程发布成功' : '课程创建成功');
    }
    modalOpen.value = false;
    resetForm();
    refreshList();
  } catch (error: any) {
    message.error(error?.message || '操作失败');
  } finally {
    submitting.value = false;
  }
};

const handlePublish = async (record: TrainingCourseItem) => {
  if (!record.id) return;
  try {
    await publishTrainingCourse(Number(record.id));
    message.success('课程发布成功');
    refreshList();
  } catch (error: any) {
    message.error(error?.message || '发布失败');
  }
};

const handleUnpublish = async (record: TrainingCourseItem) => {
  if (!record.id) return;
  try {
    await unpublishTrainingCourse(Number(record.id));
    message.success('课程下架成功');
    refreshList();
  } catch (error: any) {
    message.error(error?.message || '下架失败');
  }
};

const handleDelete = async (record: TrainingCourseItem) => {
  if (!record.id) return;
  try {
    await deleteTrainingCourse(Number(record.id));
    message.success('课程删除成功');
    refreshList();
  } catch (error: any) {
    message.error(error?.message || '删除失败');
  }
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  refreshList();
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
  max-width: 260px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
