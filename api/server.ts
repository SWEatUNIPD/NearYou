import express from 'express'
import postgres from 'postgres';
import {v4 as uuidv4} from 'uuid';

const app = express()
const port = 9000
const sql = postgres({
    host: process.env.DB_HOST ?? 'localhost',
    port: 5432,
    database: process.env.DB_NAME ?? 'admin',
    username: process.env.DB_USER ?? 'admin',
    password: process.env.DB_PASSWORD ?? 'adminadminadmin',
})

app.get('/start-rent/:userId/:bikeId', async (req, res) => {
    res.status(200).send(JSON.stringify({"id":uuidv4()}));
})

app.get('/close-rent/:rentId', async (req, res) => {
    const rentId = req.params.rentId;
    try {
        await sql`
        UPDATE rents SET is_closed = true WHERE id = ${rentId}`;
        res.status(200).send("Rent closed successfully.");
        return;
    } catch (e) {
        res.status(500).send("An error occured");
    }
});

app.listen(port, () => {
    console.log(`API Gateway running on port ${port}`)
})