import { Kafka, Partitioners } from "kafkajs";

const kafka = new Kafka({
  clientId: "consumer1",
  brokers: ["localhost:9094"],
});

const consumer = kafka.consumer({ groupId: "consumers" });

await consumer.connect();
await consumer.subscribe({ topic: "flow-test", fromBeginning: true });

await consumer.run({
  eachMessage: async ({ topic, partition, message }) => {
    console.log({
      value: message.value.toString(),
    });
  },
});
