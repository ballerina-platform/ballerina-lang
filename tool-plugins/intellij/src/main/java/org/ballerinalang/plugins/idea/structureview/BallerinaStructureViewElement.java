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
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.AnnotationDefinitionNode;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.ConnectorDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ServiceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.WorkerDeclarationNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a element in the structure view.
 */
public class BallerinaStructureViewElement implements StructureViewTreeElement, SortableTreeElement {

    protected final PsiElement element;

    BallerinaStructureViewElement(PsiElement element) {
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
        // The element can be one of BallerinaFile, ConnectorDefinitionNode instance.
        if (element instanceof BallerinaFile) {
            List<TreeElement> treeElements = new ArrayList<>();
            // Add services.
            Collection<ServiceDefinitionNode> services = PsiTreeUtil.findChildrenOfType(element,
                    ServiceDefinitionNode.class);
            for (PsiElement service : services) {
                // In here, instead of using the service, we use service.getParent(). This is done because we
                // want to show resources under a service node. This is how the sub nodes can be added.
                treeElements.add(new BallerinaStructureViewElement(service));
            }
            // Add functions.
            Collection<FunctionDefinitionNode> functions = PsiTreeUtil.findChildrenOfType(element,
                    FunctionDefinitionNode.class);
            for (PsiElement function : functions) {
                treeElements.add(new BallerinaStructureViewElement(function));
            }
            // Add connectors.
            Collection<ConnectorDefinitionNode> connectors = PsiTreeUtil.findChildrenOfType(element,
                    ConnectorDefinitionNode.class);
            for (PsiElement connector : connectors) {
                // In here, instead of using the connector, we use connector.getParent(). This is done because we
                // want to show actions under a connector node. This is how the sub nodes can be added.
                treeElements.add(new BallerinaStructureViewElement(connector));
            }
            // Add annotations.
            Collection<AnnotationDefinitionNode> annotations = PsiTreeUtil.findChildrenOfType(element,
                    AnnotationDefinitionNode.class);
            for (PsiElement annotation : annotations) {
                treeElements.add(new BallerinaStructureViewElement(annotation));
            }
            // Add structs
            Collection<StructDefinitionNode> structs = PsiTreeUtil.findChildrenOfType(element,
                    StructDefinitionNode.class);
            for (PsiElement struct : structs) {
                treeElements.add(new BallerinaStructureViewElement(struct));
            }
            // Convert the list to an array and return.
            return treeElements.toArray(new TreeElement[treeElements.size()]);
        } else if (element instanceof ConnectorDefinitionNode) {
            // If the element is a ConnectorDefinitionNode instance, we get all actions.
            List<TreeElement> treeElements = new ArrayList<>();
            // Add actions.
            Collection<ActionDefinitionNode> actions = PsiTreeUtil.findChildrenOfType(element,
                    ActionDefinitionNode.class);
            for (PsiElement action : actions) {
                treeElements.add(new BallerinaStructureViewElement(action));
            }
            // Convert the list to an array and return.
            return treeElements.toArray(new TreeElement[treeElements.size()]);
        } else if (element instanceof ServiceDefinitionNode) {
            // If the element is a ServiceDefinitionNode instance, we get all resources.
            List<TreeElement> treeElements = new ArrayList<>();
            // Add resources.
            Collection<ResourceDefinitionNode> resources = PsiTreeUtil.findChildrenOfType(element,
                    ResourceDefinitionNode.class);
            for (PsiElement resource : resources) {
                treeElements.add(new BallerinaStructureViewElement(resource));
            }

            // Convert the list to an array and return.
            return treeElements.toArray(new TreeElement[treeElements.size()]);
        } else if (element instanceof FunctionDefinitionNode || element instanceof ResourceDefinitionNode
                || element instanceof ActionDefinitionNode) {
            // If the element is a FunctionDefinitionNode instance, we get all workers.
            List<TreeElement> treeElements = new ArrayList<>();
            // Add workers.
            Collection<WorkerDeclarationNode> workers = PsiTreeUtil.findChildrenOfType(element,
                    WorkerDeclarationNode.class);
            for (PsiElement worker : workers) {
                treeElements.add(new BallerinaStructureViewElement(worker));
            }
            // Convert the list to an array and return.
            return treeElements.toArray(new TreeElement[treeElements.size()]);
        }
        // If the element type other than what we check above, return an empty array.
        return new TreeElement[0];
    }
}
