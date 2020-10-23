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

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STModulePartNode extends STNode {
    public final STNode imports;
    public final STNode members;
    public final STNode eofToken;

    STModulePartNode(
            STNode imports,
            STNode members,
            STNode eofToken) {
        this(
                imports,
                members,
                eofToken,
                Collections.emptyList());
    }

    STModulePartNode(
            STNode imports,
            STNode members,
            STNode eofToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.MODULE_PART, diagnostics);
        this.imports = imports;
        this.members = members;
        this.eofToken = eofToken;

        addChildren(
                imports,
                members,
                eofToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STModulePartNode(
                this.imports,
                this.members,
                this.eofToken,
                diagnostics);
    }

    public STModulePartNode modify(
            STNode imports,
            STNode members,
            STNode eofToken) {
        if (checkForReferenceEquality(
                imports,
                members,
                eofToken)) {
            return this;
        }

        return new STModulePartNode(
                imports,
                members,
                eofToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ModulePartNode(this, position, parent);
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
