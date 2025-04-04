import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { Tracker } from '../../src/Tracker';
import { KafkaManager } from '../../src/KafkaManager';
import { GeoPoint } from '../../src/GeoPoint';
import { env } from '../../src/config/EnvManager';

describe('Tracker', () => {
    let kafkaManagerMock: KafkaManager;
    let tracker: Tracker;

    beforeEach(() => {
        // Mock di KafkaManager
        kafkaManagerMock = {
            initAndConnectProducer: vi.fn().mockResolvedValue({
                send: vi.fn().mockResolvedValue(undefined), // Mock per il metodo send
                disconnect: vi.fn().mockResolvedValue(undefined),
            }),
            disconnectProducer: vi.fn().mockResolvedValue(undefined),
            initAndConnectConsumer: vi.fn().mockResolvedValue({
                disconnect: vi.fn().mockResolvedValue(undefined),
            }),
            disconnectConsumer: vi.fn().mockResolvedValue(undefined),
            sendMessage: vi.fn().mockResolvedValue(undefined), // Mock per sendMessage
        } as unknown as KafkaManager;

        // Crea un'istanza di Tracker con il mock di KafkaManager
        tracker = new Tracker('1', kafkaManagerMock);

        // Mock di env.SENDING_INTERVAL_MILLISECONDS
        env.SENDING_INTERVAL_MILLISECONDS = '1000'; // Valore mockato
    });

    afterEach(() => {
        vi.restoreAllMocks(); // Ripristina tutti i mock dopo ogni test
    });

    // Verifica che il messaggio venga correttamente formato e inviato tramite il KafkaManager
    it('dovrebbe muoversi lungo i punti del percorso e inviare messaggi Kafka', async () => {
        const trackPoints = [ new GeoPoint(1, 1) ];
        const sendMessageSpy = vi.spyOn(kafkaManagerMock, 'sendMessage');

        vi.useFakeTimers(); // Usa i fake timers per controllare setInterval

        const movePromise = tracker['move'](trackPoints);

        // Avanza il tempo per simulare l'invio dei messaggi
        for (let i = 0; i < trackPoints.length; i++) {
            vi.advanceTimersByTime(Number(env.SENDING_INTERVAL_MILLISECONDS));
            await Promise.resolve(); // Attendi che il ciclo di setInterval venga eseguito
        }

        // Avanza il tempo per simulare la disconnessione
        vi.advanceTimersByTime(Number(env.SENDING_INTERVAL_MILLISECONDS));
        await Promise.resolve(); // Attendi che il ciclo di setInterval venga eseguito

        // Verifica che i messaggi siano stati inviati correttamente
        expect(sendMessageSpy).toHaveBeenCalledTimes(trackPoints.length);
        expect(sendMessageSpy).toHaveBeenCalledWith(
            expect.anything(),
            'gps-data',
            JSON.stringify({
                timestamp: Date.now(),
                rentId: '1',
                latitude: trackPoints[0].getLatitude(),
                longitude: trackPoints[0].getLongitude(),
            })
        );

        vi.useRealTimers(); // Ripristina i timer reali

        await movePromise; // Attendi che la promessa di move venga risolta
    });
});