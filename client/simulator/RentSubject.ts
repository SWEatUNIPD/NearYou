import { SimulatorObserver } from './SimulatorObserver';

export abstract class RentSubject {
    private simulatorObserver: SimulatorObserver;

    register(simulatorObserver: SimulatorObserver): void {
        this.simulatorObserver = simulatorObserver;
    }

    protected notifyRentEnded(id: string): void {
        this.simulatorObserver.updateRentEnded(id);
    }
}
