class Rent extends RentSubject implements SimulatorObserver {
    private id!: number;
    private clientId!: number;
    private tracker!: Tracker;

    Rent(id: number, clientId: number, tracker: Tracker) {
        this.id = id;
        this.clientId = clientId;
        this.tracker = tracker;
    }

    activate(): void {
        this.tracker.register(this);
        this.tracker.activate();
    }

    update(): void {
        // end the rent

        this.notify(this.tracker);
    }
}
