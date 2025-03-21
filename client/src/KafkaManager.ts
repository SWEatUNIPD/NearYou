import { injectable } from 'inversify';
import { Consumer, ConsumerConfig, EachMessagePayload, Kafka, Message, Producer } from "kafkajs";

@injectable()
export class KafkaManager {
    constructor(
        private kafka: Kafka
    ) {}

    async initAndConnectProducer(): Promise<Producer> {
        try {
            let producer = this.kafka.producer();
            await producer.connect();
            return producer;
        } catch (error) {
            throw new Error(
                `Failed to connect Kafka producer: ${error}`
            );
        }
    }

    async disconnectProducer(producer: Producer): Promise<void> {
        try {
            await producer.disconnect();
        } catch (error) {
            throw new Error(
                `Failed to disconnect Kafka producer: ${error}`
            );
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
            throw new Error(
                `Failed to send message from Kafka producer: ${error}`
            );
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
            throw new Error(
                `Failed to connect Kafka consumer: ${error}`
            );
        }
    }

    async disconnectConsumer(consumer: Consumer): Promise<void> {
        try {
            await consumer.disconnect();
        } catch (error) {
            throw new Error(
                `Failed to disconnect Kafka consumer: ${error}`
            );
        }
    }
}
