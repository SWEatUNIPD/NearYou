export class GeoPoint {
    private latitude: number;
    private longitude: number;

    constructor(latitude: number, longitude: number) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    static radiusKmToGeoPoint(radiusKm: number): GeoPoint {
        if (radiusKm > 10000) {
            throw new Error(
                `Radius too big, more than the distance between the equatore and the poles`
            );
        }

        if (radiusKm > 300) {
            console.warn(`Radius suggested less than 300km for accuracy reasons, current radius is ${radiusKm}km`);
        }

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