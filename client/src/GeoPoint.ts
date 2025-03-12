// La classe GeoPoint rappresenta un punto geografico con latitudine e longitudine.
export class GeoPoint {
    constructor(
        private latitude: number,
        private longitude: number
    ) {}

    // Metodo statico che converte un raggio in chilometri in un oggetto GeoPoint.
    static radiusKmToGeoPoint(radiusKm: number): GeoPoint {
        if (radiusKm > 10000) {
            throw new Error(
                `Radius too big, more than the distance between the equatore and the poles`
            );
        }

        if (radiusKm > 300) {
            console.warn(`Radius suggested less than 300km for accuracy reasons, current radius is ${radiusKm}km`);
        }

        // Calcola la differenza di latitudine e longitudine in gradi.
        const rLatDeg: number = radiusKm / 111;
        const rLonDeg: number = radiusKm / (111 * Math.cos(rLatDeg * (Math.PI / 180)));
        
        return new GeoPoint(rLatDeg, rLonDeg);
    }

    // Genera un punto casuale all'interno di un raggio specificato da un oggetto GeoPoint.
    generateRandomPoint(radiusGeoPoint: GeoPoint): GeoPoint {
        const deltaLat = (Math.random() * 2 - 1) * radiusGeoPoint.getLatitude();
        const deltaLon = (Math.random() * 2 - 1) * radiusGeoPoint.getLongitude();

        return new GeoPoint(this.latitude + deltaLat, this.longitude + deltaLon);
    }

    // Restituisce la latitudine del punto.
    getLatitude(): number {
        return this.latitude;
    }

    // Restituisce la longitudine del punto.
    getLongitude(): number {
        return this.longitude;
    }
}