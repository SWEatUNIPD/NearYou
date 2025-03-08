import dotenv from 'dotenv'

dotenv.config({ path: './env-var.env' });

export const env = process.env;