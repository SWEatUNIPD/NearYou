import { RentObserver } from "./RentObserver";

export abstract class TrackerSubject {
    private rentObserver: RentObserver;

    register(rentObserver: RentObserver): void {
        this.rentObserver = rentObserver;
    }

    protected notifyTrackEnded(): void {
        this.rentObserver.updateTrackEnded();
    }
}
