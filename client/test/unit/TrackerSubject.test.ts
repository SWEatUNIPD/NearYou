import { TrackerSubject } from "../../src/TrackerSubject";
import { RentObserver } from "../../src/RentObserver";

// Classe concreta per testare la classe astratta
class ConcreteTrackerSubject extends TrackerSubject {}

describe("TrackerSubject", () => {
    // Registra correttamente un osservatore (Register)
    it("Registra correttamente un osservatore", () => {
        const subject = new ConcreteTrackerSubject();
        const mockObserver = {
            updateTrackEnded: vi.fn(), // Mock del metodo updateTrackEnded
        } as unknown as RentObserver; // Casting per rispettare l'interfaccia

        subject.register(mockObserver);

        // Verifica che l'osservatore sia stato registrato correttamente
        expect(() => subject["notifyTrackEnded"]()).not.toThrow();
    });

    // Lancia un errore se rentObserver non è registrato
    it("Lancia un errore se rentObserver non è registrato", () => {
        const subject = new ConcreteTrackerSubject();

        // Verifica che venga generato un errore se l'osservatore non è registrato
        // Uso una  funzione anonima (o "arrow function") che esegue il metodo notifyTrackEnded sull'oggetto subject
        expect(() => subject["notifyTrackEnded"]()).toThrowError(
            "Track ended notify error: rentObserver not initialized"
        );
    });

    // Chiama updateTrackEnded sull'osservatore quando notifyTrackEnded è chiamato
    it("Chiama updateTrackEnded sull'osservatore quando notifyTrackEnded è chiamato", () => {
        const subject = new ConcreteTrackerSubject();
        const mockObserver = {
            updateTrackEnded: vi.fn(), // Mock della funzione updateTrackEnded
        } as unknown as RentObserver;

        subject.register(mockObserver);
        subject["notifyTrackEnded"](); // Chiamata al metodo protetto

        // Verifica che il metodo updateTrackEnded sia stato chiamato
        expect(mockObserver.updateTrackEnded).toHaveBeenCalled();
    });
});