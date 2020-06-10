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
import io.ballerinalang.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STRecordFieldWithDefaultValueNode extends STNode {
    public final STNode metadata;
    public final STNode readonlyKeyword;
    public final STNode typeName;
    public final STNode fieldName;
    public final STNode equalsToken;
    public final STNode expression;
    public final STNode semicolonToken;

    STRecordFieldWithDefaultValueNode(
            STNode metadata,
            STNode readonlyKeyword,
            STNode typeName,
            STNode fieldName,
            STNode equalsToken,
            STNode expression,
            STNode semicolonToken) {
        this(
                metadata,
                readonlyKeyword,
                typeName,
                fieldName,
                equalsToken,
                expression,
                semicolonToken,
                Collections.emptyList());
    }

    STRecordFieldWithDefaultValueNode(
            STNode metadata,
            STNode readonlyKeyword,
            STNode typeName,
            STNode fieldName,
            STNode equalsToken,
            STNode expression,
            STNode semicolonToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.RECORD_FIELD_WITH_DEFAULT_VALUE, diagnostics);
        this.metadata = metadata;
        this.readonlyKeyword = readonlyKeyword;
        this.typeName = typeName;
        this.fieldName = fieldName;
        this.equalsToken = equalsToken;
        this.expression = expression;
        this.semicolonToken = semicolonToken;

        addChildren(
                metadata,
                readonlyKeyword,
                typeName,
                fieldName,
                equalsToken,
                expression,
                semicolonToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STRecordFieldWithDefaultValueNode(
                this.metadata,
                this.readonlyKeyword,
                this.typeName,
                this.fieldName,
                this.equalsToken,
                this.expression,
                this.semicolonToken,
                diagnostics);
    }

    public STRecordFieldWithDefaultValueNode modify(
            STNode metadata,
            STNode readonlyKeyword,
            STNode typeName,
            STNode fieldName,
            STNode equalsToken,
            STNode expression,
            STNode semicolonToken) {
        if (checkForReferenceEquality(
                metadata,
                readonlyKeyword,
                typeName,
                fieldName,
                equalsToken,
                expression,
                semicolonToken)) {
            return this;
        }

        return new STRecordFieldWithDefaultValueNode(
                metadata,
                readonlyKeyword,
                typeName,
                fieldName,
                equalsToken,
                expression,
                semicolonToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new RecordFieldWithDefaultValueNode(this, position, parent);
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
