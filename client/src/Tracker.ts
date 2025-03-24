import { env } from './config/EnvManager';
import { TrackFetcher } from './TrackFetcher';
import { GeoPoint } from './GeoPoint';
import { KafkaManager } from './KafkaManager';
import { Consumer, EachMessagePayload, Producer } from 'kafkajs';
import { inject, injectable } from 'inversify';
import { TYPES } from './config/InversifyType';

@injectable()
export class Tracker {
    private isAvailable = true;
    private consumer!: Consumer;

    constructor(
        private id: string,
        @inject(TYPES.KafkaManager)
        private kafkaManager: KafkaManager
    ) {}

    async activate(): Promise<void> {
        this.isAvailable = false;

        await this.listenToAdv();

        const trackFetcher = new TrackFetcher();
        try {
            const trackPoints = await trackFetcher.fetchTrack();
            await this.move(trackPoints);
        } catch (err) {
            console.error(err);
        }
    }    

    private async listenToAdv(): Promise<void> {
        const eachMessageHandler = async (payload: EachMessagePayload) => { payload };
    
        try {
            this.consumer = await this.kafkaManager.initAndConnectConsumer('adv-data', 'trackers', eachMessageHandler);
        } catch (err) {
            console.error(`Error caught trying to initialize and connect the consumer.\n${err}`);
        }
    }

    private async move(trackPoints: GeoPoint[]): Promise<void> {
        try {
            const producer: Producer = await this.kafkaManager.initAndConnectProducer();

            let currIndex = 0;
            const sendingIntervalMilliseconds = Number(env.SENDING_INTERVAL_MILLISECONDS);
            const intervalId = setInterval(async () => {
                if (currIndex == trackPoints.length) {
                    await this.kafkaManager.disconnectProducer(producer);
                    if (this.consumer != null) {
                        await this.kafkaManager.disconnectConsumer(this.consumer);
                    }

                    this.isAvailable = true;
                    clearInterval(intervalId);
                }
                
                const timestamp: number = Date.now();
                const trackerId: string = this.id;
                const latitude: number = trackPoints[currIndex].getLatitude();
                const longitude: number = trackPoints[currIndex].getLongitude();
                const message: string = JSON.stringify({
                    timestamp: timestamp,
                    rentId: trackerId,
                    latitude: latitude,
                    longitude: longitude
                });

                await this.kafkaManager.sendMessage(producer, 'gps-data', message);

                currIndex++;
            }, sendingIntervalMilliseconds);
        } catch (err) {
            console.error(`Error caught trying to move the tracker along the path.\n${err}`);
        }
    }

    getIsAvailable(): boolean {
        return this.isAvailable;
    }
}