import express from 'express'
import postgres from 'postgres';

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
    const userId = req.params.userId
    const bikeId = req.params.bikeId

    const opened_rent = await sql`SELECT * FROM rents WHERE user_id = ${userId} AND is_closed = false`;
    if (opened_rent.count > 0) {
        res.status(418).send("User has an opened rent at the moment.");
        return;
    }

    const rented_bike = await sql`SELECT * FROM rents WHERE bike_id = ${bikeId} AND is_closed = false`;
    if (rented_bike.count > 0) {
        res.status(418).send("Bike is already rented.");
        return;
    }

    try {
        const rent = await sql`INSERT INTO rents (bike_id, user_id) VALUES (${bikeId}, ${userId}) RETURNING id`;
        res.send(rent.at(0));
        return;
    } catch (e) {
        res.status(404).send("The bike or the user were not found.");
    }
})

app.get('/close-rent/:rentId', async (req, res) => {
    const rentId = req.params.rentId;
    try {
        await sql`UPDATE rents SET is_closed = true WHERE id = ${rentId}`;
        res.status(200).send("Rent closed successfully.");
        return;
    } catch (e) {
        res.status(500).send("An error occured");
    }
});

app.listen(port, () => {
    console.log(`API Gateway running on port ${port}`)
})
