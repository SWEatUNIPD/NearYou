import { SimulatorObserver } from "./SimulatorObserver";

// Classe astratta che rappresenta un soggetto che può notificare la fine di una traccia
export abstract class TrackerSubject {
    private simulatorObserver!: SimulatorObserver;

    // Metodo per registrare un osservatore
    register(simulatorObserver: SimulatorObserver): void {
        this.simulatorObserver = simulatorObserver;
    }

    // Metodo protetto per notificare la fine di una traccia
    protected notifyTrackEnded(id: string): void {
        if (this.simulatorObserver == null) {
            throw new Error(
                `Track ended notify error: simulatorObserver not initialized`
            );
        }

        this.simulatorObserver.trackEndedUpdate(id);
    }
}
