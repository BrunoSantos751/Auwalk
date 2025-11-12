import { createRequire } from "module";
const require = createRequire(import.meta.url);

export default {
  preset: "ts-jest/presets/js-with-ts-esm", // ESM + TS
  testEnvironment: "jsdom",
  setupFiles: ["<rootDir>/jest.setup-env.ts"], // setup inicial (TextEncoder/TextDecoder)
  setupFilesAfterEnv: [
    "@testing-library/jest-dom", // matchers da Testing Library
    "<rootDir>/jest.setup-after-env.ts", // cleanup ou outras configs pós-env
  ],
  transform: {
    "^.+\\.(ts|tsx)$": require.resolve("ts-jest"), // transforma TS/TSX
  },
  moduleFileExtensions: ["ts", "tsx", "js", "jsx"],
  testMatch: ["**/?(*.)+(spec|test).[tj]s?(x)"],
  moduleNameMapper: {
    "\\.(css|less|scss|sass)$": "identity-obj-proxy", // ignora CSS
    "\\.(png|jpg|jpeg|webp|svg)$": "<rootDir>/__mocks__/fileMock.js", // ignora imagens
  },
};
