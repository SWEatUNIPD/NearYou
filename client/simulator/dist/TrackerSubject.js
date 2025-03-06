"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.TrackerSubject = void 0;
class TrackerSubject {
    register(rentObserver) {
        this.rentObserver = rentObserver;
    }
    // abstract async activate(): Promise<void>;
    notify() {
        this.rentObserver.update();
    }
}
exports.TrackerSubject = TrackerSubject;
//# sourceMappingURL=TrackerSubject.js.map