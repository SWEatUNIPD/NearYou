import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { KafkaManager } from '../../src/KafkaManager';
import { Kafka, Producer, Consumer, EachMessagePayload } from 'kafkajs';

describe('KafkaManager', () => {
    let kafkaManager: KafkaManager;
    let kafkaMock: Kafka;
    let producerMock: Producer;
    let consumerMock: Consumer;

    beforeEach(() => {
        // Mock di Kafka e dei suoi componenti
        kafkaMock = {
            producer: vi.fn().mockReturnValue({
                connect: vi.fn().mockResolvedValue(undefined),
                disconnect: vi.fn().mockResolvedValue(undefined),
                send: vi.fn().mockResolvedValue(undefined),
            }),
            consumer: vi.fn().mockReturnValue({
                connect: vi.fn().mockResolvedValue(undefined),
                disconnect: vi.fn().mockResolvedValue(undefined),
                subscribe: vi.fn().mockResolvedValue(undefined),
                run: vi.fn().mockResolvedValue(undefined),
            }),
        } as unknown as Kafka;

        producerMock = kafkaMock.producer();
        consumerMock = kafkaMock.consumer({ groupId: 'test-group' });

        kafkaManager = new KafkaManager(kafkaMock);
    });

    afterEach(() => {
        vi.restoreAllMocks(); // Ripristina tutti i mock dopo ogni test
    });

    // Test per initAndConnectProducer
    it('dovrebbe inizializzare e connettere correttamente un produttore Kafka', async () => {
        const producer = await kafkaManager.initAndConnectProducer();
        expect(producer).toBeDefined();
        expect(producer.connect).toHaveBeenCalled();
    });

    it('dovrebbe lanciare un errore se la connessione del produttore fallisce', async () => {
        vi.spyOn(producerMock, 'connect').mockRejectedValue(new Error('Connection failed'));
        await expect(kafkaManager.initAndConnectProducer()).rejects.toThrow('Failed to connect Kafka producer: Error: Connection failed');
    });

    // Test per disconnectProducer
    it('dovrebbe disconnettere correttamente un produttore Kafka', async () => {
        await kafkaManager.disconnectProducer(producerMock);
        expect(producerMock.disconnect).toHaveBeenCalled();
    });

    it('dovrebbe lanciare un errore se la disconnessione del produttore fallisce', async () => {
        vi.spyOn(producerMock, 'disconnect').mockRejectedValue(new Error('Disconnection failed'));
        await expect(kafkaManager.disconnectProducer(producerMock)).rejects.toThrow('Failed to disconnect Kafka producer: Error: Disconnection failed');
    });

    // Test per sendMessage
    it('dovrebbe inviare correttamente un messaggio tramite un produttore Kafka', async () => {
        const topic = 'test-topic';
        const data = 'test-data';
        await kafkaManager.sendMessage(producerMock, topic, data);
        expect(producerMock.send).toHaveBeenCalledWith({
            topic: topic,
            messages: [{ value: data }],
        });
    });

    it('dovrebbe lanciare un errore se l\'invio del messaggio fallisce', async () => {
        vi.spyOn(producerMock, 'send').mockRejectedValue(new Error('Send failed'));
        await expect(kafkaManager.sendMessage(producerMock, 'test-topic', 'test-data')).rejects.toThrow('Failed to send message from Kafka producer: Error: Send failed');
    });

    // Test per initAndConnectConsumer
    it('dovrebbe inizializzare e connettere correttamente un consumatore Kafka', async () => {
        const topic = 'test-topic';
        const groupId = 'test-group';
        const eachMessageHandler = vi.fn().mockResolvedValue(undefined);
    
        const consumer = await kafkaManager.initAndConnectConsumer(topic, groupId, eachMessageHandler);
        expect(consumer).toBeDefined();
        expect(consumer.connect).toHaveBeenCalled();
        expect(consumer.subscribe).toHaveBeenCalledWith({ topic, fromBeginning: true });
    
        // Verifica che consumer.run sia stato chiamato con un oggetto che contiene una funzione eachMessage
        expect(consumer.run).toHaveBeenCalledWith(
            expect.objectContaining({
                eachMessage: expect.any(Function), // Verifica che eachMessage sia una funzione
            })
        );
    
        // Simula una chiamata a eachMessage e verifica che eachMessageHandler sia stato chiamato
        const runCallArgs = (consumer.run as vi.Mock).mock.calls[0][0];
        const mockPayload = { topic: 'test-topic', partition: 0, message: { value: Buffer.from('test-data') } } as EachMessagePayload;
        await runCallArgs.eachMessage(mockPayload);
    
        // Verifica che eachMessageHandler sia stato chiamato con il payload corretto
        expect(eachMessageHandler).toHaveBeenCalledWith(mockPayload);
    });

    it('dovrebbe lanciare un errore se la connessione del consumatore fallisce', async () => {
        vi.spyOn(consumerMock, 'connect').mockRejectedValue(new Error('Connection failed'));
        await expect(kafkaManager.initAndConnectConsumer('test-topic', 'test-group', vi.fn())).rejects.toThrow('Failed to connect Kafka consumer: Error: Connection failed');
    });

    // Test per disconnectConsumer
    it('dovrebbe disconnettere correttamente un consumatore Kafka', async () => {
        await kafkaManager.disconnectConsumer(consumerMock);
        expect(consumerMock.disconnect).toHaveBeenCalled();
    });

    it('dovrebbe lanciare un errore se la disconnessione del consumatore fallisce', async () => {
        vi.spyOn(consumerMock, 'disconnect').mockRejectedValue(new Error('Disconnection failed'));
        await expect(kafkaManager.disconnectConsumer(consumerMock)).rejects.toThrow('Failed to disconnect Kafka consumer: Error: Disconnection failed');
    });
});