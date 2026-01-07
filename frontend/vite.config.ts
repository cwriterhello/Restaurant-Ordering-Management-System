import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  define: {
    global: 'globalThis',
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      // 为了解决 sockjs-client 中的 global 问题
      buffer: 'buffer',
      process: 'process',
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8082',  // 修正为实际的后端端口
        changeOrigin: true
      },
      '/ws': {
        target: 'ws://localhost:8082',   // 修正为实际的后端端口
        ws: true
      }
    }
  }
})