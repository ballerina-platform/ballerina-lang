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
import io.ballerina.compiler.syntax.tree.ReUnicodeGeneralCategoryNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2201.3.0
 */
public class STReUnicodeGeneralCategoryNode extends STReUnicodePropertyNode {
    public final STNode categoryStart;
    public final STNode reUnicodeGeneralCategoryName;

    STReUnicodeGeneralCategoryNode(
            STNode categoryStart,
            STNode reUnicodeGeneralCategoryName) {
        this(
                categoryStart,
                reUnicodeGeneralCategoryName,
                Collections.emptyList());
    }

    STReUnicodeGeneralCategoryNode(
            STNode categoryStart,
            STNode reUnicodeGeneralCategoryName,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.RE_UNICODE_GENERAL_CATEGORY, diagnostics);
        this.categoryStart = categoryStart;
        this.reUnicodeGeneralCategoryName = reUnicodeGeneralCategoryName;

        addChildren(
                categoryStart,
                reUnicodeGeneralCategoryName);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STReUnicodeGeneralCategoryNode(
                this.categoryStart,
                this.reUnicodeGeneralCategoryName,
                diagnostics);
    }

    public STReUnicodeGeneralCategoryNode modify(
            STNode categoryStart,
            STNode reUnicodeGeneralCategoryName) {
        if (checkForReferenceEquality(
                categoryStart,
                reUnicodeGeneralCategoryName)) {
            return this;
        }

        return new STReUnicodeGeneralCategoryNode(
                categoryStart,
                reUnicodeGeneralCategoryName,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ReUnicodeGeneralCategoryNode(this, position, parent);
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
