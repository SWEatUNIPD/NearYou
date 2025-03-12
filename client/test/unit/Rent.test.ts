import { describe, it, expect, vi } from 'vitest';
import { Rent } from '../../src/Rent';
import { Tracker } from '../../src/Tracker';

describe("Rent", () => {
    // Test per verificare se il costruttore inizializza correttamente l'id e il tracker (costruttore)
    it("Test costruttore: Inizializzazione id e tracker", () => {
        // Creiamo un mock per Tracker
        const mockTracker = {} as Tracker; // Mock vuoto, non ci interessa il comportamento per questo test
        const rent = new Rent('1', mockTracker);

        // Verifica che l'id sia inizializzato correttamente
        expect(rent.getId()).toBe('1');

        // Verifica che il tracker sia stato assegnato correttamente
        expect(rent['tracker']).toBe(mockTracker); // Usiamo l'accesso alla proprietà privata per verificare
    });

    // Test per verificare se il metodo activate registra e attiva il tracker (activate)
    it("Attivazione: Registrazione e attivazione del tracker", () => {
        // Creiamo un mock per Tracker
        const mockTracker = {
            register: vi.fn(), // Mock del metodo register
            activate: vi.fn(), // Mock del metodo activate
        } as unknown as Tracker; // Cast per evitare errori di tipo

        const rent = new Rent('1', mockTracker);

        // Chiama il metodo activate
        rent.activate();

        // Verifica che il metodo register sia stato chiamato con l'istanza corretta di Rent
        expect(mockTracker.register).toHaveBeenCalledWith(rent);

        // Verifica che il metodo activate sia stato chiamato
        expect(mockTracker.activate).toHaveBeenCalled();
    });

    // Test per verificare se il metodo updateTrackEnded notifica la fine del rent (updateTrackEnded)
    it("Notifica fine rent: updateTrackEnded", () => {
        // Creiamo un mock per Tracker
        const mockTracker = {} as Tracker; // Non ci interessa il comportamento di Tracker per questo test

        const rent = new Rent('1', mockTracker);

        // Creiamo un mock per SimulatorObserver
        const mockSimulatorObserver = {
            updateRentEnded: vi.fn(), // Mock del metodo updateRentEnded
        };

        // Registriamo il mock come observer
        rent.register(mockSimulatorObserver);

        // Chiama il metodo updateTrackEnded
        rent.updateTrackEnded();

        // Verifica che il metodo updateRentEnded del mock sia stato chiamato con l'id corretto
        expect(mockSimulatorObserver.updateRentEnded).toHaveBeenCalledWith('1');
    });

    // Test per verificare se il metodo getId restituisce l'id corretto (getId)
    it("Restituzione id: getId", () => {
        // Creiamo un mock per Tracker
        const mockTracker = {} as Tracker; // Non ci interessa il comportamento di Tracker per questo test

        const rent = new Rent('1', mockTracker);

        // Verifica che il metodo getId restituisca l'id corretto
        expect(rent.getId()).toBe('1');
    });
});