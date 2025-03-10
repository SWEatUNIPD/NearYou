import { v4 as uuidv4 } from 'uuid';
import { SimulatorObserver } from './SimulatorObserver';
import { Rent } from './Rent';
import { Tracker } from './Tracker';

export class Simulator implements SimulatorObserver {
    private rentList: Rent[];

    constructor(rentList: Rent[]) {
        this.rentList = rentList;
    }

    startSimulation(): void {
        this.rentList.forEach(rent => {
            rent.register(this);
            rent.activate();
        });

        this.startRentsInRuntime();
    }

    private startRentsInRuntime(): void {
        const minInterval = 5;
        const maxInterval = 20;
        let randomInterval = Math.floor(Math.random() * (maxInterval - minInterval + 1)) + minInterval;
        setInterval(() => {
            if (randomInterval == 0) {
                const trk = new Tracker(uuidv4());
                const rent = new Rent(uuidv4(), trk);
                this.rentList.push(rent);
                rent.register(this);
                rent.activate();

                randomInterval = Math.floor(Math.random() * (maxInterval - minInterval + 1)) + minInterval;
            }

            randomInterval--;
        }, 1000);
    }

    updateRentEnded(id: string): void {
        const endedRentIndex = this.rentList.findIndex((trk) => trk.getId() == id);

        if (endedRentIndex == -1) {
            throw new Error(
                `Rent with id '${id}' is ended but not found in list`
            );
        }

        this.rentList.splice(endedRentIndex, 1);
    }
}
