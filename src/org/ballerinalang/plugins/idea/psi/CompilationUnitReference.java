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

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiWhiteSpace;
import org.jetbrains.annotations.NotNull;

public class CompilationUnitReference extends BallerinaElementReference {

    public CompilationUnitReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Override
    public boolean isDefinitionNode(PsiElement def) {
        return def instanceof FunctionDefinitionNode;
    }

    @NotNull
    @Override
    public Object[] getVariants() {

        PsiElement previousElement = getElement().getParent().getPrevSibling();


        PsiElement previousSibling = getElement().getPrevSibling();

        if (previousElement == null) {
            // First element
            if (previousSibling == null && getElement().getParent() instanceof PsiErrorElement) {
                return new Object[]{"public", "package", "import", "service", "function", "connector", "struct",
                        "typeconverter", "const"};
            }
            if (getElement().getParent() instanceof SimpleTypeNode) {
                return new Object[]{"int", "boolean", "string"};
            }

            while (previousSibling instanceof PsiWhiteSpace) {
                if (previousSibling.getPrevSibling() != null) {
                    previousSibling = previousSibling.getPrevSibling();
                }
            }

            if ("public".equals(previousSibling.getText())) {
                return new Object[]{"function", "connector", "struct", "const"};
            }

            return new Object[0];
        }

        // Get non whitespace previous sibling
        while (previousElement instanceof PsiWhiteSpace) {
            previousElement = previousElement.getPrevSibling();
        }

        //        if (previousElement == null) {
        //
        //            return new Object[]{"package", "import", "service", "function", "connector", "struct",
        // "typeconverter"};
        //        }
        if (previousElement instanceof ImportDeclarationNode || previousElement instanceof PackageDeclarationNode) {

            if (previousSibling == null) {
                return new Object[]{"public", "import", "service", "function", "connector", "struct", "typeconverter",
                        "const"};
            }
            return new Object[0];
            //            if(getElement().getPrevSibling().getPrevSibling())
        }
        return new Object[]{"public", "service", "function", "connector", "struct", "typeconverter", "const"};
    }
}
