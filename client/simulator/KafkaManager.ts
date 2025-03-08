import { Consumer, ConsumerConfig, EachMessagePayload, Kafka, KafkaConfig, Message, Producer } from "kafkajs";

export class KafkaManager {
    private static instance: KafkaManager;
    private kafka: Kafka;

    private constructor() {
        const kafkaConfig: KafkaConfig = {
            clientId: 'simulator',
            brokers: [process.env.BROKER ?? 'localhost:9094']
        };

        this.kafka = new Kafka(kafkaConfig);
    }

    static getInstance(): KafkaManager {
        if (this.instance == null) {
            this.instance = new KafkaManager();
        }
        return this.instance;
    }

    async initAndConnectProducer(): Promise<Producer> {
        try {
            let producer = this.kafka.producer();
            await producer.connect();
            return producer;
        } catch (error) {
            console.error('Failed to connect Kafka producer: ', error);
        }
    }

    async disconnectProducer(producer: Producer): Promise<void> {
        try {
            await producer.disconnect();
        } catch (error) {
            console.error('Failed to disconnect Kafka producer: ', error);
        }
    }

    async sendMessage(producer: Producer, topic: string, data: string): Promise<void> {
        try {
            const msg: Message = {
                value: data
            }
            await producer.send({
                topic: topic,
                messages: [msg]
            })
        } catch (error) {
            console.error('Failed to send message from Kafka producer: ', error);
        }
    }

    async initAndConnectConsumer(topic: string, groupId: string, eachMessageHandler: (payload: EachMessagePayload) => Promise<void>): Promise<Consumer> {
        try {
            const consumerConfig: ConsumerConfig = {
                groupId: groupId
            };
            const consumer = this.kafka.consumer(consumerConfig);
            await consumer.connect();
            await consumer.subscribe({
                topic,
                fromBeginning: true
            });

            await consumer.run({
                eachMessage: async (payload) => {
                    await eachMessageHandler(payload);
                },
            });
            return consumer;
        } catch (error) {
            console.error('Failed to connect Kafka consumer: ', error);
        }
    }

    async disconnectConsumer(consumer: Consumer): Promise<void> {
        try {
            await consumer.disconnect();
        } catch (error) {
            console.error('Failed to disconnect Kafka consumer: ', error);
        }
    }
}
