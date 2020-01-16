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

import org.antlr.v4.runtime.Token;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.eclipse.lsp4j.Position;

import java.util.List;
import java.util.Optional;

import static org.ballerinalang.langserver.util.TokensUtil.searchTokenAtCursor;

/**
 * Parse a given compilation unit in order to capture the token.
 *
 * @since 0.993.0
 */
class ReferencesSubRuleParser {
    private ReferencesSubRuleParser() {
    }

    static void parseCompilationUnit(String content, LSContext context, Position pos) {
        // TODO: 1/23/19 Check what happens when the content is not a valid compilation unit and when there are errors
        List<Token> tokenList = CommonUtil.getTokenList(content);
        Optional<Token> tokenAtCursor = searchTokenAtCursor(tokenList, pos.getLine(), pos.getCharacter(), true);
        tokenAtCursor.ifPresent(token -> {
            context.put(NodeContextKeys.NODE_NAME_KEY, token.getText());
            int tokenIndex = token.getTokenIndex() - 1;
            int tokenType = -1;
            if (tokenIndex > 0) {
                tokenType = tokenList.get(tokenIndex).getType();
            }
            context.put(NodeContextKeys.INVOCATION_TOKEN_TYPE_KEY, tokenType);
        });
    }
}
