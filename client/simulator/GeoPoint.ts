export class GeoPoint {
    private latitude: number;
    private longitude: number;

    constructor(latitude: number, longitude: number) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    static radiusKmToGeoPoint(radiusKm: number): GeoPoint {
        const rLatDeg: number = radiusKm / 111;
        const rLonDeg: number = radiusKm / (111 * Math.cos(rLatDeg * (Math.PI / 180)));
        
        return new GeoPoint(rLatDeg, rLonDeg);
    }

    generateRandomPoint(radiusGeoPoint: GeoPoint): GeoPoint {
        const deltaLat = (Math.random() * 2 - 1) * radiusGeoPoint.getLatitude();
        const deltaLon = (Math.random() * 2 - 1) * radiusGeoPoint.getLongitude();

        return new GeoPoint(this.latitude + deltaLat, this.longitude + deltaLon);
    }

    getLatitude(): number {
        return this.latitude;
    }

    getLongitude(): number {
        return this.longitude;
    }
}