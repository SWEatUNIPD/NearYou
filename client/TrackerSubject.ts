abstract class TrackerSubject {
    private rentObserver!: RentObserver;

    register(rentObserver: RentObserver): void {
        this.rentObserver = rentObserver;
    }

    abstract activate(): void;

    protected notify(): void {
        this.rentObserver.update();
    }
}
