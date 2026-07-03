import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    proxy: {
      '/api/v1/payments': 'http://localhost:8081',
      '/api/v1/accounts': 'http://localhost:8081',
      '/api/v1/ledger': 'http://localhost:8082',
      '/api/v1/fraud': 'http://localhost:8083',
      '/api/v1/webhooks': 'http://localhost:8084',
      '/api/v1/reconciliation': 'http://localhost:8085',
      '/api/v1/disputes': 'http://localhost:8085',
    },
  },
})
