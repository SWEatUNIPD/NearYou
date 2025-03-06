"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const uuid_1 = require("uuid");
const Tracker_1 = require("./Tracker");
const Rent_1 = require("./Rent");
const Simulator_1 = require("./Simulator");
const INIT_RENT_COUNT = 3;
let rentList = [];
for (let i = 0; i < INIT_RENT_COUNT; i++) {
    let trk = new Tracker_1.Tracker((0, uuid_1.v4)(), "gps-data");
    let rent = new Rent_1.Rent((0, uuid_1.v4)(), trk);
    rentList.push(rent);
}
let sim = new Simulator_1.Simulator(rentList);
sim.startSimulation();
//# sourceMappingURL=App.js.map