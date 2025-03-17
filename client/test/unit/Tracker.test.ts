import { describe, it, expect, vi, beforeEach } from 'vitest';
import { Tracker } from '../../src/Tracker';
import { KafkaManager } from '../../src/KafkaManager';
import { TrackFetcher } from '../../src/TrackFetcher';
import { GeoPoint } from '../../src/GeoPoint';

import { TYPES } from '../../src/config/InversifyType';
import { TrackerSubject } from '../../src/TrackerSubject';
import { SimulatorObserver } from '../../src/SimulatorObserver';
import { Producer } from 'kafkajs';
import { env } from '../../src/config/EnvManager';

describe('Tracker', () => {
    let kafkaManagerMock: KafkaManager;

  beforeEach(() => {
    kafkaManagerMock = {
        initAndConnectConsumer: vi.fn(),
        initAndConnectProducer: vi.fn(),
        disconnectProducer: vi.fn(),
        disconnectConsumer: vi.fn(),
        sendMessage: vi.fn(),
    } as unknown as KafkaManager;
  });

  it('should call fetchTrack and move methods when activate is called', async () => {
    const trackerId = 'tracker-1';
    const tracker = new Tracker(trackerId, kafkaManagerMock);

    const trackFetcherMock = {
        fetchTrack: vi.fn().mockResolvedValue([new GeoPoint(1, 1), new GeoPoint(2, 2)]),
    } as unknown as TrackFetcher;

    vi.spyOn(TrackFetcher.prototype, 'fetchTrack').mockImplementation(trackFetcherMock.fetchTrack);

    await tracker.activate();

    expect(trackFetcherMock.fetchTrack).toHaveBeenCalled();
  });

  it('should handle error when fetchTrack throws an exception', async () => {
    const trackerId = 'tracker-1';
    const tracker = new Tracker(trackerId, kafkaManagerMock);

    const trackFetcherMock = {
        fetchTrack: vi.fn().mockRejectedValue(new Error('Fetch error')),
    } as unknown as TrackFetcher;

    vi.spyOn(TrackFetcher.prototype, 'fetchTrack').mockImplementation(trackFetcherMock.fetchTrack);

    await tracker.activate();

    expect(trackFetcherMock.fetchTrack).toHaveBeenCalled();
  });

  it('should initialize and connect consumer when listenToAdv is called', async () => {
    const trackerId = 'tracker-1';
    const tracker = new Tracker(trackerId, kafkaManagerMock);

    await tracker.activate();

    expect(kafkaManagerMock.initAndConnectConsumer).toHaveBeenCalledWith('adv-data', 'trackers', expect.any(Function));
  });

  it('should handle error when initAndConnectConsumer throws an exception', async () => {
    const trackerId = 'tracker-1';
    const tracker = new Tracker(trackerId, kafkaManagerMock);

    vi.spyOn(kafkaManagerMock, 'initAndConnectConsumer').mockRejectedValue(new Error('Consumer error'));

    await tracker.activate();

    expect(kafkaManagerMock.initAndConnectConsumer).toHaveBeenCalledWith('adv-data', 'trackers', expect.any(Function));
  });


  it('should return the correct availability status when getIsAvailable is called', () => {
    const trackerId = 'tracker-1';
    const tracker = new Tracker(trackerId, kafkaManagerMock);

    expect(tracker.getIsAvailable()).toBe(true);
  });
});