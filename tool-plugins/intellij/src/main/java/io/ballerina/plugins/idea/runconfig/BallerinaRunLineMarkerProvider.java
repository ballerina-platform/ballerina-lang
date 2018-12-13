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

package io.ballerina.plugins.idea.runconfig;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import io.ballerina.plugins.idea.BallerinaIcons;
import io.ballerina.plugins.idea.psi.BallerinaAnyIdentifierName;
import io.ballerina.plugins.idea.psi.BallerinaCallableUnitSignature;
import io.ballerina.plugins.idea.psi.BallerinaFunctionDefinition;
import io.ballerina.plugins.idea.psi.BallerinaServiceDefinition;
import io.ballerina.plugins.idea.psi.BallerinaTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides run line markers in gutter for main functions and services.
 */
public class BallerinaRunLineMarkerProvider extends RunLineMarkerContributor {

    private static final Function<PsiElement, String> APPLICATION_TOOLTIP_PROVIDER = element -> "Run Application";
    private static final Function<PsiElement, String> SERVICE_TOOLTIP_PROVIDER = element -> "Run Service";

    @Nullable
    @Override
    public Info getInfo(@NotNull PsiElement element) {

        // We don't need to check anything specific in services, since services can be defined even without an
        // identifier.
        if (element.getNode().getElementType() == BallerinaTypes.SERVICE
                && element.getParent() instanceof BallerinaServiceDefinition) {
            return new Info(BallerinaIcons.RUN, SERVICE_TOOLTIP_PROVIDER, ExecutorAction.getActions(0));
        } else if (element.getNode().getElementType() == BallerinaTypes.IDENTIFIER) {
            // Get the parent element.
            PsiElement parent = element.getParent();
            if (parent instanceof BallerinaAnyIdentifierName) {
                PsiElement superParent = parent.getParent();
                if (!(superParent instanceof BallerinaCallableUnitSignature)) {
                    return null;
                }
                // Check whether the element is an identifier of a function node.
                PsiElement superParentParent = superParent.getParent();
                if (!(superParent.getParent() instanceof BallerinaFunctionDefinition)) {
                    return null;
                }
                boolean isMain = BallerinaRunUtil.isMainFunction((BallerinaFunctionDefinition) superParentParent);
                if (!isMain) {
                    return null;
                }
                // If it is a function node, add a run line marker.
                return new Info(BallerinaIcons.RUN, APPLICATION_TOOLTIP_PROVIDER, ExecutorAction.getActions(0));
            }
        }
        return null;
    }
}
