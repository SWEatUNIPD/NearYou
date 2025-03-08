import { TrackFetcher } from './TrackFetcher';
import { GeoPoint } from './GeoPoint'
import { TrackerSubject } from './TrackerSubject'
// import { connectProducer, disconnectProducer, sendMessage } from './Producer';
import { KafkaManager } from './KafkaManager';
import { Consumer, EachMessagePayload, Producer } from 'kafkajs';

export class Tracker extends TrackerSubject {
    private readonly sendingIntervalMilliseconds = 3000;
    private id: string;
    private consumer: Consumer;

    constructor(id: string) {
        super();

        this.id = id;
    }

    async activate(): Promise<void> {
        await this.listenToAdv();

        let trackFetcher = new TrackFetcher();
        let trackPoints = await trackFetcher.fetchTrack();
        await this.move(trackPoints);
    }

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

    private async move(trackPoints: GeoPoint[]): Promise<void> {
        const producer: Producer = await KafkaManager.getInstance().initAndConnectProducer();

        let currIndex = 0;
        const intervalId = setInterval(() => {
            if (currIndex < trackPoints.length - 1) {
                let trackerId: string = this.id;
                let latitude: number = trackPoints[currIndex].getLatitude();
                let longitude: number = trackPoints[currIndex].getLongitude();
                let message: string = JSON.stringify({
                    trackerId,
                    latitude,
                    longitude
                });

                KafkaManager.getInstance().sendMessage(producer, 'gps-data', message);

                currIndex++;
            } else {
                clearInterval(intervalId);
            }
        }, this.sendingIntervalMilliseconds);

        await KafkaManager.getInstance().disconnectProducer(producer);
        if (this.consumer != null) {
            await KafkaManager.getInstance().disconnectConsumer(this.consumer);
        }

        this.notifyTrackEnded();
    }
}