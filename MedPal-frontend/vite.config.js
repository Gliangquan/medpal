import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  define: {
    global: 'window',
    process: 'window.process',
    Buffer: 'window.Buffer',
  },
  server: {
    host: '0.0.0.0',
    port: 19912,
    proxy: {
      '/api': {
        target: 'http://localhost:19911',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api'),
      },
      '/ws': {
        target: 'http://localhost:19911',
        ws: true,
        changeOrigin: true,
      },
    },
  },
});
