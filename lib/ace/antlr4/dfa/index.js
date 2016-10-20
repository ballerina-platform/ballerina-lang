define(function(require, exports, module) {
    module.exports.DFA = require('./DFA').DFA;
    module.exports.DFASerializer = require('./DFASerializer').DFASerializer;
    module.exports.LexerDFASerializer = require('./DFASerializer').LexerDFASerializer;
    module.exports.PredPrediction = require('./DFAState').PredPrediction;
});
