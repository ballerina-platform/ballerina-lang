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
 */

package io.ballerina.plugins.idea.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.ElementBase;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.RowIcon;
import com.intellij.usageView.UsageViewUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PlatformIcons;
import io.ballerina.plugins.idea.BallerinaIcons;
import io.ballerina.plugins.idea.psi.BallerinaCompositeElement;
import io.ballerina.plugins.idea.psi.BallerinaFunctionDefinition;
import io.ballerina.plugins.idea.psi.BallerinaGlobalVariableDefinition;
import io.ballerina.plugins.idea.psi.BallerinaNamedElement;
import io.ballerina.plugins.idea.psi.BallerinaTypeDefinition;
import io.ballerina.plugins.idea.psi.BallerinaTypeName;
import io.ballerina.plugins.idea.psi.BallerinaTypes;
import io.ballerina.plugins.idea.stubs.BallerinaNamedStub;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * Implementation class of the named element class.
 *
 * @param <T> stub type
 */
public abstract class BallerinaNamedElementImpl<T extends BallerinaNamedStub<?>> extends BallerinaStubbedElementImpl<T>
        implements BallerinaCompositeElement, BallerinaNamedElement {

    public BallerinaNamedElementImpl(@NotNull T stub, @NotNull IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public BallerinaNamedElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean isPublic() {
        T stub = getStub();
        if (stub != null && stub.isPublic()) {
            return true;
        }
        PsiElement firstChild = getFirstChild();
        return firstChild.getNode().getElementType() == BallerinaTypes.PUBLIC;
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        return getIdentifier();
    }

    @Nullable
    @Override
    public String getName() {
        T stub = getStub();
        if (stub != null) {
            return stub.getName();
        }
        PsiElement identifier = getIdentifier();
        return identifier != null ? identifier.getText() : null;
    }

    @Nullable
    @Override
    public String getQualifiedName() {
        String name = getName();
        if (name == null) {
            return null;
        }
        return "";
    }

    @Override
    public int getTextOffset() {
        PsiElement identifier = getIdentifier();
        return identifier != null ? identifier.getTextOffset() : super.getTextOffset();
    }

    @NotNull
    @Override
    public PsiElement setName(@NonNls @NotNull String newName) throws IncorrectOperationException {
        PsiElement identifier = getIdentifier();
        if (identifier != null) {
            identifier.replace(BallerinaElementFactory.createIdentifierFromText(getProject(), newName));
        }
        return this;
    }

    @Nullable
    @Override
    public BallerinaTypeName getBallerinaType(@Nullable ResolveState context) {
        return null;
    }

    @Nullable
    @Override
    public BallerinaTypeName findSiblingType() {
        T stub = getStub();
        if (stub != null) {
            // Todo - Need this method?
        }
        return PsiTreeUtil.getNextSiblingOfType(this, BallerinaTypeName.class);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state,
                                       PsiElement lastParent, @NotNull PsiElement place) {
        return BallerinaCompositeElementImpl.processDeclarationsDefault(this, processor, state, lastParent, place);
    }

    // Todo - Remove?
    @Override
    public ItemPresentation getPresentation() {
        // Todo - Update presentation
        String text = UsageViewUtil.createNodeText(this);
        if (text != null) {
            return new ItemPresentation() {
                @Nullable
                @Override
                public String getPresentableText() {
                    return getName();
                }

                @NotNull
                @Override
                public String getLocationString() {
                    return "";
                }

                @Nullable
                @Override
                public Icon getIcon(boolean b) {
                    return BallerinaNamedElementImpl.this.getIcon(Iconable.ICON_FLAG_VISIBILITY);
                }
            };
        }
        return super.getPresentation();
    }

    @Nullable
    @Override
    public Icon getIcon(int flags) {
        // Todo - Add more icons
        Icon icon = null;
        if (this instanceof BallerinaFunctionDefinition) {
            icon = BallerinaIcons.FUNCTION;
        } else if (this instanceof BallerinaGlobalVariableDefinition) {
            icon = BallerinaIcons.GLOBAL_VARIABLE;
        } else if (this instanceof BallerinaTypeDefinition) {
            icon = BallerinaIcons.TYPE;
        }
        if (icon != null) {
            if ((flags & Iconable.ICON_FLAG_VISIBILITY) != 0) {
                RowIcon rowIcon = ElementBase.createLayeredIcon(this, icon, flags);
                rowIcon.setIcon(isPublic() ? PlatformIcons.PUBLIC_ICON : PlatformIcons.PRIVATE_ICON, 1);
                return rowIcon;
            }
            return icon;
        }
        return super.getIcon(flags);
    }

    @NotNull
    @Override
    public GlobalSearchScope getResolveScope() {
        return GlobalSearchScope.EMPTY_SCOPE;
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
        return GlobalSearchScope.allScope(getProject());
    }

    @Override
    public boolean isBlank() {
        return StringUtil.equals(getName(), "_");
    }

    @Override
    public boolean shouldGoDeeper() {
        return true;
    }
}
