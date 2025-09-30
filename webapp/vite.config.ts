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
});
