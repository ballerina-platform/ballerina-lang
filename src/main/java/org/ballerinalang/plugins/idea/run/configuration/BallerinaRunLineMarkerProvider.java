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

package org.ballerinalang.plugins.idea.run.configuration;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import org.ballerinalang.plugins.idea.BallerinaConstants;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.FunctionNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.ParameterListNode;
import org.ballerinalang.plugins.idea.psi.ServiceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.SimpleTypeArrayNode;
import org.jetbrains.annotations.Nullable;

public class BallerinaRunLineMarkerProvider extends RunLineMarkerContributor {

    private static final Function<PsiElement, String> APPLICATION_TOOLTIP_PROVIDER = element -> "Run Application";
    private static final Function<PsiElement, String> SERVICE_TOOLTIP_PROVIDER = element -> "Run Services";

    @Nullable
    @Override
    public Info getInfo(PsiElement element) {
        // We only need to add Run line marker to functions and services. So we check whether the element is an
        // identifier.
        if (element != null && element.getNode().getElementType() == BallerinaTypes.IDENTIFIER) {
            // Get the parent element.
            PsiElement parent = element.getParent();
            if (parent instanceof FunctionNode) {
                // Check whether the current function is a main function.
                if (BallerinaConstants.MAIN.equals(element.getText())) {
                    // If the current file has a package declaration, it should not be runnable.
                    // Get the BallerinaFile which contains the element.
                    BallerinaFile parentFile = PsiTreeUtil.getParentOfType(element, BallerinaFile.class);
                    if (parentFile != null) {
                        // Get the PackageDeclarationNode if available.
                        PackageDeclarationNode packageDeclarationNode = PsiTreeUtil.findChildOfType(parentFile,
                                PackageDeclarationNode.class);
                        if (packageDeclarationNode != null) {
                            return null;
                        }
                    }
                    // Get the parameter list.
                    ParameterListNode parameterListNode = PsiTreeUtil.getChildOfType(parent, ParameterListNode.class);
                    if (parameterListNode == null) {
                        return null;
                    }
                    // There should be only one argument in the main function.
                    PsiElement[] children = parameterListNode.getChildren();
                    if (children.length != 1) {
                        return null;
                    }
                    // Argument type must be SimpleTypeArrayNode because it is a string array.
                    SimpleTypeArrayNode simpleTypeArrayNode =
                            PsiTreeUtil.findChildOfType(children[0], SimpleTypeArrayNode.class);
                    if (simpleTypeArrayNode == null) {
                        return null;
                    }
                    // Get the type.
                    PsiElement nameIdentifier = simpleTypeArrayNode.getNameIdentifier();
                    if (nameIdentifier == null) {
                        return null;
                    }
                    // Type must be string.
                    if ("string".equals(nameIdentifier.getText())) {
                        // If all tests are passed, that means the current element is the identifier of a main
                        // function. So we return a new Info object.
                        return new Info(AllIcons.RunConfigurations.TestState.Run, APPLICATION_TOOLTIP_PROVIDER,
                                ExecutorAction.getActions(0));
                    }
                }
            } else if (parent instanceof ServiceDefinitionNode) {
                // If the current file has a package declaration, it should not be runnable.
                // Get the BallerinaFile which contains the element.
                BallerinaFile parentFile = PsiTreeUtil.getParentOfType(element, BallerinaFile.class);
                if (parentFile != null) {
                    // Get the PackageDeclarationNode if available.
                    PackageDeclarationNode packageDeclarationNode = PsiTreeUtil.findChildOfType(parentFile,
                            PackageDeclarationNode.class);
                    if (packageDeclarationNode != null) {
                        return null;
                    }
                }
                // We don't need to check anything specific in services. If there is a ServiceDefinitionNode, that
                // means there is a service. We just return a new Info object.
                return new Info(AllIcons.RunConfigurations.TestState.Run, SERVICE_TOOLTIP_PROVIDER,
                        ExecutorAction.getActions(0));
            }
        }
        return null;
    }
}
