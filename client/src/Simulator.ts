import { inject } from 'inversify';
import { SimulatorObserver } from './SimulatorObserver';
import { TYPES } from './config/InversifyType';
import { Tracker } from './Tracker';
import { env } from './config/EnvManager';

// Definisce la classe Simulator come iniettabile tramite Inversify
export class Simulator implements SimulatorObserver {
    private rentIdMap: Map<string, string> = new Map();

    constructor(
        @inject(TYPES.TrackerMap)
        private trackerMap: Map<string, Tracker>
    ) {
        this.trackerMap.forEach(tracker => {
            tracker.register(this);
        });
    }

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

        const requestUrl = `http://localhost:9000/start-rent/${tracker.getId()},${tracker.getId()}`;
        const response = await fetch(requestUrl);
        if (!response.ok) {
            throw new Error(
                `Rent ID request error: ${response.status} - ${await response.text()}`
            );
        }
        this.rentIdMap.set(tracker.getId(), (await response.json()).id);

        tracker.setIsAvailable(false);
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
                try {
                    await this.startRent();
                } catch (err) {
                    console.error(`Error caught trying to start a new rent in runtime.\n${err}`);
                }
                randomInterval = Math.floor(Math.random() * (maxInterval - minInterval + 1)) + minInterval;
            }

            randomInterval--;
        }, 1000);
    }

    async trackEndedUpdate(id: string): Promise<void> {
        try {
            const requestUrl = `http://localhost:9000/close-rent/${this.rentIdMap.get(id)}`;
            const response = await fetch(requestUrl);
            if (!response.ok) {
                throw new Error(
                    `Close rent request error: ${response.status} - ${await response.text()}`
                );
            }
            this.rentIdMap.delete(id);

            this.trackerMap.get(id)?.setIsAvailable(true);
        } catch (err) {
            throw err;
        }
    }
}
