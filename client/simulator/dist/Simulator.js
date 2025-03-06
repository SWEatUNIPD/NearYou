"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.Simulator = void 0;
class Simulator {
    constructor(rentList) {
        this.rentList = rentList;
    }
    startSimulation() {
        this.rentList.forEach(rent => {
            rent.register(this);
            rent.activate();
        });
    }
    updateRentEnded(id) {
        const endedRentIndex = this.rentList.findIndex((trk) => trk.getId() === id);
        if (endedRentIndex !== -1) {
            this.rentList.splice(endedRentIndex, 1);
        }
    }
}
exports.Simulator = Simulator;
//# sourceMappingURL=Simulator.js.map