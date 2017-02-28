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
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import org.antlr.jetbrains.adaptor.SymtabUtils;
import org.antlr.jetbrains.adaptor.psi.IdentifierDefSubtree;
import org.antlr.jetbrains.adaptor.psi.ScopeNode;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.BallerinaParserDefinition;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import javax.swing.*;

public class FunctionNode extends IdentifierDefSubtree implements ScopeNode {

    public FunctionNode(@NotNull ASTNode node) {
        super(node, BallerinaParserDefinition.ID);
    }

    @Nullable
    @Override
    public PsiElement resolve(PsiNamedElement element) {
        if (element.getParent() instanceof CallableUnitNameNode) {
            return SymtabUtils.resolve(this, BallerinaLanguage.INSTANCE, element,
                    "//function/Identifier");
        } else if (element.getParent() instanceof VariableReferenceNode) {
            return BallerinaPsiImplUtil.resolveElement(this, element, "//parameter/Identifier",
                    "//namedParameter/Identifier");
        } else if (element.getParent() instanceof SimpleTypeNode) {
            return SymtabUtils.resolve(this, BallerinaLanguage.INSTANCE, element,
                    "//connector/Identifier");
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
                // Create a new StringBuilder. We will use this to build the presentable text.
                StringBuilder builder = new StringBuilder(nameIdentifier.getText());
                // Get the parameter list node.
                ParameterListNode parameterListNode = PsiTreeUtil.findChildOfType(FunctionNode.this,
                        ParameterListNode.class);
                if (parameterListNode == null) {
                    return builder.toString();
                }
                // Get the parameters.
                Collection<SimpleTypeNode> parameterTypeNodes = PsiTreeUtil.findChildrenOfType(parameterListNode,
                        SimpleTypeNode.class);
                builder.append(" (");
                for (SimpleTypeNode typeNode : parameterTypeNodes) {
                    builder.append(typeNode.getText()).append(",");
                }
                // Remove the extra ',' at the end.
                builder.deleteCharAt(builder.length() - 1);
                builder.append(")");

                // Get the return type list node.
                ReturnTypeListNode returnTypeListNode = PsiTreeUtil.findChildOfType(FunctionNode.this,
                        ReturnTypeListNode.class);
                if (returnTypeListNode == null) {
                    return builder.toString();
                }
                // Get the return types.
                Collection<SimpleTypeNode> returnTypeNodes = PsiTreeUtil.findChildrenOfType(parameterListNode,
                        SimpleTypeNode.class);
                builder.append(" (");
                for (SimpleTypeNode typeNode : returnTypeNodes) {
                    builder.append(typeNode.getText()).append(",");
                }
                // Remove the extra ',' at the end.
                builder.deleteCharAt(builder.length() - 1);
                builder.append(")");
                // Return the presentable text.
                return builder.toString();
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
