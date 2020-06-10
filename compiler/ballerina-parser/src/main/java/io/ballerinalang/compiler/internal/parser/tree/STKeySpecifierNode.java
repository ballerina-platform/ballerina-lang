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

import io.ballerinalang.compiler.syntax.tree.KeySpecifierNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STKeySpecifierNode extends STNode {
    public final STNode keyKeyword;
    public final STNode openParenToken;
    public final STNode fieldNames;
    public final STNode closeParenToken;

    STKeySpecifierNode(
            STNode keyKeyword,
            STNode openParenToken,
            STNode fieldNames,
            STNode closeParenToken) {
        this(
                keyKeyword,
                openParenToken,
                fieldNames,
                closeParenToken,
                Collections.emptyList());
    }

    STKeySpecifierNode(
            STNode keyKeyword,
            STNode openParenToken,
            STNode fieldNames,
            STNode closeParenToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.KEY_SPECIFIER, diagnostics);
        this.keyKeyword = keyKeyword;
        this.openParenToken = openParenToken;
        this.fieldNames = fieldNames;
        this.closeParenToken = closeParenToken;

        addChildren(
                keyKeyword,
                openParenToken,
                fieldNames,
                closeParenToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STKeySpecifierNode(
                this.keyKeyword,
                this.openParenToken,
                this.fieldNames,
                this.closeParenToken,
                diagnostics);
    }

    public STKeySpecifierNode modify(
            STNode keyKeyword,
            STNode openParenToken,
            STNode fieldNames,
            STNode closeParenToken) {
        if (checkForReferenceEquality(
                keyKeyword,
                openParenToken,
                fieldNames,
                closeParenToken)) {
            return this;
        }

        return new STKeySpecifierNode(
                keyKeyword,
                openParenToken,
                fieldNames,
                closeParenToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new KeySpecifierNode(this, position, parent);
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
