"use strict";
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (Object.hasOwnProperty.call(mod, k)) result[k] = mod[k];
    result["default"] = mod;
    return result;
};
Object.defineProperty(exports, "__esModule", { value: true });
var codepoints = __importStar(require("./codepoints.json"));
var cps = codepoints;
function getCodePoint(name) {
    return cps[name];
}
exports.getCodePoint = getCodePoint;
function getAllCodePoints() {
    return cps;
}
exports.getAllCodePoints = getAllCodePoints;
//# sourceMappingURL=utils.js.map