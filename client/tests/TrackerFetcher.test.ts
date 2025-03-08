import { GeoPoint } from '../GeoPoint';
import { TrackFetcher } from '../TrackFetcher';

describe('TrackFetcher', () => {
    it('should fetch track points', async () => {
        const trackerFetcher = new TrackFetcher();
        const trackPoints = await trackerFetcher.fetchTrack();
        expect(trackPoints).toBeInstanceOf(Array);
        expect(trackPoints[0]).toBeInstanceOf(GeoPoint);
    });

    it('should throw an error if the request fails', async () => {
        const trackerFetcher = new TrackFetcher();
        const response = {
            ok: false,
            status: 500,
            text: async () => 'Internal Server Error',
        } as Response;
        vi.spyOn(global, 'fetch').mockResolvedValueOnce(response);
        await expect(trackerFetcher.fetchTrack()).rejects.toThrow('Track request error: 500 - Internal Server Error')
    });

    it('should decode polyline correctly', async () => {
        const trackerFetcher = new TrackFetcher();
        const response = {
            ok: true,
            json: async () => ({
                routes: [{ geometry: 'gfo}EtohhUxD@bAxJmGF' }],
            }),
        } as Response;
        vi.spyOn(global, 'fetch').mockResolvedValueOnce(response);
        const trackPoints = await trackerFetcher.fetchTrack();
        expect(trackPoints.length).toBeGreaterThan(0);
        expect(trackPoints[0]).toBeInstanceOf(GeoPoint);
    });

    it('should sample points if track points exceed maxNumTrackPoints', async () => {
        const trackerFetcher = new TrackFetcher();
        const response = {
            ok: true,
            json: async () => ({
                routes: [{ geometry: 'gfo}EtohhUxD@bAxJmGF'.repeat(100) }],
            }),
        } as Response;
        vi.spyOn(global, 'fetch').mockResolvedValueOnce(response);
        const trackPoints = await trackerFetcher.fetchTrack();
        expect(trackPoints.length).toBeLessThanOrEqual(trackerFetcher['maxNumTrackPoints']);
    });
});
