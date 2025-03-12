import { env } from './config/EnvManager';
import { TrackFetcher } from './TrackFetcher';
import { GeoPoint } from './GeoPoint';
import { TrackerSubject } from './TrackerSubject';
import { KafkaManager } from './KafkaManager';
import { Consumer, EachMessagePayload, Producer } from 'kafkajs';
import { injectable } from 'inversify';

@injectable()
// Classe che rappresenta un tracker che invia dati GPS a un broker Kafka
export class Tracker extends TrackerSubject {
    private consumer!: Consumer;

    constructor(
        private id: string
    ) {
        super();
    }

    // Metodo per attivare il tracker
    async activate(): Promise<void> {
        await this.listenToAdv();

        let trackFetcher = new TrackFetcher();
        let trackPoints = await trackFetcher.fetchTrack();
        await this.move(trackPoints);
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

        this.consumer = await KafkaManager.getInstance().initAndConnectConsumer('adv-data', 'trackers', eachMessageHandler);
    }

    // Metodo privato per muovere il tracker lungo i punti della traccia
    private async move(trackPoints: GeoPoint[]): Promise<void> {
        const producer: Producer = await KafkaManager.getInstance().initAndConnectProducer();

        let currIndex = 0;
        const sendingIntervalMilliseconds = Number(env.SENDING_INTERVAL_MILLISECONDS);
        const intervalId = setInterval(async () => {
            if (currIndex == trackPoints.length) {
                await KafkaManager.getInstance().disconnectProducer(producer);
                if (this.consumer != null) {
                    await KafkaManager.getInstance().disconnectConsumer(this.consumer);
                }
                
                clearInterval(intervalId);
                this.notifyTrackEnded();
            }
            
            let trackerId: string = this.id;
            let latitude: number = trackPoints[currIndex].getLatitude();
            let longitude: number = trackPoints[currIndex].getLongitude();
            let message: string = JSON.stringify({
                trackerId,
                latitude,
                longitude
            });

            await KafkaManager.getInstance().sendMessage(producer, 'gps-data', message);

            currIndex++;
        }, sendingIntervalMilliseconds);
    }
}