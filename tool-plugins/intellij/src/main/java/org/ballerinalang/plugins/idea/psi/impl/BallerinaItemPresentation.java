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

package org.ballerinalang.plugins.idea.psi.impl;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public abstract class BallerinaItemPresentation implements ItemPresentation {

    private PsiElement nameIdentifier;

    public BallerinaItemPresentation(PsiElement nameIdentifier) {
        this.nameIdentifier = nameIdentifier;
    }

    @Nullable
    @Override
    public String getPresentableText() {
        if (nameIdentifier == null) {
            return null;
        }
        return nameIdentifier.getText();
    }

    @Nullable
    @Override
    public String getLocationString() {
        return null;
    }
}
