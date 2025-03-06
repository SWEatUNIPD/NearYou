import { SimulatorObserver } from './SimulatorObserver';

export abstract class RentSubject {
    private simulatorObserver: SimulatorObserver;

    register(simulatorObserver: SimulatorObserver): void {
        this.simulatorObserver = simulatorObserver;
    }

    abstract activate(): void;

    protected notify(id: string): void {
        this.simulatorObserver.update(id);
    }
}
