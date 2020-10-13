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

import io.ballerina.toml.syntax.tree.SyntaxKind;

/**
 * This class represents a minutiae node which contains an invalid syntax node.
 *
 * @since 2.0.0
 */
public class STInvalidNodeMinutiae extends STMinutiae {
    private final STNode invalidNode;

    STInvalidNodeMinutiae(STNode invalidNode) {
        super(SyntaxKind.INVALID_NODE_MINUTIAE, invalidNode.toSourceCode());
        this.invalidNode = invalidNode;
    }

    public STNode invalidNode() {
        return invalidNode;
    }

    @Override
    public void writeTo(StringBuilder builder) {
        invalidNode.writeTo(builder);
    }
}
