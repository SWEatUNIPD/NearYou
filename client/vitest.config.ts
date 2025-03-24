import { defineConfig } from 'vitest/config';

export default defineConfig({
    test: {
        globals: true,

        coverage: {
            reporter: ['text', 'json', 'html'],

            include: [
                'src/**',
            ],

            exclude: [
                'src/config/**',
                'src/App.ts',
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
