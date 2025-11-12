// Corrige problemas com TextEncoder/TextDecoder no Node 22+
import { TextEncoder, TextDecoder } from "node:util";

(globalThis as any).TextEncoder = TextEncoder;
(globalThis as any).TextDecoder = TextDecoder;
