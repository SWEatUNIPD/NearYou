import { describe, it, expect, vi, beforeEach } from 'vitest';
import { TrackerSubject } from '../../src/TrackerSubject';
import { SimulatorObserver } from '../../src/SimulatorObserver';

// Implementazione concreta di TrackerSubject per i test
class TestTrackerSubject extends TrackerSubject {
  // Esponi il metodo protetto per i test
  public async testNotifyTrackEnded(id: string): Promise<void> {
    await this.notifyTrackEnded(id);
  }
}

describe('TrackerSubject', () => {
  let trackerSubject: TestTrackerSubject;
  let mockSimulatorObserver: SimulatorObserver;

  beforeEach(() => {
    // Reset dei mock
    vi.resetAllMocks();

    // Creazione del mock per SimulatorObserver
    mockSimulatorObserver = {
      trackEndedUpdate: vi.fn().mockResolvedValue(undefined)
    } as unknown as SimulatorObserver;

    // Creazione dell'istanza di TestTrackerSubject
    trackerSubject = new TestTrackerSubject();
  });

  it('dovrebbe registrare correttamente un osservatore', () => {
    trackerSubject.register(mockSimulatorObserver);
    
    // Verifica che l'osservatore sia stato registrato correttamente
    // chiamando un metodo che utilizza l'osservatore
    trackerSubject.testNotifyTrackEnded('test-id').catch(() => {});
    
    expect(mockSimulatorObserver.trackEndedUpdate).toHaveBeenCalledWith('test-id');
  });

  it('dovrebbe lanciare un errore se si tenta di notificare senza un osservatore registrato', async () => {
    // Non registrare alcun osservatore
    await expect(trackerSubject.testNotifyTrackEnded('test-id'))
      .rejects
      .toThrow('Track ended notify error: simulatorObserver not initialized');
  });

  it('dovrebbe notificare correttamente l\'osservatore quando una traccia termina', async () => {
    // Registrazione dell'osservatore
    trackerSubject.register(mockSimulatorObserver);
    
    // Notifica della fine della traccia
    await trackerSubject.testNotifyTrackEnded('test-id');
    
    // Verifica che l'osservatore sia stato notificato con l'ID corretto
    expect(mockSimulatorObserver.trackEndedUpdate).toHaveBeenCalledWith('test-id');
  });

  it('dovrebbe gestire errori durante la notifica all\'osservatore', async () => {
    // Mock per simulare un errore nella notifica
    mockSimulatorObserver.trackEndedUpdate = vi.fn().mockRejectedValue(new Error('Observer error'));
    
    // Mock di console.error
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    
    // Registrazione dell'osservatore
    trackerSubject.register(mockSimulatorObserver);
    
    // Notifica della fine della traccia
    await trackerSubject.testNotifyTrackEnded('test-id');
    
    // Verifica che l'errore sia stato gestito e loggato
    expect(consoleSpy).toHaveBeenCalled();
    expect(consoleSpy.mock.calls[0][0]).toContain('Error caught trying to update the tracker map');
  });
});