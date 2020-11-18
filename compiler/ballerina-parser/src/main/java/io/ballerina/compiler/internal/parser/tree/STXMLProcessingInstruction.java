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
import io.ballerina.compiler.syntax.tree.XMLProcessingInstruction;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STXMLProcessingInstruction extends STXMLItemNode {
    public final STNode piStart;
    public final STNode target;
    public final STNode data;
    public final STNode piEnd;

    STXMLProcessingInstruction(
            STNode piStart,
            STNode target,
            STNode data,
            STNode piEnd) {
        this(
                piStart,
                target,
                data,
                piEnd,
                Collections.emptyList());
    }

    STXMLProcessingInstruction(
            STNode piStart,
            STNode target,
            STNode data,
            STNode piEnd,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.XML_PI, diagnostics);
        this.piStart = piStart;
        this.target = target;
        this.data = data;
        this.piEnd = piEnd;

        addChildren(
                piStart,
                target,
                data,
                piEnd);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STXMLProcessingInstruction(
                this.piStart,
                this.target,
                this.data,
                this.piEnd,
                diagnostics);
    }

    public STXMLProcessingInstruction modify(
            STNode piStart,
            STNode target,
            STNode data,
            STNode piEnd) {
        if (checkForReferenceEquality(
                piStart,
                target,
                data,
                piEnd)) {
            return this;
        }

        return new STXMLProcessingInstruction(
                piStart,
                target,
                data,
                piEnd,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new XMLProcessingInstruction(this, position, parent);
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
