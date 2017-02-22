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
import com.intellij.psi.ResolveResult;
import org.antlr.jetbrains.adaptor.SymtabUtils;
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
        //		System.out.println(getClass().getSimpleName()+
        //		                   ".resolve("+element.getName()+
        //		                   " at "+Integer.toHexString(element.hashCode())+")");
        if (element.getParent() instanceof CallableUnitNameNode) {
            return BallerinaPsiImplUtil.resolveElement(this, element,"//function/Identifier",
                    "//connector/Identifier");
        } else if (element.getParent() instanceof SimpleTypeNode) {
            return BallerinaPsiImplUtil.resolveElement(this, element, "//function/Identifier",
                    "//connector/Identifier", "//structDefinition/Identifier");
        } else if (element.getParent() instanceof VariableReferenceNode) {
            return SymtabUtils.resolve(this, BallerinaLanguage.INSTANCE, element,
                    "//constantDefinition/Identifier");
        }
        return null;
    }

    @Override
    public ResolveResult[] multiResolve(IdentifierPSINode myElement) {
        return new ResolveResult[0];
    }
}
