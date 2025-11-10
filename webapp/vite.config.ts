import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

export default defineConfig({
  root: ".",
  plugins: [react()],
  resolve: {
    alias: {
      src: path.resolve(__dirname, "src"),
    },
  },
  assetsInclude: ["**/*.webp"],
  build: {
    outDir: "dist",
    emptyOutDir: true,
  },
  server: {
    port: 3000,
  },
  test: {
    globals: true, // permite usar 'describe', 'test', 'expect' sem importar
    environment: "jsdom", // simula o DOM do navegador
    setupFiles: "./src/setupTests.ts", // arquivo de setup
    include: ["src/**/*.test.{ts,tsx}"], // onde ficam os testes
    coverage: {
       provider: "v8",
      reporter: ["text", "json", "html"], // relatórios de cobertura
    },
  },
});
