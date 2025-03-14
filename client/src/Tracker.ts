import { env } from './config/EnvManager';
import { TrackFetcher } from './TrackFetcher';
import { GeoPoint } from './GeoPoint';
import { TrackerSubject } from './TrackerSubject';
import { KafkaManager } from './KafkaManager';
import { Consumer, EachMessagePayload, Producer } from 'kafkajs';
import { inject, injectable } from 'inversify';
import { TYPES } from './config/InversifyType';

@injectable()
// Classe che rappresenta un tracker che invia dati GPS a un broker Kafka
export class Tracker extends TrackerSubject {
    private consumer!: Consumer;

    constructor(
        private id: string,
        @inject(TYPES.KafkaManager)
        private kafkaManager: KafkaManager
    ) {
        super();
    }

    // Metodo per attivare il tracker
    async activate(): Promise<void> {
        // await this.listenToAdv();

        let trackFetcher = new TrackFetcher();
        try {
            let trackPoints = await trackFetcher.fetchTrack();
            await this.move(trackPoints);
        } catch (err) {
            console.error(err);
        }
    }

    // Metodo privato per ascoltare i messaggi di advertising
    private async listenToAdv(): Promise<void> {
        const eachMessageHandler = async (payload: EachMessagePayload) => {
            const { topic, partition, message } = payload;
            console.log({
                topic,
                partition,
                key: message.key?.toString(),
                value: message.value?.toString(),
            });
        };

        try {
            this.consumer = await this.kafkaManager.initAndConnectConsumer('adv-data', 'trackers', eachMessageHandler);
        } catch (err) {
            console.error(`Error caught trying to initialize and connect the consumer.\n${err}`);
        }
    }

    // Metodo privato per muovere il tracker lungo i punti della traccia
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

                    clearInterval(intervalId);
                    this.notifyTrackEnded();
                }

                console.log(`${currIndex} / ${trackPoints.length} : ${trackPoints[currIndex]}`);
                let trackerId: string = this.id;
                let latitude: number = trackPoints[currIndex].getLatitude();
                let longitude: number = trackPoints[currIndex].getLongitude();
                let message: string = JSON.stringify({
                    trackerId,
                    latitude,
                    longitude
                });

                await this.kafkaManager.sendMessage(producer, 'gps-data', message);

                currIndex++;
            }, sendingIntervalMilliseconds);
        } catch (err) {
            console.error(`Error caught trying to move the tracker along the path.\n${err}`);
        }
    }
}