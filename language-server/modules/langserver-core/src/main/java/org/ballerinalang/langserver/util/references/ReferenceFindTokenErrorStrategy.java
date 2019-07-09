/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.util.references;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.eclipse.lsp4j.Position;

/**
 * This particular error strategy finds a token with respect to a given position (line and column).
 *
 * @since 0.993.0
 */
public class ReferenceFindTokenErrorStrategy extends DefaultErrorStrategy {
    private LSContext lsContext;
    private int line;
    private int col;
    private boolean terminateCheck = false;

    public ReferenceFindTokenErrorStrategy(LSContext context, Position position) {
        this.lsContext = context;
        this.line = position.getLine();
        this.col = position.getCharacter();
    }

    @Override
    public void reportMatch(Parser recognizer) {
        super.reportMatch(recognizer);

        if (!terminateCheck) {
            Token currentToken = recognizer.getCurrentToken();
            // -1 added since the ANTLR line position is not zero based
            int tokenLine = currentToken.getLine() - 1;
            int tokenStartCol = currentToken.getCharPositionInLine();
            int tokenStopCol = tokenStartCol + currentToken.getText().length();

            if (this.line == tokenLine && this.col >= tokenStartCol && this.col <= tokenStopCol) {
                this.lsContext.put(NodeContextKeys.NODE_NAME_KEY, currentToken.getText());
                this.terminateCheck = true;
            }
        }
    }
}
