import { env } from './EnvManager';
import { v4 as uuidv4 } from 'uuid';
import { Tracker } from './Tracker';
import { Rent } from './Rent';
import { Simulator } from './Simulator';

const initRentCount = Number(env.INIT_RENT_COUNT);

let rentList: Rent[] = [];

for (let i = 0; i < initRentCount; i++) {
    const trk = new Tracker(uuidv4());
    const rent = new Rent(uuidv4(), trk);
    rentList.push(rent);
}

const sim = new Simulator(rentList);

sim.startSimulation();