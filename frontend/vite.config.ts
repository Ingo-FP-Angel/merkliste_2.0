import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
    // This changes the output dir from dist to build
    // comment this out if that isn't relevant for your project
    build: {
        outDir: 'build',
    },
    plugins: [react()],
    server: {
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
            },
        }
    }
})
