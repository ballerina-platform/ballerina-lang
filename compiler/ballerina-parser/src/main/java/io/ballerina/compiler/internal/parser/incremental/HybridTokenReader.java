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
package io.ballerina.compiler.internal.parser.incremental;

import io.ballerina.compiler.internal.parser.AbstractTokenReader;
import io.ballerina.compiler.internal.parser.ParserMode;
import io.ballerina.compiler.internal.parser.tree.STToken;

/**
 * This class supplies {@code STToken}s on-demand from
 * the old syntax tree (from previous compilation) as well as from the
 * new (modified) source text.
 * <p>
 * In other words, this class mix together {@code STToken}s from the
 * old syntax tree and new source text.
 * <p>
 * If a particular token in the old syntax tree intersects with a text edit range,
 * then issue a token from the new source text, otherwise return the node
 * from the old tree.
 *
 * @since 1.3.0
 */
public class HybridTokenReader extends AbstractTokenReader {
    private final HybridNodeStorage hybridNodeStorage;

    public HybridTokenReader(HybridNodeStorage hybridNodeStorage) {
        this.hybridNodeStorage = hybridNodeStorage;
    }

    @Override
    public STToken read() {
        return this.hybridNodeStorage.consumeToken().token();
    }

    @Override
    public STToken peek() {
        return this.hybridNodeStorage.peekToken().token();
    }

    @Override
    public STToken peek(int k) {
        return this.hybridNodeStorage.peekToken(k - 1).token();
    }

    @Override
    public STToken head() {
        return this.hybridNodeStorage.getCurrentToken().token();
    }

    @Override
    public void startMode(ParserMode mode) {
    }

    @Override
    public void endMode() {
    }

    @Override
    public int getCurrentTokenIndex() {
        return this.hybridNodeStorage.getCurrentTokenIndex();
    }
}
