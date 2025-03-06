import { APIGateway } from './APIGateway';
import { GeoPoint } from './GeoPoint'
import { TrackerSubject } from './TrackerSubject'
import { connectProducer, disconnectProducer, sendMessage } from './Producer';

export class Tracker extends TrackerSubject {
    private readonly sendingIntervalMilliseconds = 3000;
    private id: string;
    private kafkaTopic: string;

    constructor(id: string, kafkaTopic: string) {
        super();

        this.id = id;
        this.kafkaTopic = kafkaTopic;
    }

    async activate(): Promise<void> {
        let apiGateway = new APIGateway();
        let trackPoints = await apiGateway.fetchTrack();

        this.move(trackPoints);
    }

    private async move(trackPoints: GeoPoint[]): Promise<void> {
        connectProducer();

        let currIndex = 0;
        const intervalId = setInterval(() => {
            if (currIndex < trackPoints.length - 1) {
                let trackerId = this.id;
                let latitude = trackPoints[currIndex].getLatitude();
                let longitude = trackPoints[currIndex].getLongitude();
                let message = JSON.stringify({
                    trackerId,
                    latitude,
                    longitude
                });

                sendMessage(this.kafkaTopic, message);

                currIndex++;
            } else {
                clearInterval(intervalId);
            }
        }, this.sendingIntervalMilliseconds);

        disconnectProducer();

        this.notifyTrackEnded();
    }
}