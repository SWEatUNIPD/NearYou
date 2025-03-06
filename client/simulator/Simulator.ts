import { SimulatorObserver } from './SimulatorObserver';
import { Rent } from './Rent';

export class Simulator implements SimulatorObserver {
    private rentList: Rent[];

    constructor(rentList: Rent[]) {
        this.rentList = rentList;
    }

    startSimulation(): void {
        this.rentList.forEach(rent => {
            console.log("start simulation");
            rent.register(this);
            rent.activate();
        });
    }

    update(id: string): void {
        const endedRentIndex = this.rentList.findIndex((trk) => trk.getId() === id);
        if (endedRentIndex !== -1) {
            this.rentList.splice(endedRentIndex, 1);
        }
    }
}
