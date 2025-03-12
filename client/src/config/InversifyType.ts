// Definisce i tipi per l'iniezione delle dipendenze tramite Inversify
const TYPES = {
    Tracker: Symbol.for('Tracker'),
    Rent: Symbol.for('Rent'),
    RentList: Symbol.for('RentList'),
    Simulator: Symbol.for('Simulator')
};

export { TYPES }
