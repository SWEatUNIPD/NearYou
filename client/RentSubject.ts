abstract class RentSubject {
    private simulatorObserver!: SimulatorObserver;

    register(simulatorObserver: SimulatorObserver): void {
        this.simulatorObserver = simulatorObserver;
    }

    abstract activate(): void;

    protected notify(tracker: Tracker): void {
        this.simulatorObserver.update(tracker);
    }
}
