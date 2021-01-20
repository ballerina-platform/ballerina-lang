/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.evaluation.parser;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.util.HashMap;
import java.util.Map;

/**
 * Debugger expression evaluation specific wrapper implementation of the ballerina tree parser.
 *
 * @since 2.0.0
 */
public class DebugParser {

    protected Map<String, SyntaxTree> parserCache;

    public DebugParser() {
        parserCache = new HashMap<>();
    }

    /**
     * Tries to parse a given ballerina source into a syntax tree.
     *
     * @param source Input source statement.
     * @return Parsed syntax tree.
     */
    public SyntaxTree getSyntaxTreeFor(String source) throws DebugParserException {
        if (!parserCache.containsKey(source)) {
            parseAndCache(source);
        }
        return parserCache.get(source);
    }

    /**
     * Pre-processes the user input and converts into a parsable unit.
     */
    protected String preprocess(String source) {
        return source;
    }

    private void parseAndCache(String source) throws DebugParserException {
        try {
            String parsableSource = preprocess(source);
            TextDocument document = TextDocuments.from(parsableSource);
            SyntaxTree syntaxTree = SyntaxTree.from(document);
            parserCache.put(source, syntaxTree);
        } catch (Exception e) {
            throw new DebugParserException("Failed to parse the user input due to: " + e.getMessage());
        }
    }
}
