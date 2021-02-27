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

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.OnFailClauseNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STOnFailClauseNode extends STClauseNode {
    public final STNode onKeyword;
    public final STNode failKeyword;
    public final STNode typeDescriptor;
    public final STNode failErrorName;
    public final STNode blockStatement;

    STOnFailClauseNode(
            STNode onKeyword,
            STNode failKeyword,
            STNode typeDescriptor,
            STNode failErrorName,
            STNode blockStatement) {
        this(
                onKeyword,
                failKeyword,
                typeDescriptor,
                failErrorName,
                blockStatement,
                Collections.emptyList());
    }

    STOnFailClauseNode(
            STNode onKeyword,
            STNode failKeyword,
            STNode typeDescriptor,
            STNode failErrorName,
            STNode blockStatement,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.ON_FAIL_CLAUSE, diagnostics);
        this.onKeyword = onKeyword;
        this.failKeyword = failKeyword;
        this.typeDescriptor = typeDescriptor;
        this.failErrorName = failErrorName;
        this.blockStatement = blockStatement;

        addChildren(
                onKeyword,
                failKeyword,
                typeDescriptor,
                failErrorName,
                blockStatement);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STOnFailClauseNode(
                this.onKeyword,
                this.failKeyword,
                this.typeDescriptor,
                this.failErrorName,
                this.blockStatement,
                diagnostics);
    }

    public STOnFailClauseNode modify(
            STNode onKeyword,
            STNode failKeyword,
            STNode typeDescriptor,
            STNode failErrorName,
            STNode blockStatement) {
        if (checkForReferenceEquality(
                onKeyword,
                failKeyword,
                typeDescriptor,
                failErrorName,
                blockStatement)) {
            return this;
        }

        return new STOnFailClauseNode(
                onKeyword,
                failKeyword,
                typeDescriptor,
                failErrorName,
                blockStatement,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new OnFailClauseNode(this, position, parent);
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
