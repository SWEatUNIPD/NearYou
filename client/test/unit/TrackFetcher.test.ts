import { env } from '../../src/config/EnvManager';
import { GeoPoint } from '../../src/GeoPoint';
import { TrackFetcher } from '../../src/TrackFetcher';
import polyline from '@mapbox/polyline';
import { vi, describe, it, expect } from 'vitest';

describe('TrackFetcher', () => {
    // Test che verifica se il metodo fetchTrack restituisce un array di GeoPoint
    it('Verifica se il metodo fetchTrack restituisce un array di GeoPoint', async () => {
        const trackerFetcher = new TrackFetcher();
        const trackPoints = await trackerFetcher.fetchTrack();
        expect(trackPoints).toBeInstanceOf(Array);
        expect(trackPoints[0]).toBeInstanceOf(GeoPoint);
    });

    // Test che verifica se viene lanciato un errore quando la richiesta fallisce
    it('Verifica se viene lanciato un errore quando la richiesta fallisce', async () => {
        const trackerFetcher = new TrackFetcher();
        const response = {
            ok: false,
            status: 500,
            text: async () => 'Internal Server Error',
        } as Response;
        vi.spyOn(global, 'fetch').mockResolvedValueOnce(response);
        await expect(trackerFetcher.fetchTrack()).rejects.toThrow('Track request error: 500 - Internal Server Error');
    });

    // Test che verifica se la polilinea viene decodificata correttamente
    it('Verifica se la polilinea viene decodificata correttamente', async () => {
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

    // Test che verifica se i punti vengono campionati correttamente quando il numero di punti supera MAX_NUM_TRACK_POINTS
    it('Verifica se i punti vengono campionati correttamente quando il numero di punti supera MAX_NUM_TRACK_POINTS', async () => {
        const trackerFetcher = new TrackFetcher();

        // Simula una polilinea molto lunga
        const mockTrackPoints: [number, number][] = Array.from({ length: 2000 }, (_, i) => [i, i]);
        const mockEncodedPolyline = polyline.encode(mockTrackPoints);

        const response = {
            ok: true,
            json: async () => ({
                routes: [{ geometry: mockEncodedPolyline }],
            }),
        } as Response;

        vi.spyOn(global, 'fetch').mockResolvedValueOnce(response);

        const sampledPoints = await trackerFetcher.fetchTrack();

        // Verifica che il numero di punti campionati sia esattamente MAX_NUM_TRACK_POINTS
        expect(sampledPoints.length).toBe(Number(env.MAX_NUM_TRACK_POINTS));

        // Verifica che i punti siano stati campionati correttamente
        const step = Math.floor(mockTrackPoints.length / Number(env.MAX_NUM_TRACK_POINTS));
        const expectedSampledPoints = mockTrackPoints
            .filter((_, index) => index % step === 0)
            .slice(0, Number(env.MAX_NUM_TRACK_POINTS))
            .map(([latitude, longitude]) => new GeoPoint(latitude, longitude));

        expect(sampledPoints).toEqual(expectedSampledPoints);
    });

    // Test che verifica se il metodo request restituisce una risposta valida
    it('Verifica se il metodo request restituisce una risposta valida', async () => {
        const trackerFetcher = new TrackFetcher();
        const response = {
            ok: true,
            json: async () => ({
                routes: [{ geometry: 'gfo}EtohhUxD@bAxJmGF' }],
            }),
        } as Response;
        vi.spyOn(global, 'fetch').mockResolvedValueOnce(response);
        const result = await trackerFetcher['request']();
        expect(result).toBe(response);
    });
});