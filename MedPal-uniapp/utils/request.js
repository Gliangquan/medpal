const resolveBaseUrl = () => {
  if (typeof window !== 'undefined' && window.location?.origin) {
    return `${window.location.origin}/api`;
  }
  return 'http://127.0.0.1:9901/api';
};

const BASE_URL = resolveBaseUrl();
const TOKEN_KEY = 'medpal_token';

const showLoading = (show = true) => {
  if (show) {
    uni.showLoading({ title: '加载中...' });
  }
};

const hideLoading = (show = true) => {
  if (show) {
    uni.hideLoading();
  }
};

const buildQuery = (params = {}) => {
  const entries = Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== '');
  if (!entries.length) return '';
  const query = entries.map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`).join('&');
  return `?${query}`;
};

const request = ({ url, method = 'GET', data = {}, params = {}, header = {}, showLoading: loading = true }) => {
  return new Promise((resolve, reject) => {
    showLoading(loading);
    const token = uni.getStorageSync(TOKEN_KEY);
    const finalUrl = `${BASE_URL}${url}${buildQuery(params)}`;
    const body = method === 'GET' ? undefined : data;
    uni.request({
      url: finalUrl,
      method,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...header
      },
      data: body,
      success: (res) => {
        hideLoading(loading);
        const { statusCode, data: response } = res;
        if (statusCode === 401 || response?.code === 40100) {
          uni.clearStorageSync();
          uni.showToast({ title: '登录过期，请重新登录', icon: 'none' });
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/login/index' });
          }, 800);
          reject(new Error('未登录'));
          return;
        }
        if (statusCode >= 200 && statusCode < 300 && response) {
          if (response.code === 0) {
            resolve(response.data);
          } else {
            reject(new Error(response.message || '请求失败'));
          }
        } else {
          reject(new Error(response?.message || `请求失败 ${statusCode}`));
        }
      },
      fail: (error) => {
        hideLoading(loading);
        reject(error);
      }
    });
  });
};

const setToken = (token) => {
  if (token) {
    uni.setStorageSync(TOKEN_KEY, token);
  } else {
    uni.removeStorageSync(TOKEN_KEY);
  }
};

const getToken = () => uni.getStorageSync(TOKEN_KEY);

export { request, setToken, getToken, BASE_URL };
