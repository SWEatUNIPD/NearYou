import { RentSubject } from './RentSubject'
import { SimulatorObserver } from './SimulatorObserver'
import { Tracker } from './Tracker'

export class Rent extends RentSubject implements SimulatorObserver {
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

    update(): void {
        // end the rent

        this.notify(this.id);
    }

    getId(): string {
        return this.id;
    }
}
