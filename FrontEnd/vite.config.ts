import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

/**
 * In development the Vite dev server proxies /api/** to the Spring Boot backend
 * running on port 8081. This eliminates cross-origin cookie issues: the browser
 * sees everything on localhost:5173, so the session cookie is set on the same origin.
 *
 * When VITE_API_BASE_URL is set (pointing directly at the backend) the proxy is
 * still active but the service.ts uses the full URL — both paths work.
 *
 * For production (Vercel) configure VITE_API_BASE_URL to the Render backend URL
 * and ensure CORS is set on the backend to allow that origin.
 */
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        // Forward cookies correctly so Spring Session works
        secure: false,
      },
    },
  },
});
