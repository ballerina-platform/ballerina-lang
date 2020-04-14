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
import io.ballerinalang.compiler.syntax.tree.ObjectTypeDescriptor;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 1.3.0
 */
public class STObjectTypeDescriptor extends STNode {
    public final STNode objectTypeQualifiers;
    public final STNode objectKeyword;
    public final STNode openBrace;
    public final STNode members;
    public final STNode closeBrace;

    STObjectTypeDescriptor(
            STNode objectTypeQualifiers,
            STNode objectKeyword,
            STNode openBrace,
            STNode members,
            STNode closeBrace) {
        super(SyntaxKind.OBJECT_TYPE_DESCRIPTOR);
        this.objectTypeQualifiers = objectTypeQualifiers;
        this.objectKeyword = objectKeyword;
        this.openBrace = openBrace;
        this.members = members;
        this.closeBrace = closeBrace;

        addChildren(
                objectTypeQualifiers,
                objectKeyword,
                openBrace,
                members,
                closeBrace);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ObjectTypeDescriptor(this, position, parent);
    }
}
