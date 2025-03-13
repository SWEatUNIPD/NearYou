import { inject } from 'inversify';
import { SimulatorObserver } from './SimulatorObserver';
import { Rent } from './Rent';
import { TYPES } from './config/InversifyType';
import { container } from './config/Inversify.config';

// Definisce la classe Simulator come iniettabile tramite Inversify
export class Simulator implements SimulatorObserver {
    // Costruttore che inietta la lista di Rent
    constructor(
        @inject(TYPES.RentList)
        private rentList: Rent[]
    ) {}

    // Metodo per avviare la simulazione
    startSimulation(): void {
        // Registra e attiva ogni Rent nella lista
        this.rentList.forEach(rent => {
            rent.register(this);
            rent.activate();
        });

        // Avvia la creazione di nuovi Rent a runtime
        this.startRentsInRuntime();
    }

    // Metodo privato per avviare i Rent a runtime con intervalli casuali
    private startRentsInRuntime(): void {
        const minInterval = 5;
        const maxInterval = 20;
        let randomInterval = Math.floor(Math.random() * (maxInterval - minInterval + 1)) + minInterval;
        setInterval(() => {
            if (randomInterval == 0) {
                const rent: Rent = container.get<Rent>(TYPES.Rent);
                this.rentList.push(rent);
                rent.register(this);
                rent.activate();

                // Calcola un nuovo intervallo casuale
                randomInterval = Math.floor(Math.random() * (maxInterval - minInterval + 1)) + minInterval;
            }

            randomInterval--;
        }, 1000);
    }

    // Metodo per aggiornare la lista quando un Rent termina
    updateRentEnded(id: string): void {
        const endedRentIndex = this.rentList.findIndex((trk) => trk.getId() == id);

        if (endedRentIndex == -1) {
            throw new Error(
                `Rent with id '${id}' is ended but not found in list`
            );
        }

        // Rimuove il Rent terminato dalla lista
        this.rentList.splice(endedRentIndex, 1);
    }
}
