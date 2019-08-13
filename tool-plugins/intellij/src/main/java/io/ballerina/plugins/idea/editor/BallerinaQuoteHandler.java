/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.plugins.idea.editor;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import io.ballerina.plugins.idea.psi.BallerinaTypes;

/**
 * Responsible for entering the closing quotes when we type a double quote.
 */
public class BallerinaQuoteHandler extends SimpleTokenSetQuoteHandler {

    public BallerinaQuoteHandler() {
        super(BallerinaTypes.BACKTICK, //Needed to identify start tokens
                BallerinaTypes.QUOTED_STRING_LITERAL); //Need to identify ending tokens
    }

    @Override
    protected boolean isNonClosedLiteral(HighlighterIterator iterator, CharSequence chars) {
        return true;
    }
}
