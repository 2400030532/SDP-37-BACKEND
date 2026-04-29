import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],

  // Dev server (ONLY for local development)
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // local backend
        changeOrigin: true,
      },
    },
    port: 5173,
    host: true,
  },

  // Build settings (for Render deployment)
  build: {
    outDir: 'dist',
    sourcemap: false,
  }
})