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
import com.intellij.psi.PsiElement;
import org.antlr.jetbrains.adaptor.psi.IdentifierDefSubtree;
import org.ballerinalang.plugins.idea.BallerinaIcons;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.AnnotationDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ConnectorDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ServiceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.WorkerDeclarationNode;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * Provides Ballerina item presentation in structure view.
 */
public class BallerinaItemPresentation implements ItemPresentation {

    protected final PsiElement element;

    protected BallerinaItemPresentation(PsiElement element) {
        this.element = element;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean unused) {
        if (element instanceof FunctionDefinitionNode) {
            return BallerinaIcons.FUNCTION;
        } else if (element instanceof ConnectorDefinitionNode) {
            return BallerinaIcons.CONNECTOR;
        } else if (element instanceof ServiceDefinitionNode) {
            return BallerinaIcons.SERVICE;
        } else if (element instanceof AnnotationDefinitionNode) {
            return BallerinaIcons.ANNOTATION;
        } else if (element instanceof ActionDefinitionNode) {
            return BallerinaIcons.ACTION;
        } else if (element instanceof StructDefinitionNode) {
            return BallerinaIcons.STRUCT;
        } else if (element instanceof ResourceDefinitionNode) {
            return BallerinaIcons.RESOURCE;
        } else if (element instanceof WorkerDeclarationNode) {
            return BallerinaIcons.WORKER;
        }
        return BallerinaIcons.ICON;
    }

    @Nullable
    @Override
    public String getPresentableText() {
        // Todo - Add parameters, return types
        //        ASTNode node = element.getNode();
        if (element instanceof IdentifierDefSubtree) {
            PsiElement nameIdentifier = ((IdentifierDefSubtree) element).getNameIdentifier();
            if (nameIdentifier != null) {
                return nameIdentifier.getText();
            }
        }
        return element.getText();
    }

    @Nullable
    @Override
    public String getLocationString() {
        return null;
    }
}
