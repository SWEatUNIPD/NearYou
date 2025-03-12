// Interfaccia che rappresenta un osservatore del simulatore
export interface SimulatorObserver {
    updateRentEnded(id: string): void;
}
