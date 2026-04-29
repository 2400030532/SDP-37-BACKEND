import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],

  // Dev server (ONLY for local development)
  server: {
    proxy: {
      '/api': {
        target: 'https://sdp-37-backend-1.onrender.com/api/v1',
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