import {v4 as uuidv4} from 'uuid';
import { Tracker } from './Tracker';
import { Rent } from './Rent';
import { Simulator } from './Simulator';

const INIT_RENT_COUNT = 3;

let rentList: Rent[] = [];

for (let i = 0; i < INIT_RENT_COUNT; i++) {
    let trk = new Tracker(uuidv4(), "gps-data");
    let rent = new Rent(uuidv4(), trk);
    rentList.push(rent);
}

let sim = new Simulator(rentList);

sim.startSimulation();