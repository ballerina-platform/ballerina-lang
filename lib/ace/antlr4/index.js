define(function(require, exports, module) {
module.exports.atn = require('./atn/index');
module.exports.dfa = require('./dfa/index');
module.exports.tree = require('./tree/index');
module.exports.error = require('./error/index');
module.exports.Token = require('./Token').Token;
module.exports.CommonToken = require('./Token').CommonToken;
module.exports.InputStream = require('./InputStream').InputStream;
module.exports.FileStream = require('./FileStream').FileStream;
module.exports.CommonTokenStream = require('./CommonTokenStream').CommonTokenStream;
module.exports.Lexer = require('./Lexer').Lexer;
module.exports.Parser = require('./Parser').Parser;
var pc = require('./PredictionContext');
module.exports.PredictionContextCache = pc.PredictionContextCache;
module.exports.ParserRuleContext = require('./ParserRuleContext').ParserRuleContext;
module.exports.Interval = require('./IntervalSet').Interval;
    module.exports.Utils = require('./Utils');


});