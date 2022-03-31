/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.ballerinalang.semver.checker.diff;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.Token;

import java.util.List;

import static org.ballerinalang.semver.checker.util.Constants.QUALIFIER_PUBLIC;

public class FunctionDiff extends NodeDiff<FunctionDefinitionNode> {

    public FunctionDiff(FunctionDefinitionNode newNode, FunctionDefinitionNode oldNode) {
        super(newNode, oldNode);
    }

    @Override
    public CompatibilityLevel getCompatibilityLevel() {
        CompatibilityLevel compatibilityLevel = super.getCompatibilityLevel();
        if (isPublic()) {
            compatibilityLevel = CompatibilityLevel.PATCH;
        }

        return compatibilityLevel;
    }

    private boolean isPublic() {
        NodeList<Token> functionQualifiers = newNode.qualifierList();
        return functionQualifiers.stream().anyMatch(qualifier -> qualifier.text().trim().equals(QUALIFIER_PUBLIC));
    }

    public void addChildDiff(NodeDiff<Node> childDiff) {
        this.childDiffs.add(childDiff);
    }

    public void addChildDiffs(List<NodeDiff<Node>> childDiffs) {
        this.childDiffs.addAll(childDiffs);
    }
}
