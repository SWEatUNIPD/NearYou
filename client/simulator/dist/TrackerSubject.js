"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.TrackerSubject = void 0;
class TrackerSubject {
    register(rentObserver) {
        this.rentObserver = rentObserver;
    }
    notifyTrackEnded() {
        this.rentObserver.updateTrackEnded();
    }
}
exports.TrackerSubject = TrackerSubject;
//# sourceMappingURL=TrackerSubject.js.map