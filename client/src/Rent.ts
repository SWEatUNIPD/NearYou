import { RentSubject } from './RentSubject'
import { RentObserver } from './RentObserver'
import { Tracker } from './Tracker'
import { inject, injectable } from 'inversify';
import { TYPES } from './config/InversifyType';

@injectable()
// Classe che rappresenta un noleggio
export class Rent extends RentSubject implements RentObserver {
    
    constructor(
        private id: string,
        @inject(TYPES.Tracker)
        private tracker: Tracker
    ) {
        super();
    }

    // Metodo per attivare il noleggio
    activate(): void {
        this.tracker.register(this);
        this.tracker.activate();
    }

    // Metodo per notificare la fine della traccia
    updateTrackEnded(): void {
        this.notifyRentEnded(this.id);
    }

    // Metodo per ottenere l'ID del noleggio
    getId(): string {
        return this.id;
    }
}
