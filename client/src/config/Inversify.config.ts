import { Container, ResolutionContext } from 'inversify';
import { Rent } from '../Rent';
import { Simulator } from '../Simulator';
import { Tracker } from '../Tracker';
import { TYPES } from './InversifyType';
import { v4 as uuidv4 } from 'uuid';
import { env } from './EnvManager';
import { KafkaManager } from '../KafkaManager';
import { Kafka, KafkaConfig } from 'kafkajs';

// Crea un nuovo contenitore Inversify per la gestione delle dipendenze
const container = new Container();

container
    .bind<KafkaManager>(TYPES.KafkaManager)
    .toDynamicValue(() => {
        const kafkaConfig: KafkaConfig = {
            clientId: env.CLIENT_ID,
            brokers: [env.BROKER ?? 'localhost:9094']
        };
        const kafka: Kafka = new Kafka(kafkaConfig);
        return new KafkaManager(kafka);
    })
    .inSingletonScope();

// Configura il binding per Tracker
container
    .bind<Tracker>(TYPES.Tracker)
    .toDynamicValue((context: ResolutionContext) => {
        const kafkaManager: KafkaManager = context.get<KafkaManager>(TYPES.KafkaManager);
        const tracker: Tracker = new Tracker(uuidv4(), kafkaManager);
        return tracker;
    });

// Configura il binding per Rent
container
    .bind<Rent>(TYPES.Rent)
    .toDynamicValue((context: ResolutionContext) => {
        const tracker: Tracker = context.get<Tracker>(TYPES.Tracker);
        const rent: Rent = new Rent(uuidv4(), tracker);
        return rent;
    });

// Configura il binding per la lista di Rent
container
    .bind<Rent[]>(TYPES.RentList)
    .toDynamicValue((context: ResolutionContext) => {
        let rentList: Rent[] = [];
        for (let i = 0; i < Number(env.INIT_RENT_COUNT); i++) {
            const rent: Rent = context.get<Rent>(TYPES.Rent);
            rentList.push(rent);
        }
        return rentList;
    });

// Configura il binding per Simulator
container.bind(Simulator).toSelf().inSingletonScope();

export { container }
