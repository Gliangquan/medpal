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
    port: 9991,
    proxy: {
      '/api': {
        target: 'http://localhost:9901',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api'),
      },
      '/ws': {
        target: 'http://localhost:9901',
        ws: true,
        changeOrigin: true,
      },
    },
  },
});
