/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
define('ace/mode/ballerina',
    ["require", "exports", "module", "ace/lib/oop", "ace/mode/text", "ace/mode/text_highlight_rules", "ace/worker/worker_client"],
    function (require, exports, module) {

        var oop = require("ace/lib/oop");
        var TextMode = require("ace/mode/text").Mode;
        var TextHighlightRules = require("ace/mode/text_highlight_rules").TextHighlightRules;

        var MyHighlightRules = function () {

            var keywordMapper = this.createKeywordMapper({
                "ballerina-keyword-control": "if|else|iterator|try|catch|fork|join|while|new|reply|throw|throws|return|break|timeout",
                "ballerina-keyword-other": "import|package|version|public",
                "ballerina-keyword-primitive-type": "boolean|int|long|float|double|string",
                "ballerina-keyword-non-primitive-type": "message|map|exception|json|xml|xmlDocument",
                "ballerina-keyword-definition": "type|typeconvertor|connector|function|resource|service|worker|struct",
                "ballerina-keyword-language": "const|true|false"
            }, "ballerina-keyword-identifier");

            this.$rules = {
                "start": [
                    {token: "comment", regex: "//"},
                    {token: "ballerina-strings", regex: '["](?:(?:\\\\.)|(?:[^"\\\\]))*?["]'},
                    {token: "constant.numeric", regex: "0[xX][0-9a-fA-F]+\\b"},
                    {token: "constant.numeric", regex: "[+-]?\\d+(?:(?:\\.\\d*)?(?:[eE][+-]?\\d+)?)?\\b"},
                    {token: "keyword.operator", regex: "!|%|\\\\|/|\\*|\\-|\\+|~=|==|<>|!=|<=|>=|=|<|>|&&|\\|\\|"},
                    {token: "punctuation.operator", regex: "\\?|\\:|\\,|\\;|\\."},
                    {token: "paren.lparen", regex: "[[({]"},
                    {token: "paren.rparen", regex: "[\\])}]"},
                    {token: "text", regex: "\\s+"},
                    {token: "ballerina-annotation", regex: "@[a-zA-Z_$][a-zA-Z0-9_$]*"},
                    {token: "ballerina-package-reference", regex: "[a-zA-Z_$][a-zA-Z0-9_$]*:"},
                    {token: keywordMapper, regex: "[a-zA-Z_$][a-zA-Z0-9_$]*\\b"}
                ]
            };
        };
        oop.inherits(MyHighlightRules, TextHighlightRules);

        var MyMode = function () {
            this.HighlightRules = MyHighlightRules;
        };

        oop.inherits(MyMode, TextMode);

        (function () {

            this.$id = "ace/mode/ballerina";

        }).call(MyMode.prototype);

        exports.Mode = MyMode;
});