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

package org.ballerinalang.plugins.idea.completion;

import com.intellij.codeInsight.highlighting.PairedBraceMatcherAdapter;
import com.intellij.lang.BracePair;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.tree.IElementType;
import org.ballerinalang.plugins.idea.BallerinaFileType;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.codeinsight.highlighting.BallerinaBraceMatcher;

public class BallerinaPairedBraceMatcher extends PairedBraceMatcherAdapter {

    public BallerinaPairedBraceMatcher() {
        super(new BallerinaBraceMatcher(), BallerinaLanguage.INSTANCE);
    }

    @Override
    public boolean isLBraceToken(HighlighterIterator iterator, CharSequence fileText, FileType fileType) {
        return isBrace(iterator, fileText, fileType, true);
    }

    @Override
    public boolean isRBraceToken(HighlighterIterator iterator, CharSequence fileText, FileType fileType) {
        return isBrace(iterator, fileText, fileType, false);
    }

    private boolean isBrace(HighlighterIterator iterator, CharSequence fileText, FileType fileType, boolean left) {
        final BracePair pair = findPair(left, iterator, fileText, fileType);
        if (pair == null) {
            return false;
        }

        final IElementType opposite = left ? BallerinaTypes.GT : BallerinaTypes.LT;
        if ((left ? pair.getRightBraceType() : pair.getLeftBraceType()) != opposite) {
            return true;
        }
        if (fileType != BallerinaFileType.INSTANCE) {
            return false;
        }
        return true;
    }
}
