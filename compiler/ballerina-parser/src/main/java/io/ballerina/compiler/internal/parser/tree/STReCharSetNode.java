/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.compiler.syntax.tree.ReCharSetNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2201.3.0
 */
public class STReCharSetNode extends STNode {
    public final STNode reCharSet;

    STReCharSetNode(
            STNode reCharSet) {
        this(
                reCharSet,
                Collections.emptyList());
    }

    STReCharSetNode(
            STNode reCharSet,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.RE_CHAR_SET, diagnostics);
        this.reCharSet = reCharSet;

        addChildren(
                reCharSet);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STReCharSetNode(
                this.reCharSet,
                diagnostics);
    }

    public STReCharSetNode modify(
            STNode reCharSet) {
        if (checkForReferenceEquality(
                reCharSet)) {
            return this;
        }

        return new STReCharSetNode(
                reCharSet,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ReCharSetNode(this, position, parent);
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
