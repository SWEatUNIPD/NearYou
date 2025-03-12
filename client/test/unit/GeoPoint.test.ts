import {GeoPoint} from '../../src/GeoPoint';

describe("Geopoint", () => {
    // Testo il costruttore di GeoPoint ed i metodi getter: getLatitude e getLongitude
    it("Test del costruttore e dei metodi getter", () =>{
        // Crea un nuovo GeoPoint con latitudine e longitudine specificate
        const geoPoint = new GeoPoint(40.7128, -74.0060);

        // Verifica che la latitudine sia corretta
        expect(geoPoint.getLatitude()).toBe(40.7128);

        // Verifica che la longitudine sia corretta
        expect(geoPoint.getLongitude()).toBe(-74.0060);
    });

    // Testo il metodo radiusKmToGeoPoint (radiusKmToGeoPoint)
    it("Test del metodo radiusKmToGeoPoint", () => {
        const radiusKm = 100;

        // Converte un raggio in chilometri in un oggetto GeoPoint
        const geoPoint = GeoPoint.radiusKmToGeoPoint(radiusKm);

        // Verifica che la latitudine calcolata sia corretta
        expect(geoPoint.getLatitude()).toBeCloseTo(0.9009, 4);

        // Verifica che la longitudine calcolata sia corretta
        expect(geoPoint.getLongitude()).toBeCloseTo(0.9010, 4);
    });

    // Testo il metodo radiusKmToGeoPoint (radiusKmToGeoPoint) con un raggio troppo grande (> 10000)
    it("Test del metodo radiusKmToGeoPoint con raggio maggiore di 10000", () => {
        const radiusKm = 10001;

        // Verifica che il metodo lanci un'eccezione con il messaggio specificato
        expect(() => {GeoPoint.radiusKmToGeoPoint(radiusKm);}).toThrowError("Radius too big, more than the distance between the equatore and the poles");
    });

    // Testo il metodo radiusKmToGeoPoint (radiusKmToGeoPoint) con un raggio grande (> 300)
    it("Test del metodo radiusKmToGeoPoint con raggio maggiore di 300", () => {
        const radiusKm = 301; // Valore maggiore di 300

        // Simula console.warn
        const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});

        // Chiama il metodo
        GeoPoint.radiusKmToGeoPoint(radiusKm);

        // Verifica che console.warn sia stato chiamato con il messaggio corretto
        expect(consoleWarnSpy).toHaveBeenCalledWith(
            `Radius suggested less than 300km for accuracy reasons, current radius is ${radiusKm}km`
        );

        // Ripristina console.warn originale
        consoleWarnSpy.mockRestore();
    });

    // Testo il metodo generateRandomPoint (generateRandomPoint)
    it("Test del metodo generateRandomPoint", () => {
        const geoPoint = new GeoPoint(40.7128, -74.0060);
        const radiusGeoPoint = new GeoPoint(0.9, 0.9);
        
        // Genera un punto casuale all'interno di un raggio specificato
        const randomPoint = geoPoint.generateRandomPoint(radiusGeoPoint);

        // Verifica che la latitudine del punto casuale sia all'interno del range previsto
        expect(randomPoint.getLatitude()).toBeGreaterThanOrEqual(39.8128);
        expect(randomPoint.getLatitude()).toBeLessThanOrEqual(41.6128);

        // Verifica che la longitudine del punto casuale sia all'interno del range previsto
        expect(randomPoint.getLongitude()).toBeGreaterThanOrEqual(-74.9060);
        expect(randomPoint.getLongitude()).toBeLessThanOrEqual(-73.1060);
    });
});