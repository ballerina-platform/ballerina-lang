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
package io.ballerina.compiler.internal.parser.incremental;

import io.ballerina.compiler.internal.parser.tree.STNode;

/**
 * Supplies unmodified/reusable {@code STNode}s from the previous {@code SyntaxTree}.
 *
 * @since 1.3.0
 */
public class UnmodifiedSubtreeSupplier {
    private HybridNodeStorage hybridNodeStorage;

    public UnmodifiedSubtreeSupplier(HybridNodeStorage hybridNodeStorage) {
        this.hybridNodeStorage = hybridNodeStorage;
    }

    /**
     * Consumes and returns next subtree in the previous {@code SyntaxTree}.
     *
     * @return returns reusable {@code SyntaxTree}
     */
    public STNode consume() {
        return this.hybridNodeStorage.consumeSubtree().subtree();
    }

    /**
     * Returns the next subtree in the previous {@code SyntaxTree} without consuming it.
     *
     * @return reusable {@code SyntaxTree}
     */
    public STNode peek() {
        HybridNode hybridNode = this.hybridNodeStorage.peekSubtree();
        return hybridNode != null ? hybridNode.subtree() : null;
    }
}
