/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.toml.internal.parser.tree;

/**
 * Replaces internal tree nodes with the given replacements.
 * <p>
 * This class implements the {@code STTreeModifier} to visit the tree. It descends to a
 * subtree only if the target node is there in the subtree. The transformation happens
 * during the ascend.
 *
 * @since 2.0.0
 */
class STNodeReplacer extends STTreeModifier {
    private final STNode target;
    private final STNode replacement;

    STNodeReplacer(STNode target, STNode replacement) {
        this.target = target;
        this.replacement = replacement;
    }

    <T extends STNode> T replace(T root) {
        return modifyNode(root);
    }

    private boolean shouldDescend(STNode node) {
        // TODO Can we check whether to descend?
        return true;
    }

    protected <T extends STNode> T modifyNode(T node) {
        if (node == null) {
            return null;
        }

        STNode replaced = node;
        if (node == target) {
            replaced = replacement;
        } else if (shouldDescend(node)) {
            replaced = node.apply(this);
        }
        return (T) replaced;
    }
}
