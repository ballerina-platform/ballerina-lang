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

package org.ballerinalang.plugins.idea.structureview;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiFile;
import org.ballerinalang.plugins.idea.BallerinaIcons;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class BallerinaRootPresentation implements ItemPresentation {

    protected final PsiFile element;

    BallerinaRootPresentation(PsiFile element) {
        this.element = element;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean unused) {
        return BallerinaIcons.ICON;
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return element.getVirtualFile().getNameWithoutExtension();
    }

    @Nullable
    @Override
    public String getLocationString() {
        return null;
    }
}
