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

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.PsiParserFacadeImpl;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used to create new PSI elements.
 */
public class BallerinaElementFactory {

    private BallerinaElementFactory() {

    }

    @NotNull
    private static BallerinaFile createFileFromText(@NotNull Project project, @NotNull String text) {
        return (BallerinaFile) PsiFileFactory.getInstance(project).createFileFromText("a.bal",
                BallerinaLanguage.INSTANCE, text);
    }

    @NotNull
    public static ImportDeclarationNode createImportDeclaration(@NotNull Project project,
                                                                @NotNull String importString,
                                                                @Nullable String alias) {
        BallerinaFile file = createFileFromText(project, "import " + importString +
                (alias != null ? " as " + alias : "") + ";");
        return PsiTreeUtil.findChildOfType(file, ImportDeclarationNode.class);
    }

    @NotNull
    public static PsiElement createNewLine(@NotNull Project project) {
        return PsiParserFacadeImpl.SERVICE.getInstance(project).createWhiteSpaceFromText("\n");
    }

    @NotNull
    public static PsiElement createDoubleNewLine(@NotNull Project project) {
        return PsiParserFacadeImpl.SERVICE.getInstance(project).createWhiteSpaceFromText("\n\n");
    }

    @NotNull
    public static PsiElement createIdentifier(@NotNull Project project, @NotNull String text) {
        BallerinaFile file = createFileFromText(project, "package " + text + ";");
        return PsiTreeUtil.findChildOfType(file, IdentifierPSINode.class);
    }

    @NotNull
    public static PackageDeclarationNode createPackageDeclaration(@NotNull Project project,
                                                                  @NotNull String packageString) {
        BallerinaFile file = createFileFromText(project, "package " + packageString + ";");
        return PsiTreeUtil.findChildOfType(file, PackageDeclarationNode.class);
    }
}
