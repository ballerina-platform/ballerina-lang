/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.plugins.idea.spellchecker;

import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import io.ballerina.plugins.idea.psi.BallerinaIdentifier;
import io.ballerina.plugins.idea.psi.BallerinaTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Provides spell checking strategy for Ballerina files.
 */
public class BallerinaSpellcheckingStrategy extends SpellcheckingStrategy {

    private final BallerinaIdentifierTokenizer identifierTokenizer = new BallerinaIdentifierTokenizer();

    @NotNull
    @Override
    public Tokenizer getTokenizer(PsiElement element) {
        if (element instanceof BallerinaIdentifier) {
            if (((BallerinaIdentifier) element).getElementType() == BallerinaTypes.IDENTIFIER) {
                return identifierTokenizer;
            }
        }
        return super.getTokenizer(element);
    }

    // Todo - Add support for markdown documentation and line comments.
}
