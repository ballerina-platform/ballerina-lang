define(function(require, exports, module) {
   module.exports.ATN = require('./ATN').ATN;
    module.exports.ATNDeserializer = require('./ATNDeserializer').ATNDeserializer;
    module.exports.LexerATNSimulator = require('./LexerATNSimulator').LexerATNSimulator;
    module.exports.ParserATNSimulator = require('./ParserATNSimulator').ParserATNSimulator;
    module.exports.PredictionMode = require('./PredictionMode').PredictionMode;
});