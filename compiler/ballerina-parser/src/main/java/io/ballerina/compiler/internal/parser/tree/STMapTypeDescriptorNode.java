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
package io.ballerina.compiler.internal.parser.tree;

import io.ballerina.compiler.syntax.tree.MapTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STMapTypeDescriptorNode extends STTypeDescriptorNode {
    public final STNode mapKeywordToken;
    public final STNode mapTypeParamsNode;

    STMapTypeDescriptorNode(
            STNode mapKeywordToken,
            STNode mapTypeParamsNode) {
        this(
                mapKeywordToken,
                mapTypeParamsNode,
                Collections.emptyList());
    }

    STMapTypeDescriptorNode(
            STNode mapKeywordToken,
            STNode mapTypeParamsNode,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.MAP_TYPE_DESC, diagnostics);
        this.mapKeywordToken = mapKeywordToken;
        this.mapTypeParamsNode = mapTypeParamsNode;

        addChildren(
                mapKeywordToken,
                mapTypeParamsNode);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STMapTypeDescriptorNode(
                this.mapKeywordToken,
                this.mapTypeParamsNode,
                diagnostics);
    }

    public STMapTypeDescriptorNode modify(
            STNode mapKeywordToken,
            STNode mapTypeParamsNode) {
        if (checkForReferenceEquality(
                mapKeywordToken,
                mapTypeParamsNode)) {
            return this;
        }

        return new STMapTypeDescriptorNode(
                mapKeywordToken,
                mapTypeParamsNode,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new MapTypeDescriptorNode(this, position, parent);
    }

    @Override
    public void accept(STNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(STNodeTransformer<T> transformer) {
        return transformer.transform(this);
    }
}
