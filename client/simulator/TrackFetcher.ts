import polyline from "@mapbox/polyline";
import { GeoPoint } from "./GeoPoint";

export class TrackFetcher {
    private readonly mapCenter = new GeoPoint(45.406434, 11.876761);
    private readonly mapRadiusKm = 3;
    private readonly maxNumTrackPoints = 1000;

    async fetchTrack(): Promise<GeoPoint[]> {
        const response = await this.request();

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

    private async request(): Promise<Response> {
        const radiusGeoPoint = GeoPoint.radiusKmToGeoPoint(this.mapRadiusKm);
        const startGeoPoint = this.mapCenter.generateRandomPoint(radiusGeoPoint);
        const destGeoPoint = this.mapCenter.generateRandomPoint(radiusGeoPoint);
        
        const osrmUrl = 'http://router.project-osrm.org/route/v1/cycling';
        const requestUrl = `${osrmUrl}/${startGeoPoint.getLongitude()},${startGeoPoint.getLatitude()};${destGeoPoint.getLongitude()},${destGeoPoint.getLatitude()}`;

        const response = await fetch(
            requestUrl + '?overview=full&geometries=polyline'
        );

        if (!response.ok) {
            throw new Error(
                `Track request error: ${response.status} - ${await response.text()}`
            );
        }

        return response;
    }
}
