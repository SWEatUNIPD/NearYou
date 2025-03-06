"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.RentSubject = void 0;
class RentSubject {
    register(simulatorObserver) {
        this.simulatorObserver = simulatorObserver;
    }
    notifyRentEnded(id) {
        this.simulatorObserver.updateRentEnded(id);
    }
}
exports.RentSubject = RentSubject;
//# sourceMappingURL=RentSubject.js.map