// API 基础配置
const defaultServiceOrigin = typeof window !== 'undefined'
  ? window.location.origin
  : 'http://localhost:9901';
const envServiceOrigin = import.meta.env.VITE_SERVICE_ORIGIN;
const serviceOrigin = (
  typeof envServiceOrigin === 'string' && envServiceOrigin.trim()
    ? envServiceOrigin
    : defaultServiceOrigin
).replace(/\/+$/, '');
const API_BASE_URL = `${serviceOrigin}/api`;

export default {
  API_BASE_URL,
};
