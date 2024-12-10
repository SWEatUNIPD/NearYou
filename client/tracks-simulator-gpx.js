/*
 * xmlbuilder is required in order to build .gpx files for the tracks
 *      
 * npm install xmlbuilder
*/

import polyline from '@mapbox/polyline';
import fs from 'fs';
import path from 'path';
import xmlbuilder from 'xmlbuilder';

class Point {
    constructor(latitude, longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}

const mapCenter = new Point(45.406434, 11.876761);   // coordinates of the center of Padua in latitude and longitude
const mapRadiusKm = 3;   // radius of the map in km
const maxNumTrackPoints = 1000;   // maximum number of retrieved data in a track
const retrievingInterval = 5;   // how many seconds GPS data are retrieved
const numCustomers = 2;
let gpxDir = "./customer_tracks_gpx";
let sqlInsertDir = "./customer_tracks_sql_insert";

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
        longitude: center.longitude + deltaLon
    };
}

async function getPointsOnTrack(start, dest, maxNumPoints = 10) {
    const osrmUrl = "http://router.project-osrm.org/route/v1/cycling";
    const requestUrl = `${osrmUrl}/${start.longitude},${start.latitude};${dest.longitude},${dest.latitude}`;
    
    const response = await fetch(requestUrl + '?overview=full&geometries=polyline');
    if (!response.ok) {
        throw new Error(`Request error: ${response.status} - ${await response.text()}`);
    }

    const routeData = await response.json();
    const encodedPolyline = routeData.routes[0].geometry;
    
    const trackPoints = polyline.decode(encodedPolyline);
    
    let sampledPoints;
    if (maxNumPoints < trackPoints.length) {
        const step = Math.floor(trackPoints.length / maxNumPoints);
        sampledPoints = trackPoints.filter((_, index) => index % step == 0).slice(0, maxNumPoints);
    } else {
        sampledPoints = trackPoints;
    }

    return sampledPoints.map(([lat, lon]) => new Point(lat, lon));
}

function getRandomTimeInCurrentDay() {
    const now = new Date();

    const hours = Math.floor(Math.random() * 24);
    const minutes = Math.floor(Math.random() * 60);
    const seconds = Math.floor(Math.random() * 60);

    const randomDateTime = new Date(now.getFullYear(), now.getMonth(), now.getDate(), hours, minutes, seconds);
    return randomDateTime;
}

function getValidDir(dir) {
    // check if dir is invalid or equals to the current directory
    if (!dir || dir == ".") return ".";

    if (!fs.existsSync(dir)) {
        // if the directory does not exist, create the directory
        fs.mkdirSync(dir, { recursive: true });
        return dir;
    } else {
        // if the directory exists, check if the directory is not empty
        const files = fs.readdirSync(dir);
        if (files.length != 0) {
            // if not empty, create a new directory with " - copy" appended
            return getValidDir(`${dir} - copy`);
        } else {
            // if empty, return the original directory
            return dir;
        }
    }
}

function saveToGpxAndSqlInsert(customerId, trackPoints, retrievingInterval, gpxDir = ".", sqlInsertDir = ".") {
    let sqlInsertStr = "";

    const gpx = xmlbuilder.create("gpx", { version: "1.0", encoding: "UTF-8" })
        .att("version", "1.1")
        .att("creator", "JavaScript Script")
        .att("xmlns", "http://www.topografix.com/GPX/1/1");

    const trk = gpx.ele("trk");
    trk.ele("name", `Track ${customerId}`);
    
    const trkseg = trk.ele("trkseg");

    const startTime = getRandomTimeInCurrentDay();
    
    for (let i = 0; i < trackPoints.length; i++) {
        const point = trackPoints[i];
        const trkpt = trkseg.ele("trkpt", { lat: point.latitude.toString(), lon: point.longitude.toString() });
        
        const timeElement = trkpt.ele("time");
        const timeValue = new Date(startTime.getTime() + (i * retrievingInterval * 1000));
        timeElement.text(timeValue.toISOString());

        sqlInsertStr += `INSERT INTO SensorLocation VALUES (1, '${timeValue.toISOString()}', ${point.latitude}, ${point.longitude});\n`;
    }

    const gpxStr = gpx.end({ pretty: true });

    const gpxFileName = path.join(gpxDir, `trk_${customerId}.gpx`);
    fs.writeFileSync(gpxFileName, gpxStr);

    const sqlInsertFileName = path.join(sqlInsertDir, `sql_insert_${customerId}.sql`);
    fs.writeFileSync(sqlInsertFileName, sqlInsertStr);
}

async function main() {
    const [latDeg, lonDeg] = radiusKmToDeg(mapRadiusKm);
    gpxDir = getValidDir(gpxDir);
    sqlInsertDir = getValidDir(sqlInsertDir);

    for (let i = 0; i < numCustomers; i++) {
        const start = generateRandomPoint(mapCenter, latDeg, lonDeg);
        const dest = generateRandomPoint(mapCenter, latDeg, lonDeg);

        const trackPoints = await getPointsOnTrack(start, dest, maxNumTrackPoints);
        saveToGpxAndSqlInsert(i, trackPoints, retrievingInterval, gpxDir, sqlInsertDir);
    }
}

main();
