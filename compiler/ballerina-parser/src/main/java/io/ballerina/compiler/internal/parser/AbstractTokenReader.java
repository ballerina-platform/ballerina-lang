/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.internal.parser;

import io.ballerina.compiler.internal.parser.tree.STToken;

/**
 * Abstract representation of an entity that supplies {@code STNode}s and {@code STToken}s on-demand.
 *
 * @since 1.3.0
 */
// TODO can we make this an interface?
public abstract class AbstractTokenReader {

    /**
     * Consumes the input and return the next token.
     *
     * @return Next token in the input
     */
    public abstract STToken read();

    /**
     * Lookahead in the input and returns the next token. This will not consume the input.
     * That means calling this method multiple times will return the same result.
     *
     * @return Next token in the input
     */
    public abstract STToken peek();

    /**
     * Lookahead in the input and returns the token at the k-th position from the current
     * position of the input token stream. This will not consume the input. That means
     * calling this method multiple times will return the same result.
     *
     * @param k Position of the character to peek
     * @return Token at the k-position from the current position
     */
    public abstract STToken peek(int k);

    /**
     * Returns the current token. i.e: last consumed token.
     *
     * @return The current token.
     */
    public abstract STToken head();

    /**
     * Start the given mode in the token reader.
     * 
     * @param mode Mode to start
     */
    public abstract void startMode(ParserMode mode);

    /**
     * Switch the mode of the token reader to the given mode.
     *
     * @param mode Mode to switch on to
     */
    public abstract void switchMode(ParserMode mode);

    /**
     * End the current mode of the token reader.
     */
    public abstract void endMode();

    /**
     * Start the given keyword operation mode in the token reader.
     *
     * @param keywordMode mode to add
     */
    public abstract void startKeywordMode(KeywordMode keywordMode);

    /**
     * End the current keyword mode in the token reader.
     *
     */
    public abstract void endKeywordMode();

    /**
     * Get the current mode of the token reader.
     *
     * @return Current mode
     */
    public abstract ParserMode getCurrentMode();

    /**
     * Get the current index of the token reader.
     *
     * @return current token index
     */
    public abstract int getCurrentTokenIndex();
}
