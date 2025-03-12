import { env } from './config/EnvManager';
import polyline from "@mapbox/polyline";
import { GeoPoint } from "./GeoPoint";

// Classe che si occupa di recuperare i punti di una traccia geografica
export class TrackFetcher {
    private mapCenter: GeoPoint;
    private mapRadiusKm: number;
    private maxNumTrackPoints: number;

    constructor() {
        // Inizializza le proprietà della classe con i valori di configurazione
        this.mapCenter = new GeoPoint(Number(env.MAP_CENTER_LAT), Number(env.MAP_CENTER_LON));
        this.mapRadiusKm = Number(env.MAP_RADIUS_KM);
        this.maxNumTrackPoints = Number(env.MAX_NUM_TRACK_POINTS);
    }

    // Metodo che recupera i punti di una traccia geografica
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

    // Metodo privato che effettua una richiesta per ottenere i dati della traccia
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
