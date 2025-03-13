// Definisce i tipi per l'iniezione delle dipendenze tramite Inversify
export const TYPES = {
    KafkaManager: Symbol.for('KafkaManager'),
    Tracker: Symbol.for('Tracker'),
    Rent: Symbol.for('Rent'),
    RentList: Symbol.for('RentList')
};
