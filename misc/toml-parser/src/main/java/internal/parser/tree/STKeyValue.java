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

import syntax.tree.KeyValue;
import syntax.tree.Node;
import syntax.tree.NonTerminalNode;
import syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STKeyValue extends STModuleMemberDeclarationNode {
    public final STNode identifier;
    public final STNode assign;
    public final STNode value;

    STKeyValue(
            STNode identifier,
            STNode assign,
            STNode value) {
        this(
                identifier,
                assign,
                value,
                Collections.emptyList());
    }

    STKeyValue(
            STNode identifier,
            STNode assign,
            STNode value,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.KEY_VALUE, diagnostics);
        this.identifier = identifier;
        this.assign = assign;
        this.value = value;

        addChildren(
                identifier,
                assign,
                value);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STKeyValue(
                this.identifier,
                this.assign,
                this.value,
                diagnostics);
    }

    public STKeyValue modify(
            STNode identifier,
            STNode assign,
            STNode value) {
        if (checkForReferenceEquality(
                identifier,
                assign,
                value)) {
            return this;
        }

        return new STKeyValue(
                identifier,
                assign,
                value,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new KeyValue(this, position, parent);
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
