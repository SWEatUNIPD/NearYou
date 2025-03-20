import { inject } from 'inversify';
import { TYPES } from './config/InversifyType';
import { Tracker } from './Tracker';
import { env } from './config/EnvManager';

export class Simulator {
    constructor(
        @inject(TYPES.TrackerList)
        private trackerList: Tracker[]
    ) {}

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

    private async startRent(): Promise<void> {
        let tracker: Tracker | null = null;
        for (const trk of this.trackerList) {
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

        tracker.activate();
    }

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
}
