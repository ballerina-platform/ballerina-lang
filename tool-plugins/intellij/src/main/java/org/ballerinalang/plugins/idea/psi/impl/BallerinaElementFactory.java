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
 *
 */

package org.ballerinalang.plugins.idea.psi.impl;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.PsiParserFacadeImpl;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.BallerinaFunctionDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaImportDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Element factory which creates PSI nodes.
 */
public class BallerinaElementFactory {

    @NotNull
    private static BallerinaFile createFileFromText(@NotNull Project project, @NotNull String text) {
        return (BallerinaFile) PsiFileFactory.getInstance(project).createFileFromText("a.bal",
                BallerinaLanguage.INSTANCE, text);
    }

    @NotNull
    public static PsiElement createIdentifierFromText(@NotNull Project project, @NotNull String text) {
        BallerinaFile file = createFileFromText(project, "function " + text + "(){}");
        BallerinaFunctionDefinition functionDefinition = PsiTreeUtil.findChildOfType(file,
                BallerinaFunctionDefinition.class);
        return functionDefinition.getIdentifier();
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
    public static BallerinaImportDeclaration createImportDeclaration(@NotNull Project project,
                                                                     @NotNull String importString,
                                                                     @Nullable String alias) {
        BallerinaFile file = createFileFromText(project, "import " + importString +
                (alias != null ? " as " + alias : "") + ";");
        return PsiTreeUtil.findChildOfType(file, BallerinaImportDeclaration.class);
    }
}
