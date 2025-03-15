// Interfaccia che rappresenta un osservatore del simulatore
export interface SimulatorObserver {
    trackEndedUpdate(id: string): Promise<void>;
}
