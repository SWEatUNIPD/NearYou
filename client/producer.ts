import { Kafka, Partitioners } from 'kafkajs';
import fs from 'fs';
import { TrackSimulator } from './tracks-simulator';

const kafka: Kafka = new Kafka({
  clientId: 'producer1',
  brokers: ['localhost:9094'],
});

const producer = kafka.producer({
  createPartitioner: Partitioners.DefaultPartitioner,
});

const sendMessage = async (
  sensorId: number,
  rentId: number,
  latitude: number,
  longitude: number
) => {
  return producer.send({
    topic: 'gps-data',
    messages: [
      {
        value: JSON.stringify({
          sensorId,
          rentId,
          latitude,
          longitude,
        }),
      },
    ],
  });
};

async function run() {
  // const trkSim = new TrackSimulator(1);
  // await trkSim.run();

  const sensorDataFile = fs.readFileSync(
    `./sensor_data/sensor-${1}.txt`,
    'utf-8'
  );
  const lines: string[] = sensorDataFile.split(/\r?\n/);
  let currentIndex = 0;
  await producer.connect();
  const intervalId = setInterval(() => {
    if (currentIndex < lines.length - 1) {
      const values = lines[currentIndex].split(',');
      sendMessage(1, 1, Number(values[0]), Number(values[1]));
      currentIndex++;
    } else {
      clearInterval(intervalId);
    }
  }, 3000);
}

run().catch((e) => console.error(`[client/producer] ${e.message}`, e));

await producer.disconnect();
