// Definisce i tipi per l'iniezione delle dipendenze tramite Inversify
export const TYPES = {
    KafkaManager: Symbol.for('KafkaManager'),
    TrackerMap: Symbol.for('TrackerMap')
};
