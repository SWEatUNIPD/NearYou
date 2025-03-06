"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var _a;
Object.defineProperty(exports, "__esModule", { value: true });
exports.connectProducer = connectProducer;
exports.disconnectProducer = disconnectProducer;
exports.sendMessage = sendMessage;
const kafkajs_1 = require("kafkajs");
const kafka = new kafkajs_1.Kafka({
    clientId: 'producer1',
    brokers: [(_a = process.env.BROKER) !== null && _a !== void 0 ? _a : 'localhost:9094'],
});
const producer = kafka.producer({
    createPartitioner: kafkajs_1.Partitioners.DefaultPartitioner,
});
function connectProducer() {
    return __awaiter(this, void 0, void 0, function* () {
        yield producer.connect();
    });
}
function disconnectProducer() {
    return __awaiter(this, void 0, void 0, function* () {
        yield producer.disconnect();
    });
}
function sendMessage(topic, message) {
    return __awaiter(this, void 0, void 0, function* () {
        producer.send({
            topic: topic,
            messages: [
                {
                    value: message
                },
            ],
        });
    });
}
//# sourceMappingURL=Producer.js.map