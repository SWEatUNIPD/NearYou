import { RentObserver } from "./RentObserver";

// Classe astratta che rappresenta un soggetto che può notificare la fine di una traccia
export abstract class TrackerSubject {
    private rentObserver!: RentObserver;

    // Metodo per registrare un osservatore
    register(rentObserver: RentObserver): void {
        this.rentObserver = rentObserver;
    }

    // Metodo protetto per notificare la fine di una traccia
    protected notifyTrackEnded(): void {
        if (this.rentObserver == null) {
            throw new Error(
                `Track ended notify error: rentObserver not initialized`
            );
        }

        this.rentObserver.updateTrackEnded();
    }
}
