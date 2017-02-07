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

public class Reference extends BallerinaElementReference {

    public Reference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Override
    public boolean isDefinitionNode(PsiElement def) {
        return def instanceof FunctionDefinitionNode;
    }

    @NotNull
    @Override
    public Object[] getVariants() {

        PsiElement parentElement = getElement().getParent().getPrevSibling();


        if (parentElement == null) {
            // First element
            if (getElement().getPrevSibling() == null && getElement().getParent() instanceof PsiErrorElement) {
                return new Object[]{"package", "import", "service", "function", "connector", "struct", "typeconverter",
                        "const"};
            }
            if (getElement().getParent() instanceof SimpleTypeNode) {
                return new Object[]{"int", "boolean", "string"};
            }
            return new Object[0];
        }

        // Get non whitespace previous sibling
        while (parentElement instanceof PsiWhiteSpace) {
            parentElement = parentElement.getPrevSibling();
        }

//        if (parentElement == null) {
//
//            return new Object[]{"package", "import", "service", "function", "connector", "struct", "typeconverter"};
//        }
        if (parentElement instanceof ImportDeclarationNode || parentElement instanceof PackageDeclarationNode) {

            if (getElement().getPrevSibling()==null) {
                return new Object[]{"import", "service", "function", "connector", "struct", "typeconverter", "const"};
            }
            return new Object[0];
//            if(getElement().getPrevSibling().getPrevSibling())
        }
        return new Object[]{"service", "function", "connector", "struct", "typeconverter"};
    }
}
