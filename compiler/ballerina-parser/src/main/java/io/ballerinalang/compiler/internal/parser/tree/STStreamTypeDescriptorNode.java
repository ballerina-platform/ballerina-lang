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
package io.ballerinalang.compiler.internal.parser.tree;

import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.StreamTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 1.3.0
 */
public class STStreamTypeDescriptorNode extends STNode {
    public final STNode streamKeywordToken;
    public final STNode ltToken;
    public final STNode leftTypeDescNode;
    public final STNode commaToken;
    public final STNode rightTypeDescNode;
    public final STNode gtToken;

    STStreamTypeDescriptorNode(
            STNode streamKeywordToken,
            STNode ltToken,
            STNode leftTypeDescNode,
            STNode commaToken,
            STNode rightTypeDescNode,
            STNode gtToken) {
        super(SyntaxKind.STREAM_TYPE_DESC);
        this.streamKeywordToken = streamKeywordToken;
        this.ltToken = ltToken;
        this.leftTypeDescNode = leftTypeDescNode;
        this.commaToken = commaToken;
        this.rightTypeDescNode = rightTypeDescNode;
        this.gtToken = gtToken;

        addChildren(
                streamKeywordToken,
                ltToken,
                leftTypeDescNode,
                commaToken,
                rightTypeDescNode,
                gtToken);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new StreamTypeDescriptorNode(this, position, parent);
    }
}
