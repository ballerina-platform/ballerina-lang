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

package org.ballerinalang.plugins.idea.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.antlr.jetbrains.adaptor.psi.ScopeNode;
import org.ballerinalang.plugins.idea.BallerinaFileType;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class BallerinaFile extends PsiFileBase implements ScopeNode {

    public BallerinaFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, BallerinaLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return BallerinaFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Ballerina File";
    }

    @Override
    public Icon getIcon(int flags) {
        return super.getIcon(flags);
    }

    /**
     * Return null since a file scope has no enclosing scope. It is not itself in a scope.
     */
    @Override
    public ScopeNode getContext() {
        return null;
    }

    @Nullable
    @Override
    public PsiElement resolve(PsiNamedElement element) {
//        if (element.getParent() instanceof NameReferenceNode || element.getParent() instanceof StatementNode) {
//            PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(element.getParent(), PackageNameNode.class);
//            if (packageNameNode == null) {
//                return BallerinaPsiImplUtil.resolveElement(this, element, "//functionDefinition/Identifier",
//                        "//connectorDefinition/Identifier", "//structDefinition/Identifier",
//                        "//constantDefinition/Identifier", "//globalVariableDefinition/Identifier",
//                        "//annotationDefinition/Identifier");
//            }
//        } else if (element.getParent() instanceof TypeNameNode) {
//            return BallerinaPsiImplUtil.resolveElement(this, element, "//functionDefinition/Identifier",
//                    "//connectorDefinition/Identifier", "//structDefinition/Identifier");
//        } else if (element.getParent() instanceof VariableReferenceNode) {
//            return BallerinaPsiImplUtil.resolveElement(this, element, "//constantDefinition/Identifier");
//        } else if (element.getParent() instanceof AnnotationAttributeNode) {
//            AnnotationAttachmentNode annotationAttachmentNode = PsiTreeUtil.getParentOfType(element,
//                    AnnotationAttachmentNode.class);
//            if (annotationAttachmentNode == null) {
//                return null;
//            }
//            AnnotationDefinitionNode annotationDefinitionNode = BallerinaPsiImplUtil.getAnnotationDefinitionNode
//                    (annotationAttachmentNode);
//            if (annotationDefinitionNode == null) {
//                return null;
//            }
//            return BallerinaPsiImplUtil.resolveElement(annotationDefinitionNode, element,
//                    "//fieldDefinition/Identifier");
//        }
        return null;
    }
}
