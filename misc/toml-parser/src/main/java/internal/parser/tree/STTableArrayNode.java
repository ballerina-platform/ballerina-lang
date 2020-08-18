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
package internal.parser.tree;

import syntax.tree.Node;
import syntax.tree.NonTerminalNode;
import syntax.tree.SyntaxKind;
import syntax.tree.TableArrayNode;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STTableArrayNode extends STNode {
    public final STNode openBracket;
    public final STNode identifier;
    public final STNode closeBracket;

    STTableArrayNode(
            STNode openBracket,
            STNode identifier,
            STNode closeBracket) {
        this(
                openBracket,
                identifier,
                closeBracket,
                Collections.emptyList());
    }

    STTableArrayNode(
            STNode openBracket,
            STNode identifier,
            STNode closeBracket,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.TABLE_ARRAY, diagnostics);
        this.openBracket = openBracket;
        this.identifier = identifier;
        this.closeBracket = closeBracket;

        addChildren(
                openBracket,
                identifier,
                closeBracket);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STTableArrayNode(
                this.openBracket,
                this.identifier,
                this.closeBracket,
                diagnostics);
    }

    public STTableArrayNode modify(
            STNode openBracket,
            STNode identifier,
            STNode closeBracket) {
        if (checkForReferenceEquality(
                openBracket,
                identifier,
                closeBracket)) {
            return this;
        }

        return new STTableArrayNode(
                openBracket,
                identifier,
                closeBracket,
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
