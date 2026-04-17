import { request, setToken, getToken, BASE_URL } from '@/utils/request';

const userApi = {
  login(payload) {
    return request({
      url: '/user/login',
      method: 'POST',
      data: payload
    });
  },
  wechatLogin(payload) {
    return request({
      url: '/user/login/wechat',
      method: 'POST',
      data: payload
    });
  },
  register(payload) {
    return request({
      url: '/user/register',
      method: 'POST',
      data: payload
    });
  },
  resetPassword(payload) {
    return request({
      url: '/user/reset-password',
      method: 'POST',
      data: payload
    });
  },
  changePassword(payload) {
    return request({
      url: '/user/change-password',
      method: 'POST',
      data: payload
    });
  },
  fetchCurrentUser() {
    return request({
      url: '/user/get/login',
      method: 'GET'
    });
  },
  updateProfile(payload) {
    return request({
      url: '/user/update/my',
      method: 'POST',
      data: payload
    });
  },
  submitCompanionCertification(payload) {
    return request({
      url: '/user/update/my',
      method: 'POST',
      data: payload
    });
  },
  logout() {
    return request({
      url: '/user/logout',
      method: 'POST'
    });
  }
};

const hospitalApi = {
  list(params) {
    return request({
      url: '/hospital/list',
      method: 'GET',
      params
    });
  },
  detail(id) {
    return request({
      url: `/hospital/${id}`,
      method: 'GET'
    });
  },
  search(keyword) {
    return request({
      url: '/hospital/search',
      method: 'POST',
      data: { keyword }
    });
  }
};

const departmentApi = {
  list(params) {
    return request({
      url: '/department/list',
      method: 'GET',
      params
    });
  }
};

const doctorApi = {
  list(params) {
    return request({
      url: '/doctor/list',
      method: 'GET',
      params
    });
  }
};

const orderApi = {
  list(params) {
    // 如果有 companionId，使用 list-by-companion 接口
    if (params.companionId) {
      return request({
        url: '/order/list-by-companion',
        method: 'GET',
        params
      });
    }
    return request({
      url: '/order/list',
      method: 'GET',
      params
    });
  },
  get(id) {
    return request({
      url: `/order/${id}`,
      method: 'GET'
    });
  },
  create(payload) {
    return request({
      url: '/order/create',
      method: 'POST',
      data: payload
    });
  },
  saveDraft(payload) {
    return request({
      url: '/order/draft/save',
      method: 'POST',
      data: payload
    });
  },
  listDrafts(params) {
    return request({
      url: '/order/draft/list',
      method: 'GET',
      params
    });
  },
  submitDraft(id) {
    return request({
      url: `/order/draft/submit/${id}`,
      method: 'POST'
    });
  },
  cancel(id, reason) {
    return request({
      url: `/order/cancel/${id}`,
      method: 'POST',
      params: { reason }
    });
  },
  accept(id, companionId) {
    return request({
      url: `/order/accept/${id}`,
      method: 'POST',
      params: { companionId }
    });
  },
  reject(id, reason) {
    return request({
      url: `/order/reject/${id}`,
      method: 'POST',
      params: { reason }
    });
  },
  complete(id) {
    return request({
      url: `/order/complete/${id}`,
      method: 'POST'
    });
  },
  confirmComplete(id) {
    return request({
      url: `/order/confirm-complete/${id}`,
      method: 'POST'
    });
  }
};

const notificationApi = {
  list(params) {
    return request({
      url: '/notification/list',
      method: 'GET',
      params
    });
  },
  unreadCount(userId) {
    return request({
      url: '/notification/unread-count',
      method: 'GET',
      params: { userId }
    });
  },
  markAsRead(id) {
    return request({
      url: `/notification/read/${id}`,
      method: 'POST'
    });
  },
  markAllAsRead(userId) {
    return request({
      url: '/notification/read-all',
      method: 'POST',
      params: { userId }
    });
  }
};

const companionApi = {
  list(params) {
    return request({
      url: '/user/companion/list',
      method: 'GET',
      params
    });
  },
  detail(id) {
    return request({
      url: `/user/companion/${id}`,
      method: 'GET'
    });
  }
};

const evaluationApi = {
  list(params) {
    return request({
      url: '/evaluation/list',
      method: 'GET',
      params
    });
  },
  create(payload) {
    return request({
      url: '/evaluation/create',
      method: 'POST',
      data: payload
    });
  }
};

const recordApi = {
  list(params) {
    return request({
      url: '/medical-record/list',
      method: 'GET',
      params
    });
  },
  detail(id) {
    return request({
      url: `/medical-record/${id}`,
      method: 'GET'
    });
  }
};

const emergencyApi = {
  create(payload) {
    return request({
      url: '/emergency/create',
      method: 'POST',
      data: payload
    });
  },
  list(params) {
    return request({
      url: '/emergency/list',
      method: 'GET',
      params
    });
  },
  detail(id) {
    return request({
      url: `/emergency/${id}`,
      method: 'GET'
    });
  },
  respond(id) {
    return request({
      url: `/emergency/respond/${id}`,
      method: 'POST'
    });
  },
  resolve(id, resolveNote) {
    return request({
      url: `/emergency/resolve/${id}`,
      method: 'POST',
      params: { resolveNote }
    });
  }
};

const settingsApi = {
  get(userId) {
    return request({
      url: '/user-settings/get',
      method: 'GET',
      params: { userId }
    });
  },
  updateNotification(userId, payload) {
    return request({
      url: '/user-settings/notification/update',
      method: 'POST',
      params: { userId },
      data: payload
    });
  },
  updatePrivacy(userId, payload) {
    return request({
      url: '/user-settings/privacy/update',
      method: 'POST',
      params: { userId },
      data: payload
    });
  }
};

const paymentApi = {
  create({ orderId, amount, channel = 'mock', description }) {
    return request({
      url: '/payment/create',
      method: 'POST',
      params: { orderId, amount, channel, description }
    });
  },
  mockPay(paymentId) {
    return request({
      url: '/payment/mock-pay',
      method: 'POST',
      params: { paymentId }
    });
  }
};

const contentApi = {
  list(params) {
    return request({
      url: '/content/published/list',
      method: 'GET',
      params
    });
  },
  detail(id) {
    return request({
      url: `/content/published/${id}`,
      method: 'GET'
    });
  }
};

const fileApi = {
  upload(filePath, biz = 'user_avatar', name = 'file') {
    return new Promise((resolve, reject) => {
      const token = getToken();
      uni.uploadFile({
        url: `${BASE_URL}/file/upload`,
        filePath,
        name,
        formData: { biz },
        header: {
          ...(token ? { Authorization: `Bearer ${token}` } : {})
        },
        success: (res) => {
          try {
            const data = typeof res.data === 'string' ? JSON.parse(res.data || '{}') : (res.data || {});
            if (res.statusCode >= 200 && res.statusCode < 300 && data.code === 0) {
              resolve(data.data);
              return;
            }
            reject(new Error(data.message || '上传失败'));
          } catch (error) {
            reject(new Error('上传响应解析失败'));
          }
        },
        fail: (error) => reject(error)
      });
    });
  }
};

export {
  userApi,
  hospitalApi,
  departmentApi,
  doctorApi,
  orderApi,
  notificationApi,
  companionApi,
  evaluationApi,
  recordApi,
  emergencyApi,
  settingsApi,
  paymentApi,
  contentApi,
  fileApi,
  setToken
};
