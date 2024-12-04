import { Kafka, Partitioners } from "kafkajs";

const kafka = new Kafka({
  clientId: "producer1",
  brokers: ["localhost:9094"],
});

const producer = kafka.producer({
  createPartitioner: Partitioners.DefaultPartitioner,
  transactionTimeout: 5,
});

await producer.connect();
await producer.send({
  topic: "gps-data",
  messages: [
    {
      value: "Padova",
    },
  ],
});

await producer.disconnect();
