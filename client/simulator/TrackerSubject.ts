import { RentObserver } from "./RentObserver";

export abstract class TrackerSubject {
    private rentObserver: RentObserver;

    register(rentObserver: RentObserver): void {
        this.rentObserver = rentObserver;
    }

    // abstract async activate(): Promise<void>;

    protected notify(): void {
        this.rentObserver.update();
    }
}
