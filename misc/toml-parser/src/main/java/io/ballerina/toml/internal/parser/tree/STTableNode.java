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
import io.ballerina.toml.syntax.tree.TableNode;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STTableNode extends STDocumentMemberDeclarationNode {
    public final STNode openBracket;
    public final STNode identifier;
    public final STNode closeBracket;
    public final STNode fields;

    STTableNode(
            STNode openBracket,
            STNode identifier,
            STNode closeBracket,
            STNode fields) {
        this(
                openBracket,
                identifier,
                closeBracket,
                fields,
                Collections.emptyList());
    }

    STTableNode(
            STNode openBracket,
            STNode identifier,
            STNode closeBracket,
            STNode fields,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.TABLE, diagnostics);
        this.openBracket = openBracket;
        this.identifier = identifier;
        this.closeBracket = closeBracket;
        this.fields = fields;

        addChildren(
                openBracket,
                identifier,
                closeBracket,
                fields);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STTableNode(
                this.openBracket,
                this.identifier,
                this.closeBracket,
                this.fields,
                diagnostics);
    }

    public STTableNode modify(
            STNode openBracket,
            STNode identifier,
            STNode closeBracket,
            STNode fields) {
        if (checkForReferenceEquality(
                openBracket,
                identifier,
                closeBracket,
                fields)) {
            return this;
        }

        return new STTableNode(
                openBracket,
                identifier,
                closeBracket,
                fields,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new TableNode(this, position, parent);
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
