"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.RentSubject = void 0;
class RentSubject {
    register(simulatorObserver) {
        this.simulatorObserver = simulatorObserver;
    }
    notify(id) {
        this.simulatorObserver.update(id);
    }
}
exports.RentSubject = RentSubject;
//# sourceMappingURL=RentSubject.js.map