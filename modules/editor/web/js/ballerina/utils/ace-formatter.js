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
define(["require", "ace/token_iterator"],
    function (require) {

        var TokenIterator = require("ace/token_iterator").TokenIterator;

        var BallerinaFormatter = {};

        BallerinaFormatter.newLinesRules = [
            {
                type: 'paren.lparen',
                value: '{',
                indent: true
            },
            {
                type: 'paren.rparen',
                breakBefore: true,
                value: '}'
            },
            {
                type: 'paren.rparen',
                breakBefore: true,
                value: '})',
                dontBreak: true
            }
        ];

        BallerinaFormatter.spaceRules = [
            {
                type: 'ballerina-operator',
                value: '=',
                prepend: true,
                append: true
            },
            {
                type: 'ballerina-keyword-control',
                prepend: true,
                append: true
            },
            {
                type: 'ballerina-keyword-other',
                prepend: true,
                append: true
            },
            {
                type: 'ballerina-keyword-primitive-type',
                prepend: true,
                append: true
            },
            {
                type: 'ballerina-keyword-non-primitive-type',
                prepend: true,
                append: true
            },
            {
                type: 'ballerina-keyword-definition',
                prepend: true,
                append: true
            },
            {
                type: 'ballerina-keyword-language',
                prepend: true,
                append: true
            },
            {
                type: 'punctuation.operator',
                value: ",",
                prepend: false,
                append: true
            },
            {
                type: 'ballerina-operator',
                prepend: true,
                append: true
            }
        ];

        BallerinaFormatter.beautify = function (session) {
            var iterator = new TokenIterator(session, 0, 0);
            this.session = session;
            var token = iterator.getCurrentToken();
            var code = this.format(iterator);
            session.doc.setValue(code);
        };

        BallerinaFormatter.format = function (iterator, context) {

            var token = iterator.getCurrentToken(),
                 newLinesRules = this.newLinesRules,
                 spaceRules = this.spaceRules,
                 code = '',
                 indentation = 0,
                 tag,
                 lastTag,
                 lastToken = {},
                 nextToken = {},
                 value = '',
                 skipNextSpace = false;

            while (token !== null) {
                console.log(token);

                if (!token) {
                    token = iterator.stepForward();
                    continue;
                }

                nextToken = iterator.stepForward();

                //trim spaces
                if (token.type == 'whitespace') {
                    token.value = token.value.trim();
                }

                //skip empty tokens
                if (!token.value) {
                    token = nextToken;
                    continue;
                }

                //put spaces back in
                value = token.value;
                for (var i in spaceRules) {
                    if (token.type == spaceRules[i].type && (!spaceRules[i].value || token.value == spaceRules[i].value))
                    {
                        if (!skipNextSpace && spaceRules[i].prepend) {
                            value = ' ' + token.value;
                        } else {
                            skipNextSpace = false;
                        }

                        if (spaceRules[i].append) {
                            value += ' ';
                            skipNextSpace = true;
                        }
                    }
                }

                for (i in newLinesRules) {
                    if (token.type == newLinesRules[i].type && (!newLinesRules[i].value || token.value == newLinesRules[i].value)) {
                        if (newLinesRules[i].indent === false) {
                            indentation--;
                        } else if(newLinesRules[i].indent === true){
                            indentation++;
                        }

                        if (newLinesRules[i].breakBefore && (!newLinesRules[i].prev || newLinesRules[i].prev.test(lastToken.value))) {
                            code += "\n";

                            //indent
                            for (i = 0; i < indentation; i++) {
                                code += "\t";
                            }
                        }
                        break;
                    }
                }

                code += value;

                //next token
                lastTag = tag;

                lastToken = token;

                token = nextToken;

                if (token === null) {
                    break;
                }
            }

            return code;
        };

        return BallerinaFormatter;
    });