import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  base: "/Chicken_Farm_Management_System/",
  plugins: [react()],
})
