import polyline from '@mapbox/polyline';
import fs from 'fs';
import path from 'path';

class Point {
    constructor(latitude, longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}

export class TrackSimulator {
    constructor(
        sensorIdList,
        mapCenter = new Point(45.406434, 11.876761),
        mapRadiusKm = 3,
        maxNumTrackPoints = 1000,
        retrievingInterval = 3000,
        dataDir = './sensor_data',
        override = true
    ) {
        this.sensorIdList = sensorIdList;
        this.mapCenter = mapCenter;
        this.mapRadiusKm = mapRadiusKm;
        this.maxNumTrackPoints = maxNumTrackPoints;
        this.retrievingInterval = retrievingInterval;
        this.dataDir = getValidDir(dataDir, override);
    }

    getSensorDataFile(sensorId) {
        return path.join(this.dataDir, `sensor-${sensorId}.txt`);
    }

    async getPointsOnTrack(start, dest) {
        const osrmUrl = 'http://router.project-osrm.org/route/v1/cycling';
        const requestUrl = `${osrmUrl}/${start.longitude},${start.latitude};${dest.longitude},${dest.latitude}`;

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
            const step = Math.floor(
                trackPoints.length / this.maxNumTrackPoints
            );
            sampledPoints = trackPoints
                .filter((_, index) => index % step == 0)
                .slice(0, this.maxNumTrackPoints);
        } else {
            sampledPoints = trackPoints;
        }

        return sampledPoints.map(([lat, lon]) => new Point(lat, lon));
    }

    saveSensorData(sensorId, trackPoints) {
        let dataStr = '';

        const startTime = getRandomTimeInCurrentDay();

        for (let i = 0; i < trackPoints.length; i++) {
            const point = trackPoints[i];

            let timeValue = new Date(
                startTime.getTime() + i * this.retrievingInterval
            );
            timeValue = timeValue
                .toISOString()
                .replace('T', ' ')
                .replace('Z', '');

            dataStr += `${sensorId},${timeValue},${point.latitude},${point.longitude}\n`;
        }

        const dataFileName = this.getSensorDataFile(sensorId);
        fs.writeFileSync(dataFileName, dataStr);
    }

    async run() {
        const [latDeg, lonDeg] = radiusKmToDeg(this.mapRadiusKm);

        for (let sensorId of this.sensorIdList) {
            const start = generateRandomPoint(this.mapCenter, latDeg, lonDeg);
            const dest = generateRandomPoint(this.mapCenter, latDeg, lonDeg);

            const trackPoints = await this.getPointsOnTrack(
                start,
                dest,
                this.maxNumTrackPoints
            );
            this.saveSensorData(
                sensorId,
                trackPoints,
                this.retrievingInterval,
                this.dataDir
            );
        }
    }
}

function getValidDir(dir, override) {
    // check if dir is invalid or equals to the current directory
    if (!dir || dir == '.') return '.';

    if (!fs.existsSync(dir)) {
        // if the directory does not exist, create the directory
        fs.mkdirSync(dir, { recursive: true });
        return dir;
    } else if (!override) {
        // if the directory exists and the data must not to be overrode, check if the directory is not empty
        const files = fs.readdirSync(dir);
        if (files.length != 0) {
            // if not empty, create a new directory with " - copy" appended
            return getValidDir(`${dir} - copy`);
        }
    }

    // existing empty directory to override
    return dir;
}

function radiusKmToDeg(radiusKm) {
    const rLatDeg = radiusKm / 111;
    const rLonDeg = radiusKm / (111 * Math.cos(rLatDeg * (Math.PI / 180)));
    return [rLatDeg, rLonDeg];
}

function generateRandomPoint(center, latDeg, lonDeg) {
    const deltaLat = (Math.random() * 2 - 1) * latDeg;
    const deltaLon = (Math.random() * 2 - 1) * lonDeg;

    return {
        latitude: center.latitude + deltaLat,
        longitude: center.longitude + deltaLon,
    };
}

function getRandomTimeInCurrentDay() {
    const now = new Date();

    const hours = Math.floor(Math.random() * 24);
    const minutes = Math.floor(Math.random() * 60);
    const seconds = Math.floor(Math.random() * 60);

    const randomDateTime = new Date(
        now.getFullYear(),
        now.getMonth(),
        now.getDate(),
        hours,
        minutes,
        seconds
    );
    return randomDateTime;
}
