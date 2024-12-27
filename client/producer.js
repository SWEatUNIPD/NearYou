import { Kafka, Partitioners } from "kafkajs";
import fs from "fs";
import { TrackSimulator } from "./tracks-simulator.js";

const kafka = new Kafka({
  clientId: "producer1",
  brokers: ["localhost:9094"],
});

const producer = kafka.producer({
  createPartitioner: Partitioners.DefaultPartitioner,
  transactionTimeout: 5,
});

// const serializeObject = (obj) => {
//     return Buffer.from(JSON.stringify(obj));
// };

const sendMessage = async (sensorId, retrievedTime, lat, lon) => {
  return (
    producer
      .send({
        topic: "gps-data",
        messages: [
          {
            value: JSON.stringify({
              sensor_id: sensorId,
              retrieved_time: retrievedTime,
              latitude: lat,
              longitude: lon,
            }),
          },
        ],
      })
      // .then(console.log)   // usefull for degubbing
      .catch((e) => console.error(`[client/producer] ${e.message}`, e))
  );
};

async function run() {
  // const activeSensorIdList = [1];
  // const trkSim = new TrackSimulator(activeSensorIdList);
  // await trkSim.run();

  const sensorDataFile = fs.readFileSync("./sensor_data/sensor-1.txt", "utf-8");
  const lines = sensorDataFile.split(/\r?\n/);
  let currentIndex = 0;
  await producer.connect();
  const intervalId = setInterval(() => {
    if (currentIndex < lines.length - 1) {
      const values = lines[currentIndex].split(",");
      sendMessage(values[0], values[1], values[2], values[3]);
      currentIndex++;
    } else {
      clearInterval(intervalId);
    }
  }, 3000);
}

run().catch((e) => console.error(`[client/producer] ${e.message}`, e));

await producer.disconnect();
