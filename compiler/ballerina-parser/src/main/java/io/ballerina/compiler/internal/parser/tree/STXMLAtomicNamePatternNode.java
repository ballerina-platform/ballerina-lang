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
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.XMLAtomicNamePatternNode;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STXMLAtomicNamePatternNode extends STNode {
    public final STNode prefix;
    public final STNode colon;
    public final STNode name;

    STXMLAtomicNamePatternNode(
            STNode prefix,
            STNode colon,
            STNode name) {
        this(
                prefix,
                colon,
                name,
                Collections.emptyList());
    }

    STXMLAtomicNamePatternNode(
            STNode prefix,
            STNode colon,
            STNode name,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.XML_ATOMIC_NAME_PATTERN, diagnostics);
        this.prefix = prefix;
        this.colon = colon;
        this.name = name;

        addChildren(
                prefix,
                colon,
                name);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STXMLAtomicNamePatternNode(
                this.prefix,
                this.colon,
                this.name,
                diagnostics);
    }

    public STXMLAtomicNamePatternNode modify(
            STNode prefix,
            STNode colon,
            STNode name) {
        if (checkForReferenceEquality(
                prefix,
                colon,
                name)) {
            return this;
        }

        return new STXMLAtomicNamePatternNode(
                prefix,
                colon,
                name,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new XMLAtomicNamePatternNode(this, position, parent);
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
