import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { Tracker } from '../../src/Tracker';
import { KafkaManager } from '../../src/KafkaManager';
import { GeoPoint } from '../../src/GeoPoint';
import { env } from '../../src/config/EnvManager';

describe('Tracker', () => {
    let kafkaManagerMock: KafkaManager;
    let tracker: Tracker;

    beforeEach(() => {
        kafkaManagerMock = {
            initAndConnectProducer: vi.fn().mockResolvedValue({
                send: vi.fn(),
                disconnect: vi.fn()
            }),
            disconnectProducer: vi.fn().mockResolvedValue(undefined),
            initAndConnectConsumer: vi.fn().mockResolvedValue({
                disconnect: vi.fn()
            }),
            disconnectConsumer: vi.fn().mockResolvedValue(undefined),
            sendMessage: vi.fn().mockResolvedValue(undefined)
        } as unknown as KafkaManager;
    
        tracker = new Tracker('tracker-1', kafkaManagerMock);
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it('should move along the track points and send messages', async () => {
        const trackPoints = [new GeoPoint(1, 1), new GeoPoint(2, 2)];
        console.log("------------------------- 0 -------------------------");
        console.log(`trackPoints.length: ${trackPoints.length}`);
    
        const sendMessageSpy = vi.spyOn(kafkaManagerMock, 'sendMessage');
    
        console.log("------------------------- 1 -------------------------");
        console.log(`sendMessageSpy chiamata: ${sendMessageSpy.mock.calls.length} volte`);
        console.log("Argomenti di sendMessageSpy:", sendMessageSpy.mock.calls);
        console.log(`trackPoints.length: ${trackPoints.length}`);
    
        const disconnectProducerSpy = vi.spyOn(kafkaManagerMock, 'disconnectProducer');
        const disconnectConsumerSpy = vi.spyOn(kafkaManagerMock, 'disconnectConsumer');
    
        console.log("------------------------- 3 -------------------------");
        console.log(`sendMessageSpy chiamata: ${sendMessageSpy.mock.calls.length} volte`);
        console.log("Argomenti di sendMessageSpy:", sendMessageSpy.mock.calls);
        console.log(`trackPoints.length: ${trackPoints.length}`);
    
        const notifyTrackEndedSpy = vi.spyOn(tracker as any, 'notifyTrackEnded').mockResolvedValue(undefined);
        
        vi.useFakeTimers();
        
        await tracker['move'](trackPoints);
        
        vi.advanceTimersByTime(Number(env.SENDING_INTERVAL_MILLISECONDS) * (trackPoints.length + 1));
    
        console.log(`env.SENDING_INTERVAL_MILLISECONDS: ${env.SENDING_INTERVAL_MILLISECONDS}`);
        console.log(`trackPoints.length + 1: ${trackPoints.length + 1}`);
        console.log(`trackPoints.length: ${trackPoints.length}`);
        console.log(`Tot: ${Number(env.SENDING_INTERVAL_MILLISECONDS) * (trackPoints.length + 1)}`);
        
        console.log("------------------------- 4 -------------------------");
        console.log(`sendMessageSpy chiamata: ${sendMessageSpy.mock.calls.length} volte`);
        console.log("Argomenti di sendMessageSpy:", sendMessageSpy.mock.calls);
        console.log(`trackPoints.length: ${trackPoints.length}`);
        
        expect(sendMessageSpy).toHaveBeenCalledTimes(Number(trackPoints.length + 1));
        //expect(disconnectProducerSpy).toHaveBeenCalled();
        //expect(disconnectConsumerSpy).toHaveBeenCalled();
        //expect(notifyTrackEndedSpy).toHaveBeenCalledWith('tracker-1');
        
        vi.useRealTimers();
    });
});
