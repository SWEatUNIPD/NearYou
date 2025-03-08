import { SimulatorObserver } from './SimulatorObserver';

export abstract class RentSubject {
    private simulatorObserver: SimulatorObserver;

    register(simulatorObserver: SimulatorObserver): void {
        this.simulatorObserver = simulatorObserver;
    }

    protected notifyRentEnded(id: string): void {
        if (this.simulatorObserver == null) {
            throw new Error(
                `Rent ended notify error: simulatorObserver not initialized`
            );
        }

        this.simulatorObserver.updateRentEnded(id);
    }
}
