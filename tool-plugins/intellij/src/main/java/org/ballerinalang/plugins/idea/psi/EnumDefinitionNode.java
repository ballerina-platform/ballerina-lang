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

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.antlr.jetbrains.adaptor.psi.IdentifierDefSubtree;
import org.ballerinalang.plugins.idea.BallerinaIcons;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaItemPresentation;
import org.ballerinalang.plugins.idea.psi.scopes.ParameterContainer;
import org.ballerinalang.plugins.idea.psi.scopes.TopLevelDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import javax.swing.Icon;

/**
 * Represents an EnumDefinition in PSI tree.
 */
public class EnumDefinitionNode extends IdentifierDefSubtree implements TopLevelDefinition, ParameterContainer {

    public EnumDefinitionNode(@NotNull ASTNode node) {
        super(node, BallerinaTypes.IDENTIFIER);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new BallerinaItemPresentation(getNameIdentifier()) {

            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return BallerinaIcons.ENUM;
            }
        };
    }

    @Nullable
    @Override
    public PsiElement resolve(PsiNamedElement element) {
        Collection<EnumFieldNode> fields = PsiTreeUtil.findChildrenOfType(this,
                EnumFieldNode.class);
        for (EnumFieldNode field : fields) {
            IdentifierPSINode fieldName = PsiTreeUtil.getChildOfType(field, IdentifierPSINode.class);
            if (fieldName != null) {
                if (fieldName.getText().equals(element.getText())) {
                    return fieldName;
                }
            }
        }
        return null;
    }
}
