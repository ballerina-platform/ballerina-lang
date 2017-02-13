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
                breakBefore: false,
                breakAfter: true,
                avoidBreakAfterUpon: {
                    lastNonWhiteSpaceToken: {type: 'ballerina-operator', value: '='},
                }
            },
            {
                type: 'paren.rparen',
                breakBefore: true,
                breakAfter: true,
                value: '}',
                avoidBreakBeforeUpon: {
                    lastNonWhiteSpaceToken: {type: 'paren.lparen', value: "{"}
                },
                avoidBreakAfterUpon: {
                    lastNonWhiteSpaceToken: {type: 'paren.lparen', value: "{"}
                }
            },
            {
                type: 'paren.rparen',
                breakBefore: true,
                value: '})',
                breakAfter: true
            },
            {
                type: 'ballerina-import-package-name-part',
                breakBefore: false,
                breakAfter: true
            },
            {
                type: 'ballerina-annotation',
                breakBefore: true,
                breakAfter: false
            },
            {
                type: 'punctuation.operator',
                value: ";",
                breakBefore: false,
                breakAfter: true
            },
            {
                type: 'ballerina-keyword-definition',
                breakBefore: true,
                breakAfter: false
            }
        ];

        BallerinaFormatter.spaceRules = [
            {
                type: 'paren.lparen',
                value: '{',
                prepend: true,
                append: false
            },
            {
                type: 'ballerina-operator',
                value: '=',
                prepend: true,
                append: true
            },
            {
                type: 'ballerina-keyword-control',
                prepend: false,
                append: true
            },
            {
                type: 'ballerina-keyword-other',
                prepend: false,
                append: true
            },
            {
                type: 'ballerina-keyword-primitive-type',
                prepend: false,
                append: true,
                skipAppendUponNext:{
                    type: 'paren.lparen',
                    value: '['
                }
            },
            {
                type: 'ballerina-keyword-non-primitive-type',
                prepend: false,
                append: true,
                skipAppendUponNext:{
                    type: 'paren.lparen',
                    value: '['
                }
            },
            {
                type: 'ballerina-keyword-definition',
                prepend: false,
                append: true
            },
            {
                type: 'ballerina-keyword-language',
                prepend: false,
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

        BallerinaFormatter.beautify = function (session, start, end) {
            if(_.isNil(start)){
                start = 0;
            }

            if(_.isNil(end)){
                end = 0;
            }

            var iterator = new TokenIterator(session, start, end);
            this.session = session;
            var code = this.format(iterator);
            session.setValue(code);
        };

        BallerinaFormatter.format = function (iterator) {

            var token = iterator.getCurrentToken(),
                 newLinesRules = this.newLinesRules,
                 spaceRules = this.spaceRules,
                 code = '',
                 indentation = 0,
                 lastToken = {},
                 lastNonWhiteSpaceToken = {},
                 nextToken = {},
                 value = '',
                 space = ' ',
                 newLine = '\n',
                 tab = '\t';

            while (token !== null) {

                if (!token) {
                    token = iterator.stepForward();
                    continue;
                }

                nextToken = iterator.stepForward();

                //trim spaces
                if (token.type == 'whitespace') {
                    token.value = token.value.trim();
                }

                // add indent counts
                if (token.type == 'paren.lparen' && token.value === "{") {
                    indentation++;
                }
                else if (token.type == 'paren.rparen' && token.value === "}") {
                    indentation--;
                }

                //put spaces
                value = token.value;
                spaceRules.forEach(function(spaceRule){
                    if (token.type == spaceRule.type && (!spaceRule.value || token.value == spaceRule.value))
                    {
                        if (spaceRule.prepend && !_.endsWith(code, space)) {
                            value = space + token.value;
                        }
                        if (spaceRule.append) {
                            if(_.has(spaceRule, 'skipAppendUponNext')){
                               var  skipAppendUponNext = _.get(spaceRule, 'skipAppendUponNext');
                               if(_.isEqual(skipAppendUponNext.type, nextToken.type)
                                    && _.isEqual(skipAppendUponNext.value, nextToken.value)){
                                   // do nothing for now
                               } else {
                                   value += space;
                               }
                            } else {
                                value += space;
                            }
                        }
                    }
                });

                if(_.isEqual(token.type, 'ballerina-identifier')
                    && _.isEqual(lastToken.type, 'whitespace')
                    && !_.endsWith(code, space)){
                    value = space + token.value;
                }

                // put new lines
                newLinesRules.forEach(function(newLineRule){
                    if (token.type == newLineRule.type && (!newLineRule.value || token.value == newLineRule.value)) {
                        if (newLineRule.breakBefore && !_.endsWith(code, newLine)) {
                            var skipBreakBefore = false;
                            if(_.has(newLineRule, 'avoidBreakBeforeUpon')){
                                var avoidBreakBeforeUpon = _.get(newLineRule, 'avoidBreakBeforeUpon');
                                if(_.isEqual(_.get(avoidBreakBeforeUpon, 'lastToken.type'), _.get(lastToken, 'type'))
                                    && _.isEqual(_.get(avoidBreakBeforeUpon, 'lastToken.value'), _.get(lastToken, 'value'))){
                                    skipBreakBefore = true;
                                }
                                if(_.isEqual(_.get(avoidBreakBeforeUpon, 'lastNonWhiteSpaceToken.type'), _.get(lastNonWhiteSpaceToken, 'type'))
                                    && _.isEqual(_.get(avoidBreakBeforeUpon, 'lastNonWhiteSpaceToken.value'), _.get(lastNonWhiteSpaceToken, 'value'))){
                                    skipBreakBefore = true;
                                }
                            }
                            if(!skipBreakBefore){
                                code += newLine;
                                // indent
                                for (var i = 0; i < indentation; i++) {
                                    code += tab;
                                }
                            }
                        }
                        if (newLineRule.breakAfter) {
                            var skipBreakAfter = false;
                            if(_.has(newLineRule, 'avoidBreakAfterUpon')){
                                var avoidBreakAfterUpon = _.get(newLineRule, 'avoidBreakAfterUpon');
                                if(_.isEqual(_.get(avoidBreakAfterUpon, 'lastToken.type'), _.get(lastToken, 'type'))
                                    && _.isEqual(_.get(avoidBreakAfterUpon, 'lastToken.value'), _.get(lastToken, 'value'))){
                                    skipBreakAfter = true;
                                }
                                if(_.isEqual(_.get(avoidBreakAfterUpon, 'lastNonWhiteSpaceToken.type'), _.get(lastNonWhiteSpaceToken, 'type'))
                                    && _.isEqual(_.get(avoidBreakAfterUpon, 'lastNonWhiteSpaceToken.value'), _.get(lastNonWhiteSpaceToken, 'value'))){
                                    // do nothing
                                    skipBreakAfter = true
                                }
                            }
                            if(!skipBreakAfter){
                                value += newLine;
                                // indent
                                for (var i = 0; i < indentation; i++) {
                                    value += tab;
                                }
                            }
                        }
                    }
                });

                code += value;

                lastToken = token;

                if(!_.isEqual(token.type, 'whitespace')){
                    lastNonWhiteSpaceToken = token;
                }

                token = nextToken;

                if (token === null) {
                    break;
                }
            }

            return code;
        };

        return BallerinaFormatter;
    });