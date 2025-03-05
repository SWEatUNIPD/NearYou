class Tracker extends TrackerSubject {
    private id!: number;

    Tracker(id: number) {
        this.id = id;
    }

    activate(): void {
        // API request

        this.move();
    }

    private move(): void {
        // consume path points

        this.notify();
    }

    private receiveAdv(adv: string): void {

    }
}