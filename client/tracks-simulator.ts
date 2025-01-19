import polyline from '@mapbox/polyline';
import fs from 'fs';
import path from 'path';

type point = {
  latitude: number;
  longitude: number;
};

export class TrackSimulator {
  sensorId: number;
  mapCenter: point;
  mapRadiusKm: number;
  maxNumTrackPoints: number;
  retrievingInterval: number;
  dataDir: string;
  override: boolean;

  constructor(
    sensorId: number,
    mapCenter: point = { latitude: 45.406434, longitude: 11.876761 },
    mapRadiusKm: number = 3,
    maxNumTrackPoints: number = 1000,
    retrievingInterval: number = 3000,
    dataDir: string = './sensor_data',
    override: boolean = true
  ) {
    this.sensorId = sensorId;
    this.mapCenter = mapCenter;
    this.mapRadiusKm = mapRadiusKm;
    this.maxNumTrackPoints = maxNumTrackPoints;
    this.retrievingInterval = retrievingInterval;
    this.dataDir = getValidDir(dataDir, override);
    this.override = override;
  }

  getSensorDataFile(sensorId: number) {
    return path.join(this.dataDir, `sensor-${sensorId}.txt`);
  }

  async getPointsOnTrack(start: point, dest: point) {
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
      const step = Math.floor(trackPoints.length / this.maxNumTrackPoints);
      sampledPoints = trackPoints
        .filter((_, index) => index % step == 0)
        .slice(0, this.maxNumTrackPoints);
    } else {
      sampledPoints = trackPoints;
    }

    return sampledPoints.map(([latitude, longitude]): point => {
      {
        return { latitude, longitude };
      }
    });
  }

  saveSensorData(sensorId: number, trackPoints: point[]) {
    let dataStr = '';

    for (let i = 0; i < trackPoints.length; i++) {
      const point = trackPoints[i];
      dataStr += `${point.latitude},${point.longitude}\n`;
    }

    const dataFileName = this.getSensorDataFile(sensorId);
    fs.writeFileSync(dataFileName, dataStr);
  }

  async run() {
    const deg: point = radiusKmToDeg(this.mapRadiusKm);
    const start = generateRandomPoint(
      this.mapCenter,
      deg.latitude,
      deg.longitude
    );
    const dest = generateRandomPoint(
      this.mapCenter,
      deg.latitude,
      deg.longitude
    );

    const trackPoints = await this.getPointsOnTrack(start, dest);
    this.saveSensorData(this.sensorId, trackPoints);
  }
}

function getValidDir(dir: string, override: boolean = true): string {
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

function radiusKmToDeg(radiusKm: number): point {
  const rLatDeg = radiusKm / 111;
  const rLonDeg = radiusKm / (111 * Math.cos(rLatDeg * (Math.PI / 180)));
  return { latitude: rLatDeg, longitude: rLonDeg };
}

function generateRandomPoint(
  center: point,
  latDeg: number,
  lonDeg: number
): point {
  const deltaLat = (Math.random() * 2 - 1) * latDeg;
  const deltaLon = (Math.random() * 2 - 1) * lonDeg;

  return {
    latitude: center.latitude + deltaLat,
    longitude: center.longitude + deltaLon,
  };
}
