import { v4 as uuidv4 } from 'uuid';
import { Tracker } from './Tracker';
import { Rent } from './Rent';
import { Simulator } from './Simulator';
import { KafkaManager } from './KafkaManager';
import { KafkaConfig } from 'kafkajs';

const INIT_RENT_COUNT = 3;

const kafkaConfig: KafkaConfig = {
    clientId: 'simulator',
    brokers: [process.env.BROKER ?? 'localhost:9094']
};
const kafkaManager = new KafkaManager(kafkaConfig);

let rentList: Rent[] = [];

for (let i = 0; i < INIT_RENT_COUNT; i++) {
    let trk = new Tracker(uuidv4(), kafkaManager);
    let rent = new Rent(uuidv4(), trk);
    rentList.push(rent);
}

let sim = new Simulator(rentList);

sim.startSimulation();