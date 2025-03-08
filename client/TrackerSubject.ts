import { RentObserver } from "./RentObserver";

export abstract class TrackerSubject {
    private rentObserver: RentObserver;

    register(rentObserver: RentObserver): void {
        this.rentObserver = rentObserver;
    }

    protected notifyTrackEnded(): void {
        if (this.rentObserver == null) {
            throw new Error(
                `Track ended notify error: rentObserver not initialized`
            );
        }

        this.rentObserver.updateTrackEnded();
    }
}
