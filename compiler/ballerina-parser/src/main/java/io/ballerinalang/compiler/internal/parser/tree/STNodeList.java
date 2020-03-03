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
import io.ballerinalang.compiler.syntax.tree.NodeList;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class STNodeList extends STNode {

    public final List<STNode> children;
    // TODO is unmodifiable list slow??????
    public static final STNode emptyNodeList = new STNodeList(Collections.unmodifiableList(new ArrayList<>(0)));

    public STNodeList(List<STNode> children) {
        super(SyntaxKind.LIST);
        this.children = Collections.unmodifiableList(children);

        this.bucketCount = this.children.size();
        this.childBuckets = new STNode[bucketCount];
        for (int i = 0; i < children.size(); i++) {
            this.addChildNode(children.get(i), i);
        }
    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {
        return new NodeList<>(this, position, parent);
    }
}
