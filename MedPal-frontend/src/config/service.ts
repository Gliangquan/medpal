const DEFAULT_SERVICE_ORIGIN = typeof window !== 'undefined'
  ? window.location.origin
  : 'http://localhost:19911';

const envServiceOrigin = import.meta.env.VITE_SERVICE_ORIGIN;
const rawServiceOrigin = typeof envServiceOrigin === 'string' && envServiceOrigin.trim()
  ? envServiceOrigin
  : DEFAULT_SERVICE_ORIGIN;

export const SERVICE_ORIGIN = rawServiceOrigin.replace(/\/+$/, '');
export const API_BASE_URL = `${SERVICE_ORIGIN}/api`;
export const WS_BASE_URL = `${SERVICE_ORIGIN}/ws`;

export function toAbsoluteServiceUrl(path: string): string {
  if (/^https?:\/\//.test(path) || path.startsWith('data:') || path.startsWith('blob:')) {
    return path;
  }

  if (path.startsWith('/')) {
    return `${SERVICE_ORIGIN}${path}`;
  }

  return `${SERVICE_ORIGIN}/${path}`;
}
