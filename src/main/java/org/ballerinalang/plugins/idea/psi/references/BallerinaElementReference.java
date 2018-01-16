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

package org.ballerinalang.plugins.idea.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import org.antlr.jetbrains.adaptor.psi.ScopeNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BallerinaElementReference extends PsiReferenceBase<IdentifierPSINode> {

    public BallerinaElementReference(@NotNull IdentifierPSINode element) {
        /** WARNING: You must send up the text range or you get this error:
         * "Cannot find manipulator for PsiElement(ID) in org.antlr.jetbrains.sample.SampleElementRef"...
         *  when you click on an identifier.  During rename you get this
         *  error too if you don't impl handleElementRename().
         *
         *  The range is relative to start of the token; I guess for
         *  qualified references we might want to use just a part of the name.
         *  Or we might look inside string literals for stuff.
         */
        super(element, new TextRange(0, element.getText().length()));
    }

    /**
     * Change the REFERENCE's ID node (not the targeted def's ID node)
     * to reflect a rename.
     * <p>
     * Without this method, we get an error ("Cannot find manipulator...").
     * <p>
     * getElement() refers to the identifier node that references the definition.
     */
    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        return myElement.setName(newElementName);
    }

    /**
     * Resolve a reference to the definition subtree (subclass of
     * IdentifierDefSubtree), do not resolve to the ID child of that
     * definition subtree root.
     */
    @Nullable
    @Override
    public PsiElement resolve() {
        ScopeNode scope = (ScopeNode) myElement.getContext();
        if (scope == null) {
            return null;
        }
        return scope.resolve(myElement);
    }
}
