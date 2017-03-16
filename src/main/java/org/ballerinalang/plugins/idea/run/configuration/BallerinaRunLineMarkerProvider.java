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
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import org.ballerinalang.plugins.idea.BallerinaConstants;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.psi.FunctionNode;
import org.ballerinalang.plugins.idea.psi.ParameterListNode;
import org.ballerinalang.plugins.idea.psi.ServiceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.SimpleTypeArrayNode;
import org.jetbrains.annotations.Nullable;

public class BallerinaRunLineMarkerProvider extends RunLineMarkerContributor {

    private static final Function<PsiElement, String> APPLICATION_TOOLTIP_PROVIDER = element -> "Run Application";
    private static final Function<PsiElement, String> SERVICE_TOOLTIP_PROVIDER = element -> "Run Services";

    @Nullable
    @Override
    public Info getInfo(PsiElement e) {
        if (e != null && e.getNode().getElementType() == BallerinaTypes.IDENTIFIER) {
            PsiElement parent = e.getParent();
            if (parent instanceof FunctionNode) {
                if (BallerinaConstants.MAIN.equals(e.getText())) {
                    ParameterListNode parameterListNode = PsiTreeUtil.getChildOfType(parent, ParameterListNode.class);
                    if (parameterListNode == null) {
                        return null;
                    }
                    PsiElement[] children = parameterListNode.getChildren();
                    if (children.length != 1) {
                        return null;
                    }
                    SimpleTypeArrayNode simpleTypeArrayNode =
                            PsiTreeUtil.findChildOfType(children[0], SimpleTypeArrayNode.class);
                    if (simpleTypeArrayNode == null) {
                        return null;
                    }
                    PsiElement nameIdentifier = simpleTypeArrayNode.getNameIdentifier();
                    if (nameIdentifier == null) {
                        return null;
                    }
                    if ("string".equals(nameIdentifier.getText())) {
                        return new Info(AllIcons.RunConfigurations.TestState.Run, APPLICATION_TOOLTIP_PROVIDER,
                                ExecutorAction.getActions(0));
                    }
                }
            } else if (parent instanceof ServiceDefinitionNode) {
                return new Info(AllIcons.RunConfigurations.TestState.Run, SERVICE_TOOLTIP_PROVIDER,
                        ExecutorAction.getActions(0));
            }
        }
        return null;
    }
}
