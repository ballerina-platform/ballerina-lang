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

import io.ballerinalang.compiler.syntax.tree.ImportSubVersionNode;
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
public class STImportSubVersionNode extends STNode {
    public final STNode leadingDot;
    public final STNode versionNumber;

    STImportSubVersionNode(
            STNode leadingDot,
            STNode versionNumber) {
        this(
                leadingDot,
                versionNumber,
                Collections.emptyList());
    }

    STImportSubVersionNode(
            STNode leadingDot,
            STNode versionNumber,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.IMPORT_SUB_VERSION, diagnostics);
        this.leadingDot = leadingDot;
        this.versionNumber = versionNumber;

        addChildren(
                leadingDot,
                versionNumber);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STImportSubVersionNode(
                this.leadingDot,
                this.versionNumber,
                diagnostics);
    }

    public STImportSubVersionNode modify(
            STNode leadingDot,
            STNode versionNumber) {
        if (checkForReferenceEquality(
                leadingDot,
                versionNumber)) {
            return this;
        }

        return new STImportSubVersionNode(
                leadingDot,
                versionNumber,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ImportSubVersionNode(this, position, parent);
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
