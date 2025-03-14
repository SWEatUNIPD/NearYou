import { env } from './config/EnvManager';
import polyline from "@mapbox/polyline";
import { GeoPoint } from "./GeoPoint";

// Classe che si occupa di recuperare i punti di una traccia geografica
export class TrackFetcher {
    // Metodo che recupera i punti di una traccia geografica
    async fetchTrack(): Promise<GeoPoint[]> {
        try {
            const response = await this.request();

            const routeData = await response.json();
            const encodedPolyline = routeData.routes[0].geometry;

            const trackPoints = polyline.decode(encodedPolyline);

            let sampledPoints;
            const maxNumTrackPoints = Number(env.MAX_NUM_TRACK_POINTS);
            if (maxNumTrackPoints < trackPoints.length) {
                const step = Math.floor(trackPoints.length / maxNumTrackPoints);
                sampledPoints = trackPoints
                    .filter((_, index) => index % step == 0)
                    .slice(0, maxNumTrackPoints);
            } else {
                sampledPoints = trackPoints;
            }

            return sampledPoints.map(([latitude, longitude]): GeoPoint => {
                {
                    return new GeoPoint(latitude, longitude);
                }
            });
        } catch (err) {
            console.error(`Error caught trying to fetch a track.`);
            throw err;
        }
    }

    // Metodo privato che effettua una richiesta per ottenere i dati della traccia
    private async request(): Promise<Response> {
        try {
            const radiusGeoPoint = GeoPoint.radiusKmToGeoPoint(Number(env.MAP_RADIUS_KM));
            const mapCenter = new GeoPoint(Number(env.MAP_CENTER_LAT), Number(env.MAP_CENTER_LON));
            const startGeoPoint = mapCenter.generateRandomPoint(radiusGeoPoint);
            const destGeoPoint = mapCenter.generateRandomPoint(radiusGeoPoint);

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
        } catch (err) {
            console.error(`Error caught trying to generate the OSRM request.`);
            throw err;
        }
    }
}
