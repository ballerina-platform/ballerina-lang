/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.InvocationNode;
import org.ballerinalang.model.tree.expressions.VariableReferenceNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of {@link org.ballerinalang.model.tree.expressions.InvocationNode}
 *
 * @since 0.94
 */
public class BLangInvocation extends BLangVariableReference implements InvocationNode {

    public IdentifierNode packIdentifier, functionName;
    public List<ExpressionNode> argsExpressions = new ArrayList<>();
    public VariableReferenceNode variableReferenceNode;

    @Override
    public IdentifierNode getPackageIdentifier() {
        return packIdentifier;
    }

    @Override
    public IdentifierNode getFunctionName() {
        return functionName;
    }

    @Override
    public List<? extends ExpressionNode> getArgumentExpressions() {
        return argsExpressions;
    }

    @Override
    public VariableReferenceNode getVariableReferenceNode() {
        return variableReferenceNode;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.INVOCATION;
    }

    @Override
    public String toString() {
        StringBuilder br = new StringBuilder();
        if (variableReferenceNode != null) {
            // Action invocation or lambda invocation.
            br.append(String.valueOf(variableReferenceNode));
        } else {
            if (packIdentifier != null && !packIdentifier.getValue().isEmpty()) {
                br.append(String.valueOf(packIdentifier)).append(":");
            }
            br.append(String.valueOf(functionName));
        }
        br.append("(");
        if (argsExpressions.size() > 0) {
            String s = Arrays.toString(argsExpressions.toArray());
            br.append(s.substring(1,s.length() -1 ));
        }
        br.append(")");
        return br.toString();
    }
}
