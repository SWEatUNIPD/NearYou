import dotenv from 'dotenv'

// Carica le variabili di ambiente dal file .env
dotenv.config({ path: './env-var.env' });

export const env = process.env;