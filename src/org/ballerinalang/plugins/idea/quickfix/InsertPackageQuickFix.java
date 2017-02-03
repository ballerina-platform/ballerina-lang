///*
// *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *  http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// */
//
//package org.ballerinalang.plugins.idea.quickfix;
//
//import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
//import com.intellij.lang.ASTNode;
//import com.intellij.openapi.application.ApplicationManager;
//import com.intellij.openapi.command.WriteCommandAction;
//import com.intellij.openapi.editor.Editor;
//import com.intellij.openapi.project.Project;
//import com.intellij.psi.PsiFile;
//import com.intellij.util.IncorrectOperationException;
//import org.ballerinalang.plugins.idea.psi.BallerinaElementFactory;
//import org.ballerinalang.plugins.idea.psi.BallerinaFile;
//import org.ballerinalang.plugins.idea.psi.BallerinaPackageDeclaration;
//import org.jetbrains.annotations.Nls;
//import org.jetbrains.annotations.NotNull;
//
//public class InsertPackageQuickFix extends BaseIntentionAction {
//
//    private String packageName;
//
//    public InsertPackageQuickFix(String packageName) {
//        this.packageName = packageName;
//    }
//
//    @NotNull
//    @Override
//    public String getText() {
//        return "Insert package declaration '" + packageName + "'";
//    }
//
//    @Nls
//    @NotNull
//    @Override
//    public String getFamilyName() {
//        return "Ballerina package";
//    }
//
//    @Override
//    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
//        return true;
//    }
//
//    @Override
//    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
//        ApplicationManager.getApplication().invokeLater(() -> changePackageDeclaration(project, psiFile));
//    }
//
//    private void changePackageDeclaration(Project project, PsiFile psiFile) {
//        new WriteCommandAction.Simple(project) {
//            @Override
//            public void run() {
//                BallerinaFile ballerinaFile = (BallerinaFile) psiFile;
//                BallerinaPackageDeclaration packageDeclaration = BallerinaElementFactory
//                        .createPackageDeclaration(project, packageName);
//                ASTNode firstChildNode = ballerinaFile.getNode().getFirstChildNode();
//                ballerinaFile.getNode().getPsi().addBefore(packageDeclaration.getNode().getPsi(), firstChildNode
//                        .getPsi());
//            }
//        }.execute();
//    }
//}
