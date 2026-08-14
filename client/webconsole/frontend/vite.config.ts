import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    // make-you-chic-uiはgit submodule(vendor/)をfile:参照するsymlinkのため、
    // Viteが素朴に解決すると vendor/make-you-chic-ui/node_modules/react
    // (デザインシステム自身のdevDependency)を拾ってしまい、
    // frontend側のreactと二重にロードされてフックエラーになる。
    // dedupeで常にこのプロジェクト直下のreact/react-domへ強制的に一本化する。
    dedupe: ['react', 'react-dom'],
  },
  server: {
    // 開発時はbackend(webconsole自身、既定ポート9090)へ/testtool/**をproxyし、CORS設定なしで動作させる。
    proxy: {
      '/testtool': 'http://localhost:9090',
    },
  },
})
