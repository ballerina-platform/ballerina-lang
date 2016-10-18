define(function(require, exports, module) {
    module.exports.RecognitionException = require('./Errors').RecognitionException;
    module.exports.NoViableAltException = require('./Errors').NoViableAltException;
    module.exports.LexerNoViableAltException = require('./Errors').LexerNoViableAltException;
    module.exports.InputMismatchException = require('./Errors').InputMismatchException;
    module.exports.FailedPredicateException = require('./Errors').FailedPredicateException;
    module.exports.DiagnosticErrorListener = require('./DiagnosticErrorListener').DiagnosticErrorListener;
    module.exports.BailErrorStrategy = require('./ErrorStrategy').BailErrorStrategy;
    module.exports.ErrorListener = require('./ErrorListener').ErrorListener;
});