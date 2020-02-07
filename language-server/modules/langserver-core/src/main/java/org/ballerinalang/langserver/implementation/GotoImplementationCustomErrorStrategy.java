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
package org.ballerinalang.langserver.implementation;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.eclipse.lsp4j.Position;

/**
 * Custom Error Strategy for Goto Implementation.
 *
 * @since 0.990.3
 */
public class GotoImplementationCustomErrorStrategy extends LSCustomErrorStrategy {
    private LSContext lsContext;
    private int line;
    private int col;
    private boolean terminateCheck = false;
    private String relativeSourceFilePath;

    public GotoImplementationCustomErrorStrategy(LSContext context) {
        super(context);
        this.lsContext = context;
        Position position = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        this.line = position.getLine();
        this.col = position.getCharacter();
        this.relativeSourceFilePath = this.lsContext.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY).replace("\\", "/");
    }

    @Override
    public void reportMatch(Parser recognizer) {
        super.reportMatch(recognizer);

        if (recognizer.getSourceName().equals(relativeSourceFilePath) && !terminateCheck) {
            Token currentToken = recognizer.getCurrentToken();
            // -1 added since the ANTLR line position is not zero based
            int tokenLine = currentToken.getLine() - 1;
            int tokenStartCol = currentToken.getCharPositionInLine();
            int tokenStopCol = tokenStartCol + currentToken.getText().length();

            if (this.line == tokenLine && this.col >= tokenStartCol && this.col <= tokenStopCol) {
                this.lsContext.put(GotoImplementationKeys.SYMBOL_TOKEN_KEY, currentToken.getText());
                this.terminateCheck = true;
            }
        }
    }
}
