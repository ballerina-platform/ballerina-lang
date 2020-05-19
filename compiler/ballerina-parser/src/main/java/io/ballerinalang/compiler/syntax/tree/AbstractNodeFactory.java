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
package io.ballerinalang.compiler.syntax.tree;

import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeFactory;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.internal.parser.tree.SyntaxUtils;

/**
 * A factory for creating nodes in the syntax tree.
 *
 * @since 1.3.0
 */
public abstract class AbstractNodeFactory {

    public static IdentifierToken createIdentifierToken(String text) {
        STToken token = STNodeFactory.createIdentifierToken(text, STNodeFactory.createNodeList(),
                STNodeFactory.createNodeList());
        return token.createUnlinkedFacade();
    }

    public static IdentifierToken createIdentifierToken(String text,
                                                        MinutiaeList leadingMinutiae,
                                                        MinutiaeList trailingMinutiae) {
        STNode leadingMinutiaeSTNode = leadingMinutiae.internalNode();
        if (!SyntaxUtils.isSTNodeList(leadingMinutiaeSTNode)) {
            leadingMinutiaeSTNode = STNodeFactory.createNodeList(leadingMinutiaeSTNode);
        }

        STNode trailingMinutiaeSTNode = trailingMinutiae.internalNode();
        if (!SyntaxUtils.isSTNodeList(trailingMinutiaeSTNode)) {
            trailingMinutiaeSTNode = STNodeFactory.createNodeList(trailingMinutiaeSTNode);
        }

        STToken token = STNodeFactory.createIdentifierToken(text, leadingMinutiaeSTNode,
                trailingMinutiaeSTNode);
        return token.createUnlinkedFacade();
    }

    public static Token createToken(SyntaxKind kind,
                                    MinutiaeList leadingMinutiae,
                                    MinutiaeList trailingMinutiae) {
        STNode leadingMinutiaeSTNode = leadingMinutiae.internalNode();
        if (!SyntaxUtils.isSTNodeList(leadingMinutiaeSTNode)) {
            leadingMinutiaeSTNode = STNodeFactory.createNodeList(leadingMinutiaeSTNode);
        }

        STNode trailingMinutiaeSTNode = trailingMinutiae.internalNode();
        if (!SyntaxUtils.isSTNodeList(trailingMinutiaeSTNode)) {
            trailingMinutiaeSTNode = STNodeFactory.createNodeList(trailingMinutiaeSTNode);
        }

        STToken token = STNodeFactory.createToken(kind, leadingMinutiaeSTNode,
                trailingMinutiaeSTNode);
        return token.createUnlinkedFacade();
    }

    protected static STNode getOptionalSTNode(Node node) {
        return node != null ? node.internalNode() : null;
    }
}
