/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define([ './gen/antlr4/index', './gen/NELLexer', './gen/NELParser', './impl/NELListenerImpl'],
    function (antlr4, NELLexer, NELParser, NELListener) {
        return  {
            generateTreeObject: function generateTreeObject(source) {
                var chars = new antlr4.InputStream(source);
                var lexer = new NELLexer.NELLexer(chars);
                var tokens = new antlr4.CommonTokenStream(lexer);
                var parser = new NELParser.NELParser(tokens);
                var listener = new NELListenerImpl();
                parser.buildParseTrees = true;
                var tree = parser.sourceFile();
                var x = antlr4.tree.ParseTreeWalker.DEFAULT.walk(listener, tree);
            }
        }
    });

