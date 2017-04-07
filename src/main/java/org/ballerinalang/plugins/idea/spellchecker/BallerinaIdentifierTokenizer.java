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

package org.ballerinalang.plugins.idea.spellchecker;

import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import org.jetbrains.annotations.NotNull;

public class BallerinaIdentifierTokenizer extends Tokenizer<PsiNameIdentifierOwner> {

    @Override
    public void tokenize(@NotNull PsiNameIdentifierOwner element, TokenConsumer consumer) {
        // Do nothing here. This is used to prevent adding multiple fix suggestions (Rename To, etc) to the code.
        // Otherwise one suggestion will be added when the identifier is processed, another will be added when the
        // parent node of the identifier is processed.
    }
}