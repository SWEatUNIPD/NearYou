import { Tracker } from '../../src/Tracker';
import { TrackFetcher } from '../../src/TrackFetcher';
import { KafkaManager } from '../../src/KafkaManager';
import { GeoPoint } from '../../src/GeoPoint';
import { Producer, Consumer, EachMessagePayload } from 'kafkajs';

describe("Tracker", () => {
    let mockTrackFetcher: TrackFetcher;
    let mockKafkaManager: KafkaManager;
    let mockProducer: Producer;
    let mockConsumer: Consumer;

    beforeEach(() => {
        // Mock di TrackFetcher
        mockTrackFetcher = {
            fetchTrack: vi.fn(),
        } as unknown as TrackFetcher;

        // Mock di KafkaManager
        mockKafkaManager = {
            initAndConnectConsumer: vi.fn(),
            initAndConnectProducer: vi.fn(),
            disconnectProducer: vi.fn(),
            disconnectConsumer: vi.fn(),
            sendMessage: vi.fn(),
        } as unknown as KafkaManager;

        // Mock di Producer e Consumer
        mockProducer = {} as Producer;
        mockConsumer = {} as Consumer;

        // Configura i mock
        vi.spyOn(mockKafkaManager, 'initAndConnectProducer').mockResolvedValue(mockProducer);
        vi.spyOn(mockKafkaManager, 'initAndConnectConsumer').mockResolvedValue(mockConsumer);
    });

    afterEach(() => {
        vi.clearAllMocks();
        vi.useRealTimers();
    });

    // Test per il metodo activate (activate)
    it("activate: dovrebbe ottenere i punti della traccia e iniziare a muoversi", async () => {
        const tracker = new Tracker('tracker-1', mockKafkaManager);

        // Configura il mock di TrackFetcher
        const mockTrackPoints = [
            new GeoPoint(45.4068224, 11.8766614), // Padova
            new GeoPoint(45.5470262, 11.5443812), // Vicenza
        ];
        vi.mocked(mockTrackFetcher.fetchTrack).mockResolvedValue(mockTrackPoints);

        // Sostituisci TrackFetcher con il mock
        vi.spyOn(TrackFetcher.prototype, 'fetchTrack').mockImplementation(mockTrackFetcher.fetchTrack);

        // Esegui il metodo activate
        await tracker.activate();

        // Verifica che fetchTrack sia stato chiamato
        expect(mockTrackFetcher.fetchTrack).toHaveBeenCalled();

        // Verifica che initAndConnectProducer sia stato chiamato
        expect(mockKafkaManager.initAndConnectProducer).toHaveBeenCalled();

        // Verifica che initAndConnectConsumer sia stato chiamato
        expect(mockKafkaManager.initAndConnectConsumer).toHaveBeenCalled();
    });
    
    // Test per listenToAdv per verificare la gestione dei messaggi
    it("listenToAdv: dovrebbe gestire correttamente i messaggi ricevuti", async () => {
        const tracker = new Tracker('tracker-1', mockKafkaManager);

        // Mock della console.log per verificare che venga chiamata
        const consoleSpy = vi.spyOn(console, 'log');

        // Variabile per memorizzare la callback
        let savedCallback: ((payload: EachMessagePayload) => Promise<void>) | undefined;

        // Sovrascrivi il metodo initAndConnectConsumer per catturare la callback
        vi.mocked(mockKafkaManager.initAndConnectConsumer).mockImplementation(
            async (topic, groupId, callback) => {
                savedCallback = callback;
                return mockConsumer;
            }
        );

        // Chiamata privata al metodo listenToAdv
        await (tracker as any).listenToAdv();

        // Verifica che initAndConnectConsumer sia stato chiamato con i parametri corretti
        expect(mockKafkaManager.initAndConnectConsumer).toHaveBeenCalledWith(
            'adv-data', 'trackers', expect.any(Function)
        );

        // Verifica che la callback sia stata catturata
        expect(savedCallback).toBeDefined();

        // Simula la ricezione di un messaggio con tutte le proprietà richieste
        const testPayload: EachMessagePayload = {
            topic: 'test-topic',
            partition: 0,
            message: {
                key: Buffer.from('test-key'),
                value: Buffer.from('test-value'),
                timestamp: '0',
                size: 0,
                attributes: 0,
                offset: '0'
            },
            heartbeat: async () => {},
            pause: () => () => {}
        };

        // Chiamiamo la callback catturata con il payload di test
        if (savedCallback) {
            await savedCallback(testPayload);
        }

        // Verifica che console.log sia stato chiamato con i dati corretti
        expect(consoleSpy).toHaveBeenCalledWith({
            topic: 'test-topic',
            partition: 0,
            key: 'test-key',
            value: 'test-value',
        });
    });

    // Test per move quando deve entrare nell'intervallo di tempo
    // Il problema è che non riesco ad avanzare il tempo per far scattare l'intervallo

});