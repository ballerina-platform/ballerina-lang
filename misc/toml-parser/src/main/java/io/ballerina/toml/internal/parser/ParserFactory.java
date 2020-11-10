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
package io.ballerina.toml.internal.parser;

import io.ballerina.tools.text.CharReader;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;


/**
 * A factory for creating {@code TomlParser} instances.
 * <p>
 * Creates a regular parser or an incremental parser based on the parameters.
 *
 * @since 2.0.0
 */
public class ParserFactory {

    private ParserFactory() {
    }

    /**
     * Creates a regular {@code TomlParser} instance from the given {@code String}.
     *
     * @param text source code
     * @return a {@code TomlParser} instance
     */
    public static TomlParser getParser(String text) {
        TextDocument textDocument = TextDocuments.from(text);
        AbstractTokenReader tokenReader = new TokenReader(getLexer(textDocument));
        return new TomlParser(tokenReader);
    }

    /**
     * Creates a regular {@code TomlParser} instance from the given {@code TextDocument}.
     *
     * @param textDocument source code
     * @return a {@code TomlParser} instance
     */
    public static TomlParser getParser(TextDocument textDocument) {
        AbstractTokenReader tokenReader = new TokenReader(getLexer(textDocument));
        return new TomlParser(tokenReader);
    }

    private static TomlLexer getLexer(TextDocument textDocument) {
        return new TomlLexer(CharReader.from(textDocument));
    }
}
