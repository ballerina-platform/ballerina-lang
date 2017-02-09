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

    var oop = require("ace/lib/oop");
    var TextMode = require("ace/mode/text").Mode;
    var goLangHighlightRules = require("ace/mode/golang_highlight_rules").GolangHighlightRules;
    var DocCommentHighlightRules = require("ace/mode/doc_comment_highlight_rules").DocCommentHighlightRules;
    var MatchingBraceOutdent = require("ace/mode/matching_brace_outdent").MatchingBraceOutdent;
    var Range = require("ace/range").Range;
    var WorkerClient = require("ace/worker/worker_client").WorkerClient;
    var CstyleBehaviour = require("ace/mode/behaviour/cstyle").CstyleBehaviour;
    var CStyleFoldMode = require("ace/mode/folding/cstyle").FoldMode;

    var Mode = function() {
        this.HighlightRules = goLangHighlightRules;
        this.$outdent = new MatchingBraceOutdent();
        this.$behaviour = new CstyleBehaviour();
        this.foldingRules = new CStyleFoldMode();
    };
    oop.inherits(Mode, TextMode);
    var MyHighlightRules = function() {

// TODO: update with all keywords of NEL
        var keywordMapper = this.createKeywordMapper({
            "keyword.control": "if|then|else|do|break|case|catch|default|enum|final|finally|for|goto|return|switch|this|throw|throws|try|void|while|reply",
            "keyword.operator": "and|or|not",
            "keyword.other": "resource|message|endpoint|GET|PUT|POST|HEAD|Path|Source|Service|tags|description|produces|log|invoke|sendTo|reply|eval",
            "storage.type": "boolean|char|byte|short|int|long|float|double|constant|Exception|ConnectionClosedException|ConnectionFailedException|MediaType|APPLICATION_JSON|APPLICATION_XML",
            "storage.modifier": "private|public|protected",
            "support.function": "abstract|assert|implements|import|interface|instanceof|native|new|package|static|strictfp|super|synchronized|transient|volatile",
            "constant.language": "true|false"
        }, "identifier");
        this.$rules = {
            "start": [
                {
                    token : "comment",
                    regex : "\\/\\/.*$"
                },
                DocCommentHighlightRules.getStartRule("doc-start"),
                {
                    token : "comment.start", // multi line comment
                    regex : "\\/\\*",
                    next : "comment"
                }, {
                    token : "string", // single line
                    regex : /"(?:[^"\\]|\\.)*?"/
                },
                { token : "string",  regex : '["](?:(?:\\\\.)|(?:[^"\\\\]))*?["]' },
                { token : "constant.numeric", regex : "0[xX][0-9a-fA-F]+\\b" },
                { token : "constant.numeric", regex: "[+-]?\\d+(?:(?:\\.\\d*)?(?:[eE][+-]?\\d+)?)?\\b" },
                { token : "keyword.operator", regex : "!|%|\\\\|/|\\*|\\-|\\+|~=|==|<>|!=|<=|>=|=|<|>|&&|\\|\\|" },
                { token : "punctuation.operator", regex : "\\?|\\:|\\,|\\;|\\." },
                { token : "paren.lparen", regex : "[[({]" },
                { token : "paren.rparen", regex : "[\\])}]" },
                { token : "text", regex : "\\s+" },
                { token: keywordMapper, regex: "[a-zA-Z_$][a-zA-Z0-9_$]*\\b" }
            ],
            "comment" : [
                {
                    token : "comment.end",
                    regex : "\\*\\/",
                    next : "start"
                }, {
                    defaultToken : "comment"
                }
            ]
        };
        this.embedRules(DocCommentHighlightRules, "doc-",
            [ DocCommentHighlightRules.getEndRule("start") ]);
    };
    oop.inherits(MyHighlightRules, goLangHighlightRules);
    var Mode = function() {
        this.HighlightRules = MyHighlightRules;
        this.$outdent = new MatchingBraceOutdent();
        this.foldingRules = new CStyleFoldMode();
        this.$behaviour = new CstyleBehaviour();
    };
    oop.inherits(Mode, TextMode);

    (function() {

        this.lineCommentStart = "//";
        this.blockComment = {start: "/*", end: "*/"};

        this.getNextLineIndent = function(state, line, tab) {
            var indent = this.$getIndent(line);

            var tokenizedLine = this.getTokenizer().getLineTokens(line, state);
            var tokens = tokenizedLine.tokens;
            var endState = tokenizedLine.state;

            if (tokens.length && tokens[tokens.length-1].type == "comment") {
                return indent;
            }

            if (state == "start" || state == "no_regex") {
                var match = line.match(/^.*(?:\bcase\b.*:|[\{\(\[])\s*$/);
                if (match) {
                    indent += tab;
                }
            } else if (state == "doc-start") {
                if (endState == "start" || endState == "no_regex") {
                    return "";
                }
                var match = line.match(/^\s*(\/?)\*/);
                if (match) {
                    if (match[1]) {
                        indent += " ";
                    }
                    indent += "* ";
                }
            }

            return indent;
        };

        this.checkOutdent = function(state, line, input) {
            return this.$outdent.checkOutdent(line, input);
        };

        this.autoOutdent = function(state, doc, row) {
            return this.$outdent.autoOutdent(doc, row);
        };

        this.createWorker = function(session) {
            var worker = new WorkerClient(["ace"], "ace/mode/nel_worker", "NelWorker");
            worker.attachToDocument(session.getDocument());

            worker.on("annotate", function(results) {
                session.setAnnotations(results.data);
            });

            worker.on("terminate", function() {
                session.clearAnnotations();
            });

            return worker;
        };

        this.$id = "ace/mode/ballerina";
    }).call(Mode.prototype);

    exports.Mode = Mode;
});
