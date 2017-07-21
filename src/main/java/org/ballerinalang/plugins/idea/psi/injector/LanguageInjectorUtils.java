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

package org.ballerinalang.plugins.idea.psi.injector;

import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.psi.ExpressionListNode;
import org.ballerinalang.plugins.idea.psi.ExpressionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.QuotedLiteralString;
import org.ballerinalang.plugins.idea.psi.VariableReferenceNode;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

public class LanguageInjectorUtils {

    private LanguageInjectorUtils() {

    }

    public static boolean isValid(@NotNull PsiLanguageInjectionHost host, @NotNull Set<String> packageNames,
                                  @NotNull Set<String> functionNames) {
        if (!(host instanceof QuotedLiteralString)) {
            return false;
        }
        ExpressionListNode expressionListNode = PsiTreeUtil.getTopmostParentOfType(host, ExpressionListNode.class);
        if (expressionListNode == null) {
            return false;
        }
        VariableReferenceNode variableReferenceNode = PsiTreeUtil.getPrevSiblingOfType(expressionListNode,
                VariableReferenceNode.class);
        if (variableReferenceNode == null) {
            return false;
        }
        NameReferenceNode nameReferenceNode = PsiTreeUtil.getChildOfType(variableReferenceNode,
                NameReferenceNode.class);
        if (nameReferenceNode == null) {
            return false;
        }
        PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(nameReferenceNode, PackageNameNode.class);
        if (packageNameNode == null) {
            return false;
        }
        IdentifierPSINode functionName = PsiTreeUtil.getChildOfType(nameReferenceNode, IdentifierPSINode.class);
        if (functionName == null) {
            return false;
        }
        ExpressionNode expressionNode = PsiTreeUtil.getParentOfType(host, ExpressionNode.class);
        if (expressionNode == null) {
            return false;
        }
        Collection<QuotedLiteralString> quotedLiteralStrings = PsiTreeUtil.findChildrenOfType(expressionNode,
                QuotedLiteralString.class);
        // We ignore this case since the string might not be identified correctly and will cause issues.
        if (quotedLiteralStrings.size() > 1) {
            return false;
        }
        return packageNames.contains(packageNameNode.getText()) && functionNames.contains(functionName.getText());
    }
}
