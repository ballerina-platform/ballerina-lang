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

package io.ballerina.semver.checker.diff;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;

import static io.ballerina.semver.checker.util.Constants.QUALIFIER_PUBLIC;

public class FunctionDiff extends NodeDiff<FunctionDefinitionNode> {

    public FunctionDiff(FunctionDefinitionNode newNode, FunctionDefinitionNode oldNode) {
        super(newNode, oldNode);
    }

    @Override
    public CompatibilityLevel getCompatibilityLevel() {
        CompatibilityLevel compatibilityLevel = super.getCompatibilityLevel();
        // checks if both the old and current versions of function definition is not public and if so, all the
        // sub-level incompatibilities can be discarded.
        if (isPrivateFunction()) {
            compatibilityLevel = CompatibilityLevel.PATCH;
        }

        return compatibilityLevel;
    }

    private boolean isPrivateFunction() {
        return newNode != null && oldNode != null
                && newNode.qualifierList().stream().noneMatch(qualifier -> qualifier.text().equals(QUALIFIER_PUBLIC))
                && oldNode.qualifierList().stream().noneMatch(qualifier -> qualifier.text().equals(QUALIFIER_PUBLIC));
    }
}
