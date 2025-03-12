import 'reflect-metadata';
import { container } from './config/Inversify.config';
import { Simulator } from './Simulator';

// Ottiene un'istanza di Simulator dal contenitore e avvia la simulazione
const sim = container.get(Simulator);
sim.startSimulation();