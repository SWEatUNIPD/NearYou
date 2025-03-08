import { Kafka, Partitioners } from 'kafkajs';

const kafka = new Kafka({
    clientId: 'producer1',
    brokers: [process.env.BROKER ?? 'localhost:9094'],
});

const producer = kafka.producer({
    createPartitioner: Partitioners.DefaultPartitioner,
});

export async function connectProducer(): Promise<void> {
    await producer.connect();
}

export async function disconnectProducer(): Promise<void> {
    await producer.disconnect();
}

export async function sendMessage(topic: string, message: string): Promise<void> {
    producer.send({
        topic: topic,
        messages: [
            {
                value: message
            },
        ],
    });
}
