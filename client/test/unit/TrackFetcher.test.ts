import { env } from '../../src/config/EnvManager';
import { GeoPoint } from '../../src/GeoPoint';
import { TrackFetcher } from '../../src/TrackFetcher';

import polyline from '@mapbox/polyline';

describe('TrackFetcher', () => {
    // Test che verifica se il metodo fetchTrack restituisce un array di GeoPoint (fetchTrack)
    it('Verifica se il metodo fetchTrack restituisce un array di GeoPoint', async () => {
        const trackerFetcher = new TrackFetcher();
        const trackPoints = await trackerFetcher.fetchTrack();
        expect(trackPoints).toBeInstanceOf(Array); // Verifica che trackPoints sia un array
        expect(trackPoints[0]).toBeInstanceOf(GeoPoint); // Verifica che il primo elemento dell'array sia un'istanza di GeoPoint
    });

    // Test che verifica se viene lanciato un errore quando la richiesta fallisce (fetchTrack)
    it('Verifica se viene lanciato un errore quando la richiesta fallisce', async () => {
        const trackerFetcher = new TrackFetcher();
        const response = {
            ok: false,
            status: 500,
            text: async () => 'Internal Server Error',
        } as Response;
        vi.spyOn(global, 'fetch').mockResolvedValueOnce(response); // Simula una risposta di errore
        await expect(trackerFetcher.fetchTrack()).rejects.toThrow('Track request error: 500 - Internal Server Error') // Verifica che venga lanciato un errore con il messaggio corretto
    });

    // Test che verifica se la polilinea viene decodificata correttamente (fetchTrack)
    it('Verifica se la polilinea viene decodificata correttamente', async () => {
        const trackerFetcher = new TrackFetcher();
        const response = {
            ok: true,
            json: async () => ({
                routes: [{ geometry: 'gfo}EtohhUxD@bAxJmGF' }], // Questa stringa rappresenta una polilinea codificata, un formato comune per rappresentare percorsi geografici in modo compatto.
            }),
        } as Response;
        vi.spyOn(global, 'fetch').mockResolvedValueOnce(response); // Simula una risposta con una polilinea codificata
        const trackPoints = await trackerFetcher.fetchTrack();
        expect(trackPoints.length).toBeGreaterThan(0); // Verifica che ci siano punti nella traccia
        expect(trackPoints[0]).toBeInstanceOf(GeoPoint); // Verifica che il primo punto sia un'istanza di GeoPoint
    });

    // Test che verifica se i punti vengono campionati correttamente se superano MAX_NUM_TRACK_POINTS (fetchTrack)
    it('Verifica se i punti vengono campionati correttamente se superano MAX_NUM_TRACK_POINTS', async () => {
        const trackerFetcher = new TrackFetcher();
        const response = {
            ok: true,
            json: async () => ({
                routes: [{ geometry: 'gfo}EtohhUxD@bAxJmGF'.repeat(100) }], // Questa stringa rappresenta una polilinea codificata, un formato comune per rappresentare percorsi geografici in modo compatto. 
            }),
        } as Response;
        vi.spyOn(global, 'fetch').mockResolvedValueOnce(response); // Simula una risposta con una polilinea molto lunga
        const trackPoints = await trackerFetcher.fetchTrack();
        expect(trackPoints.length).toBeLessThanOrEqual(Number(env.MAX_NUM_TRACK_POINTS)); // Verifica che il numero di punti sia inferiore o uguale a maxNumTrackPoints
    });

    // Verifica se i punti vengono campionati correttamente quando il numero di punti supera MAX_NUM_TRACK_POINTS (fetchTrack)
    it('Verifica se i punti vengono campionati correttamente quando il numero di punti supera MAX_NUM_TRACK_POINTS', async () => {
        const trackerFetcher = new TrackFetcher();
        
        // Modifica: imposta manualmente maxNumTrackPoints ad un valore più basso
        const testMaxNumTrackPoints = 10;
        // TODO: alza i numeri e lascia MAX_NUM_TRACK_POINTS
        // trackerFetcher['maxNumTrackPoints'] = testMaxNumTrackPoints;

        // Simula una risposta con una polilinea lunga che genera almeno 100 punti
        const longPolyline = 'gfo}EtohhUxD@bAxJmGF'.repeat(25); // Polilinea lunga
        
        // Viene simulata una risposta HTTP che contiene la polilinea codificata
        const response = {
            ok: true,
            json: async () => ({
                routes: [{ geometry: longPolyline }],
            }),
        } as Response;

        // Mock della fetch per restituire la risposta simulata
        vi.spyOn(global, 'fetch').mockResolvedValueOnce(response);

        // Decodifica la polilinea per avere i punti originali
        const decodedPoints = polyline.decode(longPolyline);
        
        // Assicurati che ci siano abbastanza punti per il test
        expect(decodedPoints.length).toBeGreaterThan(testMaxNumTrackPoints);

        // Ottieni i punti della traccia
        const trackPoints = await trackerFetcher.fetchTrack();
        
        // Verifica che il numero di punti campionati sia <= maxNumTrackPoints
        expect(trackPoints.length).toBeLessThanOrEqual(testMaxNumTrackPoints);

        // Calcola lo step esattamente come nella funzione originale
        const step = Math.floor(decodedPoints.length / testMaxNumTrackPoints);
        
        // Calcola manualmente quali punti dovrebbero essere selezionati dopo il filtering e lo slicing
        const expectedFilteredPoints = decodedPoints
            .filter((_, index) => index % step === 0)
            .slice(0, testMaxNumTrackPoints);
        
        // Verifica che il numero di punti dopo il campionamento corrisponda a quello atteso
        expect(trackPoints.length).toBe(expectedFilteredPoints.length);

        // Verifica che i punti campionati siano quelli corretti
        trackPoints.forEach((point, index) => {
            const expectedPoint = expectedFilteredPoints[index];
            expect(point.getLatitude()).toBeCloseTo(expectedPoint[0], 5); // Verifica la latitudine
            expect(point.getLongitude()).toBeCloseTo(expectedPoint[1], 5); // Verifica la longitudine
        });
    });

    // Test che verifica se il metodo request restituisce una risposta valida (request)
    it('Verifica se il metodo request restituisce una risposta valida', async () => {
        const trackerFetcher = new TrackFetcher();
        const response = {
            ok: true,
            json: async () => ({
                routes: [{ geometry: 'gfo}EtohhUxD@bAxJmGF' }],
            }),
        } as Response;
        vi.spyOn(global, 'fetch').mockResolvedValueOnce(response); // Simula una risposta valida
        const result = await trackerFetcher['request']();
        expect(result).toBe(response); // Verifica che la risposta sia quella simulata
    });
});
