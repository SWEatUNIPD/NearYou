// Definisce i tipi per l'iniezione delle dipendenze tramite Inversify
const TYPES = {
    KafkaManager: Symbol.for('KafkaManager'),
    Tracker: Symbol.for('Tracker'),
    Rent: Symbol.for('Rent'),
    RentList: Symbol.for('RentList')
};

export { TYPES }
