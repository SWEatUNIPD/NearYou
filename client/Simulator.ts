class Simulator implements SimulatorObserver {
    private rentList!: Rent[];

    Simulator(rentList: Rent[]) {
        this.rentList = rentList;

        this.rentList.forEach(rent => {
            rent.register(this);
            rent.activate();
        });
    }

    update(tracker: Tracker): void {
        
    }
}
