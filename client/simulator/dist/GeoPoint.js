"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.GeoPoint = void 0;
class GeoPoint {
    constructor(latitude, longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    static radiusKmToGeoPoint(radiusKm) {
        const rLatDeg = radiusKm / 111;
        const rLonDeg = radiusKm / (111 * Math.cos(rLatDeg * (Math.PI / 180)));
        return new GeoPoint(rLatDeg, rLonDeg);
    }
    generateRandomPoint(radiusGeoPoint) {
        const deltaLat = (Math.random() * 2 - 1) * radiusGeoPoint.getLatitude();
        const deltaLon = (Math.random() * 2 - 1) * radiusGeoPoint.getLongitude();
        return new GeoPoint(this.latitude + deltaLat, this.longitude + deltaLon);
    }
    getLatitude() {
        return this.latitude;
    }
    getLongitude() {
        return this.longitude;
    }
}
exports.GeoPoint = GeoPoint;
//# sourceMappingURL=GeoPoint.js.map