<template>
  <div class="page-container">
    <a-page-header
      title="陪诊认证审核"
      sub-title="审核陪诊员实名认证与资质认证"
      @back="() => $router.back()"
    />

    <a-card :bordered="false" style="margin-top: 24px">
      <a-row :gutter="12" style="margin-bottom: 16px">
        <a-col :xs="24" :sm="12" :md="8">
          <a-input-search
            v-model:value="keyword"
            placeholder="搜索昵称/账号/手机号"
            allow-clear
            @search="handleSearch"
          />
        </a-col>
        <a-col :xs="12" :sm="6" :md="4">
          <a-select v-model:value="realNameStatus" style="width: 100%" @change="handleSearch">
            <a-select-option value="">实名认证-全部</a-select-option>
            <a-select-option value="pending">实名认证-待审核</a-select-option>
            <a-select-option value="approved">实名认证-已通过</a-select-option>
            <a-select-option value="rejected">实名认证-已驳回</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="12" :sm="6" :md="4">
          <a-select v-model:value="qualificationStatus" style="width: 100%" @change="handleSearch">
            <a-select-option value="">资质认证-全部</a-select-option>
            <a-select-option value="pending">资质认证-待审核</a-select-option>
            <a-select-option value="approved">资质认证-已通过</a-select-option>
            <a-select-option value="rejected">资质认证-已驳回</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="24" :md="8">
          <a-space>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-col>
      </a-row>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        :row-key="(record: any) => record.id"
        :scroll="{ x: 1300 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'realNameStatus'">
            <a-tag :color="statusColor(record.realNameStatus)">{{ statusLabel(record.realNameStatus) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'qualificationStatus'">
            <a-tag :color="statusColor(record.qualificationStatus)">{{ statusLabel(record.qualificationStatus) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'idCard'">
            {{ maskIdCard(record.idCard) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openAuditModal(record)">查看/审核</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="auditModalVisible"
      title="认证审核详情"
      width="900px"
      :confirm-loading="submitting"
      @ok="submitAudit"
    >
      <template v-if="selectedUser">
        <a-descriptions bordered :column="2" size="small">
          <a-descriptions-item label="陪诊员ID">{{ selectedUser.id }}</a-descriptions-item>
          <a-descriptions-item label="昵称">{{ selectedUser.userName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="账号">{{ selectedUser.userAccount || '-' }}</a-descriptions-item>
          <a-descriptions-item label="手机号">{{ selectedUser.userPhone || '-' }}</a-descriptions-item>
          <a-descriptions-item label="身份证号" :span="2">{{ selectedUser.idCard || '-' }}</a-descriptions-item>
        </a-descriptions>

        <a-divider orientation="left">实名认证材料</a-divider>
        <a-row :gutter="12">
          <a-col :span="12">
            <div class="img-wrap">
              <div class="img-title">身份证正面</div>
              <img
                v-if="selectedUser.idCardFront"
                class="proof-img"
                :src="normalizeFileUrl(selectedUser.idCardFront)"
                alt="身份证正面"
              />
              <div v-else class="img-empty">未上传</div>
            </div>
          </a-col>
          <a-col :span="12">
            <div class="img-wrap">
              <div class="img-title">身份证反面</div>
              <img
                v-if="selectedUser.idCardBack"
                class="proof-img"
                :src="normalizeFileUrl(selectedUser.idCardBack)"
                alt="身份证反面"
              />
              <div v-else class="img-empty">未上传</div>
            </div>
          </a-col>
        </a-row>
        <a-form layout="vertical" style="margin-top: 12px">
          <a-form-item label="实名认证审核结果">
            <a-radio-group v-model:value="auditForm.realNameStatus">
              <a-radio-button value="pending">待审核</a-radio-button>
              <a-radio-button value="approved">通过</a-radio-button>
              <a-radio-button value="rejected">驳回</a-radio-button>
            </a-radio-group>
            <div style="margin-top: 8px; color: #999; font-size: 12px;">如陪诊员补充材料后重新提交，可重新设为待审核</div>
          </a-form-item>
        </a-form>

        <a-divider orientation="left">资质认证材料</a-divider>
        <a-descriptions bordered :column="2" size="small">
          <a-descriptions-item label="资质类型">{{ selectedUser.qualificationType || '-' }}</a-descriptions-item>
          <a-descriptions-item label="工作年限">{{ selectedUser.workYears || '-' }}</a-descriptions-item>
          <a-descriptions-item label="擅长方向" :span="2">{{ selectedUser.specialties || '-' }}</a-descriptions-item>
          <a-descriptions-item label="服务区域" :span="2">{{ selectedUser.serviceArea || '-' }}</a-descriptions-item>
        </a-descriptions>
        <div class="img-wrap" style="margin-top: 12px">
          <div class="img-title">资质证书</div>
          <img
            v-if="selectedUser.qualificationCert"
            class="proof-img"
            :src="normalizeFileUrl(selectedUser.qualificationCert)"
            alt="资质证书"
          />
          <div v-else class="img-empty">未上传</div>
        </div>
        <a-form layout="vertical" style="margin-top: 12px">
          <a-form-item label="资质认证审核结果">
            <a-radio-group v-model:value="auditForm.qualificationStatus">
              <a-radio-button value="pending">待审核</a-radio-button>
              <a-radio-button value="approved">通过</a-radio-button>
              <a-radio-button value="rejected">驳回</a-radio-button>
            </a-radio-group>
            <div style="margin-top: 8px; color: #999; font-size: 12px;">被驳回后重新提交的申请，也会重新进入待审核列表</div>
          </a-form-item>
        </a-form>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import {
  auditCompanionCertification,
  listCompanionCertificationByPage,
  type User
} from '../../api';
import { API_BASE_URL, toAbsoluteServiceUrl } from '../../config/service';

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '昵称', dataIndex: 'userName', key: 'userName', width: 120 },
  { title: '账号', dataIndex: 'userAccount', key: 'userAccount', width: 130 },
  { title: '手机号', dataIndex: 'userPhone', key: 'userPhone', width: 130 },
  { title: '身份证号', dataIndex: 'idCard', key: 'idCard', width: 170 },
  { title: '实名认证', dataIndex: 'realNameStatus', key: 'realNameStatus', width: 120 },
  { title: '资质认证', dataIndex: 'qualificationStatus', key: 'qualificationStatus', width: 120 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' as const },
];

const tableData = ref<User[]>([]);
const loading = ref(false);
const submitting = ref(false);
const keyword = ref('');
const realNameStatus = ref('');
const qualificationStatus = ref('');

const auditModalVisible = ref(false);
const selectedUser = ref<User | null>(null);
const auditForm = reactive({
  realNameStatus: 'pending',
  qualificationStatus: 'pending'
});

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  pageSizeOptions: ['10', '20'],
});

const statusLabel = (status?: string) => {
  if (status === 'approved') return '已通过';
  if (status === 'rejected') return '已驳回';
  if (status === 'pending') return '待审核';
  return '未提交';
};

const statusColor = (status?: string) => {
  if (status === 'approved') return 'success';
  if (status === 'rejected') return 'error';
  if (status === 'pending') return 'processing';
  return 'default';
};

const maskIdCard = (idCard?: string) => {
  if (!idCard) return '-';
  if (idCard.length < 8) return idCard;
  return `${idCard.slice(0, 4)}********${idCard.slice(-4)}`;
};

const normalizeFileUrl = (url?: string) => {
  if (!url) return '';
  if (/^https?:\/\//.test(url)) return url;
  if (url.startsWith('/api/')) return toAbsoluteServiceUrl(url);
  const match = url.match(/^\/files\/([^/]+)\/([^/]+)\/(.+)$/);
  if (match) {
    const [, biz, userId, filename] = match;
    return `${API_BASE_URL}/file/preview/${biz}/${userId}/${filename}`;
  }
  return toAbsoluteServiceUrl(url);
};

const fetchData = async () => {
  loading.value = true;
  try {
    const response = await listCompanionCertificationByPage({
      current: pagination.current,
      pageSize: pagination.pageSize,
      keyword: keyword.value || undefined,
      realNameStatus: realNameStatus.value || undefined,
      qualificationStatus: qualificationStatus.value || undefined,
    });
    if (response.code === 0) {
      tableData.value = response.data.records || [];
      pagination.total = response.data.total || 0;
    } else {
      message.error(response.message || '获取审核列表失败');
    }
  } catch (error: any) {
    message.error(error?.message || '获取审核列表失败');
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.current = 1;
  fetchData();
};

const handleReset = () => {
  keyword.value = '';
  realNameStatus.value = '';
  qualificationStatus.value = '';
  handleSearch();
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchData();
};

const openAuditModal = (record: User) => {
  selectedUser.value = record;
  auditForm.realNameStatus = record.realNameStatus || 'pending';
  auditForm.qualificationStatus = record.qualificationStatus || 'pending';
  auditModalVisible.value = true;
};

const submitAudit = async () => {
  if (!selectedUser.value) return;

  const payload: any = { userId: selectedUser.value.id };
  if (auditForm.realNameStatus !== selectedUser.value.realNameStatus) {
    payload.realNameStatus = auditForm.realNameStatus;
  }
  if (auditForm.qualificationStatus !== selectedUser.value.qualificationStatus) {
    payload.qualificationStatus = auditForm.qualificationStatus;
  }
  if (!payload.realNameStatus && !payload.qualificationStatus) {
    message.warning('请至少修改一项审核状态');
    return;
  }
  if (payload.realNameStatus === 'pending' || payload.qualificationStatus === 'pending') {
    message.warning('审核结果只能设置为“通过”或“驳回”');
    return;
  }

  submitting.value = true;
  try {
    const response = await auditCompanionCertification(payload);
    if (response.code === 0) {
      message.success('审核提交成功');
      auditModalVisible.value = false;
      fetchData();
    } else {
      message.error(response.message || '审核提交失败');
    }
  } catch (error: any) {
    message.error(error?.message || '审核提交失败');
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.page-container {
  padding: 24px;
}

.img-wrap {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 12px;
  background: #fafafa;
}

.img-title {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.proof-img {
  width: 100%;
  max-height: 240px;
  object-fit: contain;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
}

.img-empty {
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  color: #999;
}
</style>
