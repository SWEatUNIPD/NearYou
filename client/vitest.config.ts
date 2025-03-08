import { defineConfig } from 'vitest/config';

export default defineConfig({
    test: {
        globals: true,
        // environment: 'node', ???

        coverage: {
            reporter: ['text', 'json', 'html'],

            exclude: [
                '.nvmrc',
                '.prettierrc',
                'Dockerfile',
                'env-var.env',
                'package-lock.json',
                'package.json',
                'tsconfig.json',
                'vitest.config.ts',
                'node_modules/**',
                'test/**',
                'coverage/**',
            ],

            thresholds: {
                lines: 80,
                functions: 80,
                branches: 80,
                statements: 80,
            },
        },
    },
});
