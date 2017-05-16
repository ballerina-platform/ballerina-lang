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

import com.intellij.icons.AllIcons;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import org.antlr.jetbrains.adaptor.SymtabUtils;
import org.antlr.jetbrains.adaptor.psi.IdentifierDefSubtree;
import org.antlr.jetbrains.adaptor.psi.ScopeNode;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.BallerinaParserDefinition;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class FunctionDefinitionNode extends IdentifierDefSubtree implements ScopeNode {

    public FunctionDefinitionNode(@NotNull ASTNode node) {
        super(node, BallerinaParserDefinition.ID);
    }

    @Nullable
    @Override
    public PsiElement resolve(PsiNamedElement element) {
        // WARNING: SymtabUtils.resolve() will return the element node instead of the Identifier node. This might
        // cause issues when using find usage, etc.
        if (element.getParent() instanceof NameReferenceNode) {
            return BallerinaPsiImplUtil.resolveElement(this, element, "//functionDefinition/Identifier",
                    "//parameter/Identifier");
        } else if (element.getParent() instanceof VariableReferenceNode) {
            return BallerinaPsiImplUtil.resolveElement(this, element, "//parameter/Identifier");
        } else if (element.getParent() instanceof TypeNameNode) {
            return SymtabUtils.resolve(this, BallerinaLanguage.INSTANCE, element,
                    "//connectorDefinition/Identifier");
        }
        return null;
    }

    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {

            @Nullable
            @Override
            public String getPresentableText() {
                // Get the identifier.
                PsiElement nameIdentifier = getNameIdentifier();
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

            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return AllIcons.Nodes.Field;
            }
        };
    }
}
