import { RentSubject } from "../../src/RentSubject";
import { SimulatorObserver } from "../../src/SimulatorObserver";

// Classe concreta per testare la classe astratta
class ConcreteRentSubject extends RentSubject {}

describe("RentSubject", () => {
    // Registra simulatorObserver correttamente (register)
    it("Registra simulatorObserver correttamente", () => {
        const subject = new ConcreteRentSubject();
        const mockObserver = {
            updateRentEnded: vi.fn(), // Mock del metodo updateRentEnded
        } as unknown as SimulatorObserver; // Casting per rispettare l'interfaccia

        subject.register(mockObserver);

        // Verifica che l'osservatore sia stato registrato correttamente
        expect(() => subject["notifyRentEnded"]("test-id")).not.toThrow();
    });

    // Lancia un errore se simulatorObserver non è registrato (notifyRentEnded)
    it("Lancia un errore se simulatorObserver non è registrato", () => {
        const subject = new ConcreteRentSubject();

        // Verifica che venga generato un errore se l'osservatore non è registrato
        // Uso una  funzione anonima (o "arrow function") che esegue il metodo notifyRentEnded sull'oggetto subject
        expect(() => subject["notifyRentEnded"]("test-id")).toThrowError(
            "Rent ended notify error: simulatorObserver not initialized"
        );
    });

    // Chiama updateRentEnded su simulatorObserver quando notifyRentEnded è chiamato (notifyRentEnded)
    it("Chiama updateRentEnded su simulatorObserver quando notifyRentEnded è chiamato", () => {
        const subject = new ConcreteRentSubject();
        const mockObserver = {
            updateRentEnded: vi.fn(), // Mock della funzione updateRentEnded
        } as unknown as SimulatorObserver;

        subject.register(mockObserver);
        subject["notifyRentEnded"]("test-id"); // Chiamata al metodo protetto

        // Verifica che il metodo updateRentEnded sia stato chiamato con l'ID corretto
        expect(mockObserver.updateRentEnded).toHaveBeenCalledWith("test-id");
    });
});