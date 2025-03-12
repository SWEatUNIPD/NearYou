import dotenv from 'dotenv'

dotenv.config({ path: './src/config/env-var.env' });

export const env = process.env;