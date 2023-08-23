/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.internal.parser.tree;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STRecordFieldNode extends STNode {
    public final STNode metadata;
    public final STNode readonlyKeyword;
    public final STNode typeName;
    public final STNode fieldName;
    public final STNode questionMarkToken;
    public final STNode semicolonToken;

    STRecordFieldNode(
            STNode metadata,
            STNode readonlyKeyword,
            STNode typeName,
            STNode fieldName,
            STNode questionMarkToken,
            STNode semicolonToken) {
        this(
                metadata,
                readonlyKeyword,
                typeName,
                fieldName,
                questionMarkToken,
                semicolonToken,
                Collections.emptyList());
    }

    STRecordFieldNode(
            STNode metadata,
            STNode readonlyKeyword,
            STNode typeName,
            STNode fieldName,
            STNode questionMarkToken,
            STNode semicolonToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.RECORD_FIELD, diagnostics);
        this.metadata = metadata;
        this.readonlyKeyword = readonlyKeyword;
        this.typeName = typeName;
        this.fieldName = fieldName;
        this.questionMarkToken = questionMarkToken;
        this.semicolonToken = semicolonToken;

        addChildren(
                metadata,
                readonlyKeyword,
                typeName,
                fieldName,
                questionMarkToken,
                semicolonToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STRecordFieldNode(
                this.metadata,
                this.readonlyKeyword,
                this.typeName,
                this.fieldName,
                this.questionMarkToken,
                this.semicolonToken,
                diagnostics);
    }

    public STRecordFieldNode modify(
            STNode metadata,
            STNode readonlyKeyword,
            STNode typeName,
            STNode fieldName,
            STNode questionMarkToken,
            STNode semicolonToken) {
        if (checkForReferenceEquality(
                metadata,
                readonlyKeyword,
                typeName,
                fieldName,
                questionMarkToken,
                semicolonToken)) {
            return this;
        }

        return new STRecordFieldNode(
                metadata,
                readonlyKeyword,
                typeName,
                fieldName,
                questionMarkToken,
                semicolonToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new RecordFieldNode(this, position, parent);
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
