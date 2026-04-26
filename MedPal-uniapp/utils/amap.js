const H5_AMAP_KEY = '75684cb61e55ed808e8b66d173e60270';
const H5_AMAP_SECURITY_CODE = '53373ecd1961febbc11a2abc54e40d7f';
const WX_AMAP_KEY = '2f28e2a27dd14749e7e020c69cfb1320';

const DEFAULT_CENTER = {
  latitude: 39.90923,
  longitude: 116.397428
};

let h5LoaderPromise = null;

const formatCoords = (latitude, longitude) => {
  if (typeof latitude !== 'number' || typeof longitude !== 'number') {
    return '';
  }
  return `${latitude.toFixed(4)}, ${longitude.toFixed(4)}`;
};

const loadAmapSdk = async () => {
  if (typeof window === 'undefined') {
    return null;
  }
  if (window.AMap) {
    return window.AMap;
  }
  if (h5LoaderPromise) {
    return h5LoaderPromise;
  }

  h5LoaderPromise = new Promise((resolve, reject) => {
    window._AMapSecurityConfig = {
      securityJsCode: H5_AMAP_SECURITY_CODE
    };

    const existingScript = document.getElementById('medpal-amap-sdk');
    if (existingScript) {
      existingScript.addEventListener('load', () => resolve(window.AMap));
      existingScript.addEventListener('error', () => reject(new Error('高德地图脚本加载失败')));
      return;
    }

    const script = document.createElement('script');
    script.id = 'medpal-amap-sdk';
    script.async = true;
    script.defer = true;
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${H5_AMAP_KEY}&plugin=AMap.Geocoder,AMap.Scale,AMap.ToolBar`;
    script.onload = () => {
      if (window.AMap) {
        resolve(window.AMap);
        return;
      }
      reject(new Error('高德地图加载失败'));
    };
    script.onerror = () => reject(new Error('高德地图脚本加载失败'));
    document.head.appendChild(script);
  });

  return h5LoaderPromise;
};

export {
  H5_AMAP_KEY,
  H5_AMAP_SECURITY_CODE,
  WX_AMAP_KEY,
  DEFAULT_CENTER,
  formatCoords,
  loadAmapSdk
};
