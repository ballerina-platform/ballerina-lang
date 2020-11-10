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
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STRecordTypeDescriptorNode extends STTypeDescriptorNode {
    public final STNode recordKeyword;
    public final STNode bodyStartDelimiter;
    public final STNode fields;
    public final STNode recordRestDescriptor;
    public final STNode bodyEndDelimiter;

    STRecordTypeDescriptorNode(
            STNode recordKeyword,
            STNode bodyStartDelimiter,
            STNode fields,
            STNode recordRestDescriptor,
            STNode bodyEndDelimiter) {
        this(
                recordKeyword,
                bodyStartDelimiter,
                fields,
                recordRestDescriptor,
                bodyEndDelimiter,
                Collections.emptyList());
    }

    STRecordTypeDescriptorNode(
            STNode recordKeyword,
            STNode bodyStartDelimiter,
            STNode fields,
            STNode recordRestDescriptor,
            STNode bodyEndDelimiter,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.RECORD_TYPE_DESC, diagnostics);
        this.recordKeyword = recordKeyword;
        this.bodyStartDelimiter = bodyStartDelimiter;
        this.fields = fields;
        this.recordRestDescriptor = recordRestDescriptor;
        this.bodyEndDelimiter = bodyEndDelimiter;

        addChildren(
                recordKeyword,
                bodyStartDelimiter,
                fields,
                recordRestDescriptor,
                bodyEndDelimiter);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STRecordTypeDescriptorNode(
                this.recordKeyword,
                this.bodyStartDelimiter,
                this.fields,
                this.recordRestDescriptor,
                this.bodyEndDelimiter,
                diagnostics);
    }

    public STRecordTypeDescriptorNode modify(
            STNode recordKeyword,
            STNode bodyStartDelimiter,
            STNode fields,
            STNode recordRestDescriptor,
            STNode bodyEndDelimiter) {
        if (checkForReferenceEquality(
                recordKeyword,
                bodyStartDelimiter,
                fields,
                recordRestDescriptor,
                bodyEndDelimiter)) {
            return this;
        }

        return new STRecordTypeDescriptorNode(
                recordKeyword,
                bodyStartDelimiter,
                fields,
                recordRestDescriptor,
                bodyEndDelimiter,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new RecordTypeDescriptorNode(this, position, parent);
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
