import 'reflect-metadata';
import { container } from './config/Inversify.config';
import { Simulator } from './Simulator';

const simulator = container.get(Simulator);
simulator.startSimulation();
