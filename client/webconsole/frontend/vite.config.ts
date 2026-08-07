import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react-swc'

// https://vite.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        // 開発時はbackend(webconsole自身、既定ポート9090)へ/testtool/**をproxyし、CORS設定なしで動作させる。
        proxy: {
            "/testtool": "http://localhost:9090",
        },
    },
})
