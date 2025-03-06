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
Object.defineProperty(exports, "__esModule", { value: true });
exports.Tracker = void 0;
const APIGateway_1 = require("./APIGateway");
const TrackerSubject_1 = require("./TrackerSubject");
const Producer_1 = require("./Producer");
class Tracker extends TrackerSubject_1.TrackerSubject {
    constructor(id, kafkaTopic) {
        super();
        this.sendingIntervalMilliseconds = 3000;
        this.id = id;
        this.kafkaTopic = kafkaTopic;
    }
    activate() {
        return __awaiter(this, void 0, void 0, function* () {
            let apiGateway = new APIGateway_1.APIGateway();
            let trackPoints = yield apiGateway.fetchTrack();
            this.move(trackPoints);
        });
    }
    move(trackPoints) {
        return __awaiter(this, void 0, void 0, function* () {
            (0, Producer_1.connectProducer)();
            let currIndex = 0;
            const intervalId = setInterval(() => {
                if (currIndex < trackPoints.length - 1) {
                    let trackerId = this.id;
                    let latitude = trackPoints[currIndex].getLatitude();
                    let longitude = trackPoints[currIndex].getLongitude();
                    let message = JSON.stringify({
                        trackerId,
                        latitude,
                        longitude
                    });
                    (0, Producer_1.sendMessage)(this.kafkaTopic, message);
                    currIndex++;
                }
                else {
                    clearInterval(intervalId);
                }
            }, this.sendingIntervalMilliseconds);
            (0, Producer_1.disconnectProducer)();
            this.notifyTrackEnded();
        });
    }
}
exports.Tracker = Tracker;
//# sourceMappingURL=Tracker.js.map