import { RentSubject } from './RentSubject'
import { RentObserver } from './RentObserver'
import { Tracker } from './Tracker'

export class Rent extends RentSubject implements RentObserver {
    private id: string;
    private tracker: Tracker;

    constructor(id: string, tracker: Tracker) {
        super();

        this.id = id;
        this.tracker = tracker;
    }

    activate(): void {
        this.tracker.register(this);
        this.tracker.activate();
    }

    updateTrackEnded(): void {
        this.notifyRentEnded(this.id);
    }

    getId(): string {
        return this.id;
    }
}
