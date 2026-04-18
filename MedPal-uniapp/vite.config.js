import { defineConfig } from 'vite';
import uni from '@dcloudio/vite-plugin-uni';

export default defineConfig({
  plugins: [uni()],
  server: {
    host: '0.0.0.0',
    port: 19913,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:19911',
        changeOrigin: true,
      },
      '/ws': {
        target: 'ws://127.0.0.1:19911',
        ws: true,
        changeOrigin: true,
      },
    },
  },
  preview: {
    host: '0.0.0.0',
    port: 19913,
  },
  define: {
    'process.env.NODE_ENV': JSON.stringify(process.env.NODE_ENV || 'production'),
  },
});
