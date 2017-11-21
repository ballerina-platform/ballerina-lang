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
ace.define('ace/mode/ballerina',
    ['require', 'exports', 'module'], (acequire, exports, module) => {
        require('ace/mode-javascript');
        require('./ace-styles.scss');

        acequire('ace/config').set('workerPath', 'dist');

        const oop = acequire('ace/lib/oop');
        const JavaScriptMode = acequire('ace/mode/javascript').Mode;
        const TextHighlightRules = acequire('ace/mode/text_highlight_rules').TextHighlightRules;
        const WorkerClient = acequire('ace/worker/worker_client').UIWorkerClient;

        const BallerinaHighlightRules = function () {
            const keywordMapper = this.createKeywordMapper({
                'ballerina-keyword-control': 'if|else|iterator|try|catch|finally|fork|join|while|throw|throws|' +
                'return|break|timeout|transaction|aborted|abort|committed|failed|retry|next|bind|with|lengthof|typeof',
                'ballerina-keyword-other': 'import|version|public|attach|as',
                'ballerina-keyword-primitive-type': 'boolean|int|float|string|var|any',
                'ballerina-keyword-non-primitive-type': 'message|map|exception|json|xml|xmlDocument|xmlns|error',
                'ballerina-keyword-definition': 'annotation|package|type|connector|function|resource|' +
                'service|action|worker|struct|transformer|endpoint',
                'ballerina-keyword-language': 'const|true|false|reply|create|parameter',
            }, 'ballerina-identifier');

            this.$rules = {
                start: [
                    { token: 'comment', regex: /^\s*(\/\/).*$/ },
                    { token: 'ballerina-xml-json', regex: '[`](?:(?:\\\\.)|(?:[^`\\\\]))*?[`]' },
                    { token: 'ballerina-strings', regex: '["](?:(?:\\\\.)|(?:[^"\\\\]))*?["]' },
                    { token: 'ballerina-numeric', regex: '0[xX][0-9a-fA-F]+\\b' },
                    { token: 'ballerina-numeric', regex: '[+-]?\\d+(?:(?:\\.\\d*)?(?:[eE][+-]?\\d+)?)?\\b' },
                    { token: 'ballerina-operator', regex: '!|%|\\\\|/|\\*|\\-|\\+|~=|==|=|<>|!=|<=|>=|<|>|&&|\\|\\|' },
                    { token: 'punctuation.operator', regex: '\\?|\\:|\\,|\\;|\\.' },
                    { token: 'paren.lparen', regex: '[[({]' },
                    { token: 'paren.rparen', regex: '[\\])}]' },
                    { token: 'whitespace', regex: '(?:\\s+)' },
                    { token: 'ballerina-annotation', regex: '@[a-zA-Z_$][a-zA-Z0-9_$]*' },
                    { token: 'ballerina-package-reference', regex: '[a-zA-Z_$][a-zA-Z0-9_$]*:' },
                    { token: 'ballerina-import-package-name-part', regex: '(?:(?:\\w+\\.)+\\w+\\s*;)' },
                    { token: keywordMapper, regex: '[a-zA-Z_$][a-zA-Z0-9_$]*\\b' },
                ],
            };
        };
        oop.inherits(BallerinaHighlightRules, TextHighlightRules);

        const BallerinaMode = function () {
            JavaScriptMode.call(this);
            this.HighlightRules = BallerinaHighlightRules;

            this.createWorker = function (session) {
                const worker = new WorkerClient(['ace/aceb'], 'ace/worker/ballerina', 'WorkerModule');
                worker.attachToDocument(session.getDocument());

                worker.on('lint', (results) => {
                    if (!_.isNil(results.data) && _.isArray(results.data)) {
                        results.data.forEach((syntaxError) => {
                            // ace's rows start from zero, but parser begins from 1
                            syntaxError.row -= 1;
                        });
                        session.setAnnotations(results.data);
                    } else {
                        // no new errors or something wrong with validator. clear up current errors
                        session.clearAnnotations();
                    }
                });

                worker.on('terminate', () => {
                    session.clearAnnotations();
                });

                return worker;
            };
        };

        // inherit from javascript mode
        oop.inherits(BallerinaMode, JavaScriptMode);

        (function () {
            this.createWorker = function (session) {
                return null;
            };
            this.$id = 'ace/mode/ballerina';
        }).call(BallerinaMode.prototype);

        exports.Mode = BallerinaMode;
    });
