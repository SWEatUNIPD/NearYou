import { inject } from 'inversify';
import { SimulatorObserver } from './SimulatorObserver';
import { TYPES } from './config/InversifyType';
import { Tracker } from './Tracker';
import { env } from './config/EnvManager';

// Definisce la classe Simulator come iniettabile tramite Inversify
export class Simulator implements SimulatorObserver {
    constructor(
        @inject(TYPES.TrackerMap)
        private trackerMap: Map<string, Tracker>
    ) { }

    private async startRent(): Promise<void> {
        let tracker: Tracker | null = null;
        for (const trk of this.trackerMap.values()) {
            if (trk.getIsAvailable()) {
                tracker = trk;
                break;
            }
        }
        if (tracker == null) {
            throw new Error(
                'Impossible to generate a rent, no track available'
            );
        }

        // TODO: API per chiedere rent id (butta via)
        tracker.setIsAvailable(false);
        tracker.register(this);
        tracker.activate();
    }

    // Metodo per avviare la simulazione
    async startSimulation(): Promise<void> {
        for (let i = 0; i < Number(env.INIT_RENT_COUNT); i++) {
            try {
                await this.startRent();
            } catch (err) {
                console.error(`Error caught trying to start a new rent.\n${err}`);
                return;
            }
        }

        this.startRentsInRuntime();
    }

    // Metodo privato per avviare i Rent a runtime con intervalli casuali
    private startRentsInRuntime(): void {
        const minInterval = 5;
        const maxInterval = 15;
        let randomInterval = Math.floor(Math.random() * (maxInterval - minInterval + 1)) + minInterval;
        setInterval(async () => {
            if (randomInterval == 0) {
                await this.startRent();
                randomInterval = Math.floor(Math.random() * (maxInterval - minInterval + 1)) + minInterval;
            }

            randomInterval--;
        }, 1000);
    }

    trackEndedUpdate(id: string): void {
        try {
            this.trackerMap.get(id)?.setIsAvailable(true);

            // TODO: chiamata api (qua o in tracker?)
        } catch (err) {
            throw new Error(
                `Tracker with id '${id}' is ended but not found in list`
            );
        }
    }
}
