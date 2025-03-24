import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { Simulator } from '../../src/Simulator';
import { Tracker } from '../../src/Tracker';
import { KafkaManager } from '../../src/KafkaManager';

describe('Simulator', () => {
    let trackerList: Tracker[];
    let simulator: Simulator;

    // Definisci un mock per KafkaManager
    const kafkaManagerMock = {
        initAndConnectConsumer: vi.fn(),
        initAndConnectProducer: vi.fn(),
        disconnectProducer: vi.fn(),
        disconnectConsumer: vi.fn(),
        sendMessage: vi.fn(),
    } as unknown as KafkaManager;

    beforeEach(() => {
        // Inizializza una lista di tracker fittizi
        trackerList = [
            new Tracker('1', kafkaManagerMock),
            new Tracker('2', kafkaManagerMock)
        ];

        // Crea un'istanza di Simulator con la lista di tracker
        simulator = new Simulator(trackerList);

        // Mock di env.INIT_RENT_COUNT
        vi.mock('../../src/config/EnvManager', () => ({
            env: {
                INIT_RENT_COUNT: '2', // Valore mockato
            },
        }));
    });

    afterEach(() => {
        vi.restoreAllMocks(); // Ripristina tutti i mock dopo ogni test
    });

    // Test di startSimulation
    it('dovrebbe avviare correttamente la simulazione con il numero iniziale di rent', async () => {
        // Mock del metodo activate dei tracker per simulare l'avvio
        const activateSpy = vi.spyOn(Tracker.prototype, 'activate').mockResolvedValue(undefined);

        await simulator.startSimulation();

        // Verifica che il metodo activate sia stato chiamato per ogni rent
        expect(activateSpy).toHaveBeenCalledTimes(2);
    });

    // Test di startRent
    it('dovrebbe avviare correttamente un rent con un tracker disponibile', async () => {
        // Mock del metodo getIsAvailable per simulare un tracker disponibile
        vi.spyOn(Tracker.prototype, 'getIsAvailable').mockReturnValue(true);

        // Mock del metodo activate per simulare l'avvio
        const activateSpy = vi.spyOn(Tracker.prototype, 'activate').mockResolvedValue(undefined);

        await simulator['startRent']();

        // Verifica che il metodo activate sia stato chiamato
        expect(activateSpy).toHaveBeenCalled();
    });

    // Test di startRent con tracker null
    it('dovrebbe lanciare un\'eccezione se non ci sono tracker disponibili', async () => {
        // Mock del metodo getIsAvailable per simulare che nessun tracker è disponibile
        vi.spyOn(Tracker.prototype, 'getIsAvailable').mockReturnValue(false);

        // Verifica che venga lanciata un'eccezione
        await expect(simulator['startRent']()).rejects.toThrow('Impossible to generate a rent, no track available');
    });
});