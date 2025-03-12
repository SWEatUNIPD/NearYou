import { injectable } from 'inversify';
import { Consumer, ConsumerConfig, EachMessagePayload, Kafka, Message, Producer } from "kafkajs";

@injectable()
// Classe che gestisce la connessione e le operazioni con Kafka
export class KafkaManager {
    constructor(
        private kafka: Kafka
    ) {}

    // Metodo per inizializzare e connettere un produttore Kafka
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

    // Metodo per disconnettere un produttore Kafka
    async disconnectProducer(producer: Producer): Promise<void> {
        try {
            await producer.disconnect();
        } catch (error) {
            throw new Error(
                `Failed to disconnect Kafka producer: ${error}`
            );
        }
    }

    // Metodo per inviare un messaggio tramite un produttore Kafka
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

    // Metodo per inizializzare e connettere un consumatore Kafka
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

    // Metodo per disconnettere un consumatore Kafka
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
