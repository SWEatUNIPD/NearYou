import { SimulatorObserver } from './SimulatorObserver';

// Classe astratta che rappresenta un soggetto che può notificare la fine di un noleggio
export abstract class RentSubject {
    private simulatorObserver!: SimulatorObserver;

    // Metodo per registrare un osservatore
    register(simulatorObserver: SimulatorObserver): void {
        this.simulatorObserver = simulatorObserver;
    }

    // Metodo protetto per notificare la fine di un noleggio
    protected notifyRentEnded(id: string): void {
        if (this.simulatorObserver == null) {
            throw new Error(
                `Rent ended notify error: simulatorObserver not initialized`
            );
        }

        try {
            this.simulatorObserver.updateRentEnded(id);
        } catch (err) {
            console.error(`Error caught trying to update the simulator rents list.\n${err}`);
        }
    }
}
