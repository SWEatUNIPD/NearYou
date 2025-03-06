import polyline = require('@mapbox/polyline');
import { GeoPoint } from './GeoPoint';
import { TrackerSubject } from './TrackerSubject'

export class Tracker extends TrackerSubject {
    private readonly mapCenter: GeoPoint = new GeoPoint(45.406434, 11.876761);
    private readonly mapRadiusKm: number = 3;
    private readonly maxNumTrackPoints: number = 1000;

    private id: string;

    constructor(id: string) {
        super();

        this.id = id;
    }

    async activate(): Promise<void> {
        const radiusGeoPoint = GeoPoint.radiusKmToGeoPoint(this.mapRadiusKm);
        const startGeoPoint = this.mapCenter.generateRandomPoint(radiusGeoPoint);
        const destGeoPoint = this.mapCenter.generateRandomPoint(radiusGeoPoint);

        const trackPoints = await this.fetchTrack(startGeoPoint, destGeoPoint);
        await this.move(trackPoints);
    }

    private async fetchTrack(startGeoPoint: GeoPoint, destGeoPoint: GeoPoint): Promise<GeoPoint[]> {
        const osrmUrl = 'http://router.project-osrm.org/route/v1/cycling';
        const requestUrl = `${osrmUrl}/${startGeoPoint.getLongitude()},${startGeoPoint.getLatitude()};${destGeoPoint.getLongitude()},${destGeoPoint.getLatitude()}`;

        const response = await fetch(
            requestUrl + '?overview=full&geometries=polyline'
        );

        if (!response.ok) {
            throw new Error(
                `Request error: ${response.status} - ${await response.text()}`
            );
        }

        const routeData = await response.json();
        const encodedPolyline = routeData.routes[0].geometry;

        const trackPoints = polyline.decode(encodedPolyline);

        let sampledPoints;
        if (this.maxNumTrackPoints < trackPoints.length) {
            const step = Math.floor(trackPoints.length / this.maxNumTrackPoints);
            sampledPoints = trackPoints
                .filter((_, index) => index % step == 0)
                .slice(0, this.maxNumTrackPoints);
        } else {
            sampledPoints = trackPoints;
        }

        return sampledPoints.map(([latitude, longitude]): GeoPoint => {
            {
                return new GeoPoint(latitude, longitude);
            }
        });
    }

    private async move(trackPoints: GeoPoint[]): Promise<void> {
        // consume path points
        console.log("start move tracker " + this.id);

        this.notify();
    }

    private receiveAdv(adv: string): void {

    }
}