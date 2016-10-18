/* ***** BEGIN LICENSE BLOCK *****
 * Distributed under the BSD license:
 *
 * Copyright (c) 2010, Ajax.org B.V.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Ajax.org B.V. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL AJAX.ORG B.V. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ***** END LICENSE BLOCK ***** */

define(function(require, exports, module) {
    "use strict";

    var oop = require("../lib/oop");
    var Mirror = require("../worker/mirror").Mirror;
    var antlr4 = require("../antlr4/index");

  // var antlr4 = require("../antlr4/index");
    var nel = require("../nel-gen/index");
   // var lint = require("./javascript/jshint").JSHINT;

    function startRegex(arr) {
        return RegExp("^(" + arr.join("|") + ")");
    }

    var disabledWarningsRe = startRegex([
        "Bad for in variable '(.+)'.",
        'Missing "use strict"'
    ]);
    var errorsRe = startRegex([
        "Unexpected",
        "Expected ",
        "Confusing (plus|minus)",
        "\\{a\\} unterminated regular expression",
        "Unclosed ",
        "Unmatched ",
        "Unbegun comment",
        "Bad invocation",
        "Missing space after",
        "Missing operator at"
    ]);
    var infoRe = startRegex([
        "Expected an assignment",
        "Bad escapement of EOL",
        "Unexpected comma",
        "Unexpected space",
        "Missing radix parameter.",
        "A leading decimal point can",
        "\\['{a}'\\] is better written in dot notation.",
        "'{a}' used out of scope"
    ]);

    var NelWorker = exports.NelWorker = function(sender) {
        Mirror.call(this, sender);
        this.setTimeout(500);
        this.setOptions();
    };

    oop.inherits(NelWorker, Mirror);

    (function() {
        this.setOptions = function(options) {
            this.options = options || {
                    // undef: true,
                    // unused: true,
                    esnext: true,
                    moz: true,
                    devel: true,
                    browser: true,
                    node: true,
                    laxcomma: true,
                    laxbreak: true,
                    lastsemic: true,
                    onevar: false,
                    passfail: false,
                    maxerr: 100,
                    expr: true,
                    multistr: true,
                    globalstrict: true
                };
            this.doc.getValue() && this.deferredUpdate.schedule(100);
        };

        this.changeOptions = function(newOptions) {
            oop.mixin(this.options, newOptions);
            this.doc.getValue() && this.deferredUpdate.schedule(100);
        };

        this.isValidJS = function(str) {
            try {
                // evaluated code can only create variables in this function
                eval("throw 0;" + str);
            } catch(e) {
                if (e === 0)
                    return true;
            }
            return false
        };

        this.onUpdate = function() {
            var value = this.doc.getValue();
            var errors = [];
            var results = validate(value);

            this.sender.emit("annotate", results);

        };
        var validate = function(input) {


            var parseTreeForAce = function(input) {
                var stream = new antlr4.InputStream(input);
                var lexer = new nel.WUMLLexer(stream);
                var tokens = new antlr4.CommonTokenStream(lexer);
                var parser = new nel.WUMLParser(tokens);
                var annotations = [];
                var listener = new AnnotatingErrorListener(annotations);
                parser.removeErrorListeners();
                //lexer.removeErrorListeners();
                //lexer.addErrorListener(listener);
                parser.addErrorListener(listener);
                parser.buildParseTrees = true;
                parser.sourceFile();
                return annotations;
            };

            var AnnotatingErrorListener = function(annotations) {
                antlr4.error.ErrorListener.call(this);
                this.annotations = annotations;
                return this;
            };

            AnnotatingErrorListener.prototype = Object.create(antlr4.error.ErrorListener.prototype);
            AnnotatingErrorListener.prototype.constructor = AnnotatingErrorListener;

            AnnotatingErrorListener.prototype.syntaxError = function(recognizer, offendingSymbol, line, column, msg, e) {
                this.annotations.push({
                    row: line - 1,
                    column: column,
                    text: msg,
                    type: "error"
                });
            };

            var finalResult = parseTreeForAce(input);
            return finalResult;

            //  return [ { row: 0, column: 0, text: "MyMode says Hello!", type: "error" } ];
        };

    }).call(NelWorker.prototype);

});
