/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.parser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaLexer;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParserErrorListener;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Compiler Context Aware Token Cache.
 *
 * @since 1.1
 */
class TokenCache {

    private static final CompilerContext.Key<TokenCache> TOKEN_CACHE_KEY = new CompilerContext.Key<>();

    private CompilerContext context;

    private Map<PackageID, Map<String, Tokens>> pkgCache = new HashMap<>();

    private TokenCache(CompilerContext context) {

        this.context = context;
        this.context.put(TOKEN_CACHE_KEY, this);
    }

    static TokenCache getInstance(CompilerContext context) {

        TokenCache cache = context.get(TOKEN_CACHE_KEY);
        if (cache == null) {
            cache = new TokenCache(context);
        }

        return cache;
    }

    CommonTokenStream getTokenStream(CompilerInput sourceEntry, PackageID packageID,
                                     BDiagnosticSource diagnosticSrc) throws IOException {

        Map<String, Tokens> cache;
        if ((cache = pkgCache.get(packageID)) == null || !pkgCache.containsKey(packageID)) {
            cache = new HashMap<>();
            pkgCache.put(packageID, cache);
        }

        String entryName = sourceEntry.getEntryName();
        Tokens tokens = cache.get(entryName);
        if (tokens != null && tokens.hash == getHash(sourceEntry)) {
            return new CommonTokenStream(new ListTokenSource(tokens.tokenList));
        }
        CommonTokenStream tokenStream = createFilledTokenStream(sourceEntry, diagnosticSrc);
        tokens = new Tokens(getHash(sourceEntry), tokenStream.getTokens());
        cache.put(entryName, tokens);
        return tokenStream;
    }

    private CommonTokenStream createFilledTokenStream(CompilerInput sourceEntry, BDiagnosticSource diagnosticSrc)
            throws IOException {

        ANTLRInputStream ais = new ANTLRInputStream(
                new InputStreamReader(new ByteArrayInputStream(sourceEntry.getCode()), StandardCharsets.UTF_8));
        ais.name = sourceEntry.getEntryName();
        BallerinaLexer lexer = new BallerinaLexer(ais);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new BallerinaParserErrorListener(context, diagnosticSrc));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        tokenStream.fill();
        return tokenStream;
    }

    private static int getHash(CompilerInput sourceEntry) {

        byte[] code = sourceEntry.getCode();
        return Arrays.hashCode(code);
    }

    /**
     * Data holder class for Token Cache.
     *
     * @since 1.1
     */
    static class Tokens {

        int hash;
        List<Token> tokenList;

        Tokens(int hash, List<Token> tokenList) {

            this.hash = hash;
            this.tokenList = tokenList;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Tokens tokens = (Tokens) o;
            return hash == tokens.hash;
        }

        @Override
        public int hashCode() {

            return hash;
        }
    }

    /**
     * Represents {@link TokenSource} Created from a Pre-Build token list.
     *
     * @since 1.1
     */
    static class ListTokenSource implements TokenSource {

        private final Iterator<Token> iterator;

        ListTokenSource(List<Token> tokenList) {

            iterator = tokenList.iterator();
        }

        @Override
        public Token nextToken() {

            if (iterator.hasNext()) {
                return iterator.next();
            }
            return null;
        }

        @Override
        public int getLine() {

            return 0;
        }

        @Override
        public int getCharPositionInLine() {

            return 0;
        }

        @Override
        public CharStream getInputStream() {

            return null;
        }

        @Override
        public String getSourceName() {

            return null;
        }

        @Override
        public void setTokenFactory(TokenFactory<?> tokenFactory) {

        }

        @Override
        public TokenFactory<?> getTokenFactory() {

            return null;
        }
    }
}
