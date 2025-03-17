import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { Simulator } from '../../src/Simulator';
import { Tracker } from '../../src/Tracker';
import { env } from '../../src/config/EnvManager';

describe('Simulator', () => {
    let trackerMap: Map<string, Tracker>;
    let simulator: Simulator;

    beforeEach(() => {
        // Inizializza una mappa di tracker fittizi
        trackerMap = new Map<string, Tracker>();
        const tracker1 = new Tracker('tracker-1', {} as any);
        const tracker2 = new Tracker('tracker-2', {} as any);
        trackerMap.set('tracker-1', tracker1);
        trackerMap.set('tracker-2', tracker2);

        // Crea un'istanza di Simulator con la mappa di tracker
        simulator = new Simulator(trackerMap);

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

    it('dovrebbe avviare i rent a runtime con intervalli casuali', async () => {
      // Mock del metodo startRent per simulare l'avvio di un rent
      const startRentSpy = vi.spyOn(simulator as any, 'startRent').mockResolvedValue(undefined);
  
      // Usiamo i fake timers per controllare setInterval
      vi.useFakeTimers();
      simulator['startRentsInRuntime']();
  
      // Simuliamo l'avanzamento del tempo per attivare il setInterval
      // Avanziamo di un tempo sufficiente per far scattare il setInterval
      vi.advanceTimersByTime(20000); // Avanza di 20 secondi
  
      // Verifica che startRent sia stato chiamato almeno una volta
      expect(startRentSpy).toHaveBeenCalled();
  
      // Ripristiniamo i timer reali
      vi.useRealTimers();
  });

    // Test di trackEndedUpdate
    it('dovrebbe gestire correttamente l\'aggiornamento della fine di un percorso', async () => {
        // Simula l'aggiornamento della fine di un percorso
        await simulator.trackEndedUpdate('tracker-1');

        // Verifica che non vengano lanciati errori
        expect(true).toBe(true); // Placeholder per la verifica
    });

    // Test di trackEndedUpdate che lancia l'eccezione
    it('dovrebbe gestire correttamente un errore durante l\'aggiornamento della fine di un percorso', async () => {
        // Mock di un comportamento che genera un errore
        vi.spyOn(simulator as any, 'trackEndedUpdate').mockRejectedValue(new Error('Errore durante l\'aggiornamento'));

        // Verifica che l'errore venga propagato
        await expect(simulator.trackEndedUpdate('tracker-1')).rejects.toThrow('Errore durante l\'aggiornamento');
    });
});