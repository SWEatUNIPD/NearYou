"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.APIGateway = void 0;
const polyline_1 = __importDefault(require("@mapbox/polyline"));
const GeoPoint_1 = require("./GeoPoint");
class APIGateway {
    constructor() {
        this.mapCenter = new GeoPoint_1.GeoPoint(45.406434, 11.876761);
        this.mapRadiusKm = 3;
        this.maxNumTrackPoints = 1000;
    }
    fetchTrack() {
        return __awaiter(this, void 0, void 0, function* () {
            const response = yield this.request();
            const routeData = yield response.json();
            const encodedPolyline = routeData.routes[0].geometry;
            const trackPoints = polyline_1.default.decode(encodedPolyline);
            let sampledPoints;
            if (this.maxNumTrackPoints < trackPoints.length) {
                const step = Math.floor(trackPoints.length / this.maxNumTrackPoints);
                sampledPoints = trackPoints
                    .filter((_, index) => index % step == 0)
                    .slice(0, this.maxNumTrackPoints);
            }
            else {
                sampledPoints = trackPoints;
            }
            return sampledPoints.map(([latitude, longitude]) => {
                {
                    return new GeoPoint_1.GeoPoint(latitude, longitude);
                }
            });
        });
    }
    request() {
        return __awaiter(this, void 0, void 0, function* () {
            const radiusGeoPoint = GeoPoint_1.GeoPoint.radiusKmToGeoPoint(this.mapRadiusKm);
            const startGeoPoint = this.mapCenter.generateRandomPoint(radiusGeoPoint);
            const destGeoPoint = this.mapCenter.generateRandomPoint(radiusGeoPoint);
            const osrmUrl = 'http://router.project-osrm.org/route/v1/cycling';
            const requestUrl = `${osrmUrl}/${startGeoPoint.getLongitude()},${startGeoPoint.getLatitude()};${destGeoPoint.getLongitude()},${destGeoPoint.getLatitude()}`;
            const response = yield fetch(requestUrl + '?overview=full&geometries=polyline');
            if (!response.ok) {
                throw new Error(`Request error: ${response.status} - ${yield response.text()}`);
            }
            return response;
        });
    }
}
exports.APIGateway = APIGateway;
//# sourceMappingURL=APIGateway.js.map