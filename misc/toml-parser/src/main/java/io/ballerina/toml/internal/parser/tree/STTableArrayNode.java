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

import io.ballerina.toml.syntax.tree.Node;
import io.ballerina.toml.syntax.tree.NonTerminalNode;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.toml.syntax.tree.TableArrayNode;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STTableArrayNode extends STDocumentMemberDeclarationNode {
    public final STNode firstOpenBracket;
    public final STNode secondOpenBracket;
    public final STNode identifier;
    public final STNode firstCloseBracket;
    public final STNode secondCloseBracket;
    public final STNode fields;

    STTableArrayNode(
            STNode firstOpenBracket,
            STNode secondOpenBracket,
            STNode identifier,
            STNode firstCloseBracket,
            STNode secondCloseBracket,
            STNode fields) {
        this(
                firstOpenBracket,
                secondOpenBracket,
                identifier,
                firstCloseBracket,
                secondCloseBracket,
                fields,
                Collections.emptyList());
    }

    STTableArrayNode(
            STNode firstOpenBracket,
            STNode secondOpenBracket,
            STNode identifier,
            STNode firstCloseBracket,
            STNode secondCloseBracket,
            STNode fields,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.TABLE_ARRAY, diagnostics);
        this.firstOpenBracket = firstOpenBracket;
        this.secondOpenBracket = secondOpenBracket;
        this.identifier = identifier;
        this.firstCloseBracket = firstCloseBracket;
        this.secondCloseBracket = secondCloseBracket;
        this.fields = fields;

        addChildren(
                firstOpenBracket,
                secondOpenBracket,
                identifier,
                firstCloseBracket,
                secondCloseBracket,
                fields);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STTableArrayNode(
                this.firstOpenBracket,
                this.secondOpenBracket,
                this.identifier,
                this.firstCloseBracket,
                this.secondCloseBracket,
                this.fields,
                diagnostics);
    }

    public STTableArrayNode modify(
            STNode firstOpenBracket,
            STNode secondOpenBracket,
            STNode identifier,
            STNode firstCloseBracket,
            STNode secondCloseBracket,
            STNode fields) {
        if (checkForReferenceEquality(
                firstOpenBracket,
                secondOpenBracket,
                identifier,
                firstCloseBracket,
                secondCloseBracket,
                fields)) {
            return this;
        }

        return new STTableArrayNode(
                firstOpenBracket,
                secondOpenBracket,
                identifier,
                firstCloseBracket,
                secondCloseBracket,
                fields,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new TableArrayNode(this, position, parent);
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
