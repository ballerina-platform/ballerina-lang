/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerinalang.compiler.internal.parser;

import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STToken;

import java.util.ArrayDeque;
import java.util.List;

/**
 * An abstract lexer to be extended by all ballerina lexer implementations.
 * 
 * @since 2.0.0
 */
public abstract class AbstractLexer {

    protected List<STNode> leadingTriviaList;
    protected CharReader reader;
    protected ParserMode mode;
    protected ArrayDeque<ParserMode> modeStack = new ArrayDeque<>();
    protected final BallerinaParserErrorListener errorListener = new BallerinaParserErrorListener();

    public AbstractLexer(CharReader charReader, ParserMode initialParserMode) {
        this.reader = charReader;
        startMode(initialParserMode);
    }

    /**
     * Get the next lexical token.
     * 
     * @return Next lexical token.
     */
    public abstract STToken nextToken();

    /**
     * Reset the lexer to a given offset. The lexer will read the next tokens
     * from the given offset.
     * 
     * @param offset Offset
     */
    public void reset(int offset) {
        reader.reset(offset);
    }

    /**
     * Start the given operation mode of the lexer.
     * 
     * @param mode Mode to switch on to
     */
    public void startMode(ParserMode mode) {
        this.mode = mode;
        this.modeStack.push(mode);
    }

    /**
     * End the current mode the mode of the lexer and fall back the previous mode.
     */
    public void endMode() {
        this.modeStack.pop();
        this.mode = this.modeStack.peek();
    }
}
