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
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.XMLNamePatternChainingNode;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STXMLNamePatternChainingNode extends STNode {
    public final STNode startToken;
    public final STNode xmlNamePattern;
    public final STNode gtToken;

    STXMLNamePatternChainingNode(
            STNode startToken,
            STNode xmlNamePattern,
            STNode gtToken) {
        this(
                startToken,
                xmlNamePattern,
                gtToken,
                Collections.emptyList());
    }

    STXMLNamePatternChainingNode(
            STNode startToken,
            STNode xmlNamePattern,
            STNode gtToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.XML_NAME_PATTERN_CHAIN, diagnostics);
        this.startToken = startToken;
        this.xmlNamePattern = xmlNamePattern;
        this.gtToken = gtToken;

        addChildren(
                startToken,
                xmlNamePattern,
                gtToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STXMLNamePatternChainingNode(
                this.startToken,
                this.xmlNamePattern,
                this.gtToken,
                diagnostics);
    }

    public STXMLNamePatternChainingNode modify(
            STNode startToken,
            STNode xmlNamePattern,
            STNode gtToken) {
        if (checkForReferenceEquality(
                startToken,
                xmlNamePattern,
                gtToken)) {
            return this;
        }

        return new STXMLNamePatternChainingNode(
                startToken,
                xmlNamePattern,
                gtToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new XMLNamePatternChainingNode(this, position, parent);
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
