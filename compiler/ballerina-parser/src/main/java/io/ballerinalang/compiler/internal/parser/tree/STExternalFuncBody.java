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

import io.ballerinalang.compiler.syntax.tree.BlockStatement;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;

public class STExternalFuncBody extends STStatement {

    public final STNode assign;
    public final STNode annotation;
    public final STNode externalKeyword;
    public final STNode semicolon;

    public STExternalFuncBody(SyntaxKind kind,
                              STNode assign,
                              STNode annotation,
                              STNode externalKeyword,
                              STNode semicolon) {
        super(kind);
        this.assign = assign;
        this.annotation = annotation;
        this.externalKeyword = externalKeyword;
        this.semicolon = semicolon;

        this.bucketCount = 4;
        this.childBuckets = new STNode[this.bucketCount];
        this.addChildNode(assign, 0);
        this.addChildNode(annotation, 1);
        this.addChildNode(externalKeyword, 2);
        this.addChildNode(semicolon, 3);
    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {
        return new BlockStatement(this, position, parent);
    }
}
