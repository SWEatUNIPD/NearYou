import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { Simulator } from '../../src/Simulator';
import { Rent } from '../../src/Rent';

describe("Simulator", () => {
    // Mock di Rent
    let mockRent: Rent;

    beforeEach(() => {
        // Creiamo un mock per Rent
        mockRent = {
            getId: vi.fn(),
            register: vi.fn(),
            activate: vi.fn(),
        } as unknown as Rent;
        // Quando crei un mock di una classe, TypeScript potrebbe lamentarsi perché il mock non ha tutte le proprietà e i metodi della classe reale.
        // Usando as unknown as MyClass, stai dicendo a TypeScript: "Tratta questo oggetto come se fosse di tipo MyClass, anche se non lo è completamente".
    });

    afterEach(() => {
        // Ripristina i mock dopo ogni test
        vi.clearAllMocks();
        vi.useRealTimers(); // Ripristina i timer reali
    });

    // Test per verificare se rentList viene inizializzato correttamente (Costruttore)
    it("Test costruttore: Inizializzazione rentList", () => {
        const rentList = [mockRent];
        const simulator = new Simulator(rentList);

        // Verifica che la lista di rent nel simulatore sia uguale a quella inizializzata
        expect(simulator['rentList']).toEqual(rentList);
    });

    // Test per verificare se la simulazione inizia e tutti i rent vengono attivati (startSimulation)
    it("Avvio simulazione e attivazione di tutti i rent", () => {
        const rentList = [mockRent];
        const simulator = new Simulator(rentList);

        // Avvia la simulazione
        simulator.startSimulation();

        // Verifica che il metodo activate sia stato chiamato per ogni rent
        expect(mockRent.activate).toHaveBeenCalled();
    });

    // Test per verificare se l'osservatore viene registrato per ogni rent all'avvio della simulazione (startSimulation)
    it("Registra l'osservatore per ogni rent all'avvio della simulazione", () => {
        const rentList = [mockRent];
        const simulator = new Simulator(rentList);

        // Avvia la simulazione
        simulator.startSimulation();

        // Verifica che il metodo register sia stato chiamato con il simulatore come argomento
        expect(mockRent.register).toHaveBeenCalledWith(simulator);
    });

    // Test per verificare se un rent viene rimosso quando termina (updateRentEnded)
    it("Rimozione rent quando termina", () => {
        const rentList = [mockRent];
        const simulator = new Simulator(rentList);

        // Configura il mock per restituire un ID specifico
        vi.mocked(mockRent.getId).mockReturnValue('1');

        // Termina il rent con id '1'
        simulator.updateRentEnded('1');

        // Verifica che la lista di rent nel simulatore sia vuota
        expect(simulator['rentList']).toEqual([]);
    });

    // Test per verificare se viene lanciato un errore quando si tenta di terminare un rent inesistente (updateRentEnded)
    it("Lancia un errore se il rent da terminare non viene trovato", () => {
        const rentList = [mockRent];
        const simulator = new Simulator(rentList);

        // Configura il mock per restituire un ID specifico
        vi.mocked(mockRent.getId).mockReturnValue('1');

        // Verifica che venga lanciato un errore quando si tenta di terminare un rent inesistente
        expect(() => simulator.updateRentEnded('2')).toThrowError("Rent with id '2' is ended but not found in list");
    });

    // Test per startRentsInRuntime quando deve entrare nell'intervallo di tempo
    // Il problema è che non riesco ad avanzare il tempo per far scattare l'intervallo

});