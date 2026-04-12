const ROLE_PATIENT = 'patient';
const ROLE_COMPANION = 'companion';
const ROLE_ADMIN = 'admin';
const ROLE_UNKNOWN = 'unknown';

const normalizeRole = (input) => {
  const role = typeof input === 'string' ? input : input?.userRole;
  if (role === 'patient' || role === 'user') return ROLE_PATIENT;
  if (role === ROLE_COMPANION) return ROLE_COMPANION;
  if (role === ROLE_ADMIN) return ROLE_ADMIN;
  return ROLE_UNKNOWN;
};

const isPatientRole = (input) => normalizeRole(input) === ROLE_PATIENT;

const isCompanionRole = (input) => normalizeRole(input) === ROLE_COMPANION;

const normalizeCertStatus = (status) => {
  if (!status) return 'not_submitted';
  const normalized = String(status).trim().toLowerCase();
  if (['approved', 'approve', 'pass', 'passed', 'success', 'done', '通过', '已通过'].includes(normalized)) {
    return 'approved';
  }
  if (['pending', 'reviewing', 'in_review', 'processing', '审核中', '待审核'].includes(normalized)) {
    return 'pending';
  }
  if (['rejected', 'reject', 'failed', '不通过', '已驳回'].includes(normalized)) {
    return 'rejected';
  }
  return 'not_submitted';
};

const isCompanionCertified = (user) => {
  if (!isCompanionRole(user)) return false;
  return normalizeCertStatus(user?.realNameStatus) === 'approved'
    && normalizeCertStatus(user?.qualificationStatus) === 'approved';
};

const certStatusText = (status) => {
  const normalized = normalizeCertStatus(status);
  if (normalized === 'approved') return '已认证';
  if (normalized === 'pending') return '审核中';
  if (normalized === 'rejected') return '已驳回';
  return '未提交';
};

export {
  ROLE_PATIENT,
  ROLE_COMPANION,
  ROLE_ADMIN,
  ROLE_UNKNOWN,
  normalizeRole,
  isPatientRole,
  isCompanionRole,
  normalizeCertStatus,
  isCompanionCertified,
  certStatusText
};
