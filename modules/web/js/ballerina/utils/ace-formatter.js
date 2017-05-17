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
import _ from 'lodash';
const ace = global.ace;

let TokenIterator = ace.acequire('ace/token_iterator').TokenIterator;

let newLinesRules = [
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
            lastNonWhiteSpaceToken: {type: 'paren.lparen', value: '{'}
        },
        avoidBreakAfterUpon: {
            lastNonWhiteSpaceToken: {type: 'paren.lparen', value: '{'}
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
        value: ';',
        breakBefore: false,
        breakAfter: true
    },
    {
        type: 'ballerina-keyword-definition',
        breakBefore: true,
        avoidBreakBeforeUpon: {
            contexts: ['annotationDef']
        },
        breakAfter: false
    }
];

let spaceRules = [
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
        forceSpaceBefore: true,
        append: true
    },
    {
        type: 'ballerina-keyword-other',
        prepend: false,
        forceSpaceBefore: true,
        append: true
    },
    {
        type: 'ballerina-keyword-primitive-type',
        prepend: false,
        forceSpaceBefore: true,
        append: true,
        skipAppendUponNext:{
            type: 'paren.lparen',
            value: '['
        }
    },
    {
        type: 'ballerina-keyword-non-primitive-type',
        prepend: false,
        forceSpaceBefore: true,
        append: true,
        skipAppendUponNext:{
            type: 'paren.lparen',
            value: '['
        }
    },
    {
        type: 'ballerina-keyword-definition',
        prepend: false,
        forceSpaceBefore: true,
        append: true
    },
    {
        type: 'ballerina-keyword-language',
        prepend: false,
        forceSpaceBefore: true,
        append: true
    },
    {
        type: 'punctuation.operator',
        value: ',',
        prepend: false,
        forceNoSpaceBefore: true,
        append: true
    },
    {
        type: 'ballerina-operator',
        prepend: true,
        append: true
    }
];

class BallerinaFormatter {
    static beautify(session, start, end) {
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
    }

    static format(iterator) {

        var token = iterator.getCurrentToken(),
            code = '',
            indentation = 0,
            lastToken = {},
            lastNonWhiteSpaceToken = {},
            nextToken = {},
            value = '',
            space = ' ',
            newLine = '\n',
            tab = '\t',
            annotationStack = [],
            inAnnotationDef = false;

        while (token !== null) {

            if (!token) {
                token = iterator.stepForward();
                continue;
            }

            nextToken = iterator.stepForward();

            //trim spaces
            if (token.type === 'whitespace' && _.isEmpty(annotationStack)) {
                token.value = token.value.trim();
            }

            //detect annotation attachment context
            if (token.type === 'ballerina-annotation') {
                annotationStack.push(token.value);
            }

            // add indent counts
            if (token.type === 'paren.lparen' && token.value === '{') {
                if(_.isEmpty(annotationStack)){
                    indentation++;
                }
            }
            else if (token.type === 'paren.rparen' && token.value === '}') {
                if(_.isEmpty(annotationStack)){
                    indentation--;
                }
                // exitting annotation def context
                if(inAnnotationDef) {
                    inAnnotationDef = false;
                }
            }

            //put spaces
            value = token.value;
            // skip annotations
            if(_.isEmpty(annotationStack)) {
                spaceRules.forEach(function(spaceRule){
                    if (token.type === spaceRule.type && (!spaceRule.value || token.value === spaceRule.value))
                    {
                        if (spaceRule.prepend && !_.endsWith(code, space)) {
                            value = space + token.value;
                        }
                        if (spaceRule.forceSpaceBefore && (!_.endsWith(code, space)
                                      && !_.endsWith(code, tab)
                                      && !_.endsWith(code, newLine)
                                      && !_.isEmpty(code))) {
                            code += space;
                        }
                        if (spaceRule.forceNoSpaceBefore && _.endsWith(code, space)) {
                            code = code.slice(0, -1);
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
                    && !_.endsWith(code, space) && !_.endsWith(code, tab)){
                    value = space + token.value;
                }

                // put new lines
                newLinesRules.forEach(function(newLineRule){
                    if (token.type === newLineRule.type && (!newLineRule.value || token.value === newLineRule.value)) {
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
                                if(_.has(avoidBreakBeforeUpon, 'contexts')) {
                                    avoidBreakBeforeUpon.contexts.forEach(context => {
                                        if (_.isEqual(context, 'annotationDef') && inAnnotationDef) {
                                            skipBreakBefore = true;
                                        }
                                    });
                                }
                            }
                            if(!skipBreakBefore){
                                code += newLine;
                                // indent
                                for (let i = 0; i < indentation; i++) {
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
                                    skipBreakAfter = true;
                                }
                            }
                            if(!skipBreakAfter){
                                value += newLine;
                                var indent = indentation;
                                // indent
                                if(token.type === 'paren.lparen' && token.value === '{' && nextToken.type === 'paren.rparen' && nextToken.value === '}') {
                                    // if block has no content don't indent so the closing '}' is not unnecessarily indented
                                    indent -= 1;
                                }
                                for (let i = 0; i < indent; i++) {
                                    value += tab;
                                }
                            }
                        }
                    }
                });
            } else {
                if(_.isEqual(annotationStack.length, 1) && _.isEqual(token.type, 'ballerina-annotation')) {
                    code += newLine;
                    // indent
                    for (let i = 0; i < indentation; i++) {
                        code += tab;
                    }
                }
            }

            //detect annotation definition context
            if (token.type === 'ballerina-keyword-definition' && token.value === 'annotation') {
                inAnnotationDef = true;
            }

            if (token.type === 'paren.rparen') {
                let rparens = token.value.split('');
                rparens.forEach((rparen) => {
                    if(rparen === '}' && !_.isEmpty(annotationStack)) {
                        annotationStack.pop();
                    }
                });
            }

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
    }
}

export default BallerinaFormatter;
