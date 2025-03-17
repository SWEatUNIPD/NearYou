import { describe, it, expect, vi, beforeEach } from 'vitest';
import { Tracker } from '../../src/Tracker';
import { KafkaManager } from '../../src/KafkaManager';
import { TrackFetcher } from '../../src/TrackFetcher';
import { GeoPoint } from '../../src/GeoPoint';
import { env } from '../../src/config/EnvManager';

describe('Tracker', () => {
    let kafkaManagerMock: KafkaManager;

    // Prima di ogni test, inizializza un mock per KafkaManager
    beforeEach(() => {
        kafkaManagerMock = {
            initAndConnectConsumer: vi.fn(), // Mock per initAndConnectConsumer
            initAndConnectProducer: vi.fn(), // Mock per initAndConnectProducer
            disconnectProducer: vi.fn(),     // Mock per disconnectProducer
            disconnectConsumer: vi.fn(),     // Mock per disconnectConsumer
            sendMessage: vi.fn(),            // Mock per sendMessage
        } as unknown as KafkaManager; // Casting a KafkaManager per evitare errori di tipo
    });

    // Test: Verifica che i metodi fetchTrack e move vengano chiamati quando activate è eseguito
    it('dovrebbe chiamare i metodi fetchTrack e move quando activate è chiamato', async () => {
        const trackerId = 'tracker-1';
        const tracker = new Tracker(trackerId, kafkaManagerMock);

        // Mock per TrackFetcher che simula il fetch di una traccia
        const trackFetcherMock = {
            fetchTrack: vi.fn().mockResolvedValue([new GeoPoint(1, 1), new GeoPoint(2, 2)]),
        } as unknown as TrackFetcher;

        // Sostituisci il metodo fetchTrack di TrackFetcher con il mock
        vi.spyOn(TrackFetcher.prototype, 'fetchTrack').mockImplementation(trackFetcherMock.fetchTrack);

        // Esegui il metodo activate
        await tracker.activate();

        // Verifica che fetchTrack sia stato chiamato
        expect(trackFetcherMock.fetchTrack).toHaveBeenCalled();
    });

    // Test: Verifica che gli errori vengano gestiti correttamente quando fetchTrack genera un'eccezione
    it('dovrebbe gestire correttamente un errore quando fetchTrack genera un\'eccezione', async () => {
        const trackerId = 'tracker-1';
        const tracker = new Tracker(trackerId, kafkaManagerMock);

        // Mock per TrackFetcher che simula un errore durante il fetch
        const trackFetcherMock = {
            fetchTrack: vi.fn().mockRejectedValue(new Error('Errore durante il fetch')),
        } as unknown as TrackFetcher;

        // Sostituisci il metodo fetchTrack di TrackFetcher con il mock
        vi.spyOn(TrackFetcher.prototype, 'fetchTrack').mockImplementation(trackFetcherMock.fetchTrack);

        // Esegui il metodo activate
        await tracker.activate();

        // Verifica che fetchTrack sia stato chiamato
        expect(trackFetcherMock.fetchTrack).toHaveBeenCalled();
    });

    // Test: Verifica che il consumer venga inizializzato e connesso correttamente quando listenToAdv è chiamato
    it('dovrebbe inizializzare e connettere il consumer quando listenToAdv è chiamato', async () => {
        const trackerId = 'tracker-1';
        const tracker = new Tracker(trackerId, kafkaManagerMock);

        // Esegui il metodo activate
        await tracker.activate();

        // Verifica che initAndConnectConsumer sia stato chiamato con i parametri corretti
        expect(kafkaManagerMock.initAndConnectConsumer).toHaveBeenCalledWith('adv-data', 'trackers', expect.any(Function));
    });

    // Test: Verifica che gli errori vengano gestiti correttamente quando initAndConnectConsumer genera un'eccezione
    it('dovrebbe gestire correttamente un errore quando initAndConnectConsumer genera un\'eccezione', async () => {
        const trackerId = 'tracker-1';
        const tracker = new Tracker(trackerId, kafkaManagerMock);

        // Simula un errore durante l'inizializzazione e connessione del consumer
        vi.spyOn(kafkaManagerMock, 'initAndConnectConsumer').mockRejectedValue(new Error('Errore del consumer'));

        // Esegui il metodo activate
        await tracker.activate();

        // Verifica che initAndConnectConsumer sia stato chiamato con i parametri corretti
        expect(kafkaManagerMock.initAndConnectConsumer).toHaveBeenCalledWith('adv-data', 'trackers', expect.any(Function));
    });

    // Test: Verifica che lo stato di disponibilità sia restituito correttamente
    it('dovrebbe restituire correttamente lo stato di disponibilità quando getIsAvailable è chiamato', () => {
        const trackerId = 'tracker-1';
        const tracker = new Tracker(trackerId, kafkaManagerMock);

        // Verifica che lo stato di disponibilità sia true di default
        expect(tracker.getIsAvailable()).toBe(true);
    });
});