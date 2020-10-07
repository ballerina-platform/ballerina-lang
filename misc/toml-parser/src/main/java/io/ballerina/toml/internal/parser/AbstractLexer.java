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
package io.ballerina.toml.internal.parser;

import io.ballerina.toml.internal.diagnostics.DiagnosticCode;
import io.ballerina.toml.internal.parser.tree.STNode;
import io.ballerina.toml.internal.parser.tree.STNodeDiagnostic;
import io.ballerina.toml.internal.parser.tree.STToken;
import io.ballerina.tools.text.CharReader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An abstract lexer to be extended by all ballerina lexer implementations.
 *
 * @since 2.0.0
 */
public abstract class AbstractLexer {

    protected List<STNode> leadingTriviaList;
    private Collection<STNodeDiagnostic> diagnostics = new ArrayList<>();
    protected CharReader reader;

    public AbstractLexer(CharReader charReader) {
        this.reader = charReader;
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

    private void resetDiagnosticList() {
        this.diagnostics = new ArrayList<>();
    }

    private boolean noDiagnostics() {
        return diagnostics.isEmpty();
    }

    private Collection<STNodeDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    protected STToken cloneWithDiagnostics(STToken toClone) {
        if (noDiagnostics()) {
            return toClone;
        }

        STToken cloned = SyntaxErrors.addSyntaxDiagnostics(toClone, getDiagnostics());
        resetDiagnosticList();
        return cloned;
    }

    protected void reportLexerError(DiagnosticCode diagnosticCode, Object... args) {
        diagnostics.add(SyntaxErrors.createDiagnostic(diagnosticCode, args));
    }
}
