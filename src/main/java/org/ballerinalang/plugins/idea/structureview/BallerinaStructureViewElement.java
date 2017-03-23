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

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import org.antlr.jetbrains.adaptor.xpath.XPath;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BallerinaStructureViewElement implements StructureViewTreeElement, SortableTreeElement {
    protected final PsiElement element;

    public BallerinaStructureViewElement(PsiElement element) {
        this.element = element;
    }

    @Override
    public Object getValue() {
        return element;
    }

    @Override
    public void navigate(boolean requestFocus) {
        if (element instanceof NavigationItem) {
            ((NavigationItem) element).navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigate() {
        return element instanceof NavigationItem && ((NavigationItem) element).canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return element instanceof NavigationItem && ((NavigationItem) element).canNavigateToSource();
    }

    @NotNull
    @Override
    public String getAlphaSortKey() {
        String s = element instanceof PsiNamedElement ? ((PsiNamedElement) element).getName() : null;
        if (s == null) {
            return "unknown key";
        }
        return s;
    }

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        return new BallerinaItemPresentation(element);
    }

    @NotNull
    @Override
    public TreeElement[] getChildren() {
        //Todo - Add more children types
        if (element instanceof BallerinaFile) {
            List<TreeElement> treeElements = new ArrayList<>();

            Collection<? extends PsiElement> services = XPath.findAll(BallerinaLanguage.INSTANCE, element,
                    "//serviceDefinition/Identifier");
            for (PsiElement el : services) {
                treeElements.add(new BallerinaStructureViewElement(el));
            }

            Collection<? extends PsiElement> functions = XPath.findAll(BallerinaLanguage.INSTANCE, element,
                    "//functionDefinition/Identifier");
            for (PsiElement el : functions) {
                treeElements.add(new BallerinaStructureViewElement(el));
            }

            Collection<? extends PsiElement> connectors = XPath.findAll(BallerinaLanguage.INSTANCE, element,
                    "//connectorDefinition/Identifier");
            for (PsiElement el : connectors) {
                treeElements.add(new BallerinaStructureViewElement(el));
            }

            return treeElements.toArray(new TreeElement[functions.size()]);
        }
        return new TreeElement[0];
    }
}
