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
import antlr4 from 'antlr4';
import BallerinaLexer from './antlr-gen/BallerinaLexer';
import BallerinaParser from './antlr-gen/BallerinaParser';
import BLangParserErrorListener from './error-listener'
import BLangParserListener from './listener';

class Parser {

    /**
     * Creates AST for the given ballerina source
     * @param input {string} ballerina source content
     */
    parse(input){
        // setup parser
        var chars = new antlr4.InputStream(input);
        var lexer = new BallerinaLexer.BallerinaLexer(chars);
        var tokens  = new antlr4.CommonTokenStream(lexer);
        var parser = new BallerinaParser.BallerinaParser(tokens);
        var listener = new BLangParserListener(parser);

        // set custom error listener for collecting syntax errors
        var errorListener = new BLangParserErrorListener();
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        // start parsing
        var tree = parser.compilationUnit();

        antlr4.tree.ParseTreeWalker.DEFAULT.walk(listener, tree);

        return {
            errors: errorListener.getErrors(),
            ast: listener.getASTRoot()
        };
    };
}

export default Parser;