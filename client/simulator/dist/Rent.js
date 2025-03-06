"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.Rent = void 0;
const RentSubject_1 = require("./RentSubject");
class Rent extends RentSubject_1.RentSubject {
    constructor(id, tracker) {
        super();
        this.id = id;
        this.tracker = tracker;
    }
    activate() {
        this.tracker.register(this);
        this.tracker.activate();
    }
    updateTrackEnded() {
        this.notifyRentEnded(this.id);
    }
    getId() {
        return this.id;
    }
}
exports.Rent = Rent;
//# sourceMappingURL=Rent.js.map