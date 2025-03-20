import { Container, ResolutionContext } from 'inversify';
import { Simulator } from '../Simulator';
import { Tracker } from '../Tracker';
import { TYPES } from './InversifyType';
import { env } from './EnvManager';
import { KafkaManager } from '../KafkaManager';
import { Kafka, KafkaConfig } from 'kafkajs';

export const container = new Container();

container
    .bind<KafkaManager>(TYPES.KafkaManager)
    .toDynamicValue((): KafkaManager => {
        const kafkaConfig: KafkaConfig = {
            clientId: env.CLIENT_ID,
            brokers: [env.BROKER ?? 'localhost:9094']
        };
        const kafka: Kafka = new Kafka(kafkaConfig);
        return new KafkaManager(kafka);
    })
    .inSingletonScope();

container
    .bind<Tracker[]>(TYPES.TrackerList)
    .toDynamicValue((context: ResolutionContext): Tracker[] => {
        const kafkaManager: KafkaManager = context.get<KafkaManager>(TYPES.KafkaManager);
        let trackerList: Tracker[] = [];
        for (let i = 1; i <= Number(env.INIT_TRACKER_COUNT); i++) {
            const id = i.toString();
            const tracker: Tracker = new Tracker(id, kafkaManager);
            trackerList.push(tracker);
        }
        return trackerList;
    });

container.bind(Simulator).toSelf().inSingletonScope();
