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

import io.ballerinalang.compiler.syntax.tree.ImportOrgNameNode;
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
public class STImportOrgNameNode extends STNode {
    public final STNode orgName;
    public final STNode slashToken;

    STImportOrgNameNode(
            STNode orgName,
            STNode slashToken) {
        this(
                orgName,
                slashToken,
                Collections.emptyList());
    }

    STImportOrgNameNode(
            STNode orgName,
            STNode slashToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.IMPORT_ORG_NAME, diagnostics);
        this.orgName = orgName;
        this.slashToken = slashToken;

        addChildren(
                orgName,
                slashToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STImportOrgNameNode(
                this.orgName,
                this.slashToken,
                diagnostics);
    }

    public STImportOrgNameNode modify(
            STNode orgName,
            STNode slashToken) {
        if (checkForReferenceEquality(
                orgName,
                slashToken)) {
            return this;
        }

        return new STImportOrgNameNode(
                orgName,
                slashToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ImportOrgNameNode(this, position, parent);
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
