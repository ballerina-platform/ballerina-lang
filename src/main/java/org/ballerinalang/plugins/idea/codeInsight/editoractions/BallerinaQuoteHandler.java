/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.plugins.idea.codeInsight.editoractions;

import com.intellij.codeInsight.editorActions.JavaLikeQuoteHandler;
import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.jetbrains.annotations.NotNull;

public class BallerinaQuoteHandler extends SimpleTokenSetQuoteHandler implements JavaLikeQuoteHandler {

    private static final TokenSet SINGLE_LINE_LITERALS = TokenSet.create(BallerinaTypes.QUOTED_STRING);

    public BallerinaQuoteHandler() {
        super(BallerinaTypes.DOUBLE_QUOTE, BallerinaTypes.BACK_TICK, //Needed to identify start tokens
                BallerinaTypes.QUOTED_STRING, BallerinaTypes.BACKTICKED_STRING);//Need to identify ending tokens
    }

    @Override
    protected boolean isNonClosedLiteral(HighlighterIterator iterator, CharSequence chars) {
        return true;
    }

    @Override
    public TokenSet getConcatenatableStringTokenTypes() {
        return SINGLE_LINE_LITERALS;
    }

    @Override
    public String getStringConcatenationOperatorRepresentation() {
        return "+";
    }

    @Override
    public TokenSet getStringTokenTypes() {
        return SINGLE_LINE_LITERALS;
    }

    @Override
    public boolean isAppropriateElementTypeForLiteral(@NotNull IElementType tokenType) {
        return true;
    }

    @Override
    public boolean needParenthesesAroundConcatenation(PsiElement element) {
        return false;
    }
}
