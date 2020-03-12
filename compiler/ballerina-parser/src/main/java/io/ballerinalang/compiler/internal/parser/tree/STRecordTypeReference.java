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

public class STRecordTypeReference extends STNode {

    public final STNode asterisk;
    public final STNode type;
    public final STNode semicolonToken;

    public STRecordTypeReference(STNode asterisk,
                                  STNode type,
                                  STNode semicolonToken) {
        super(SyntaxKind.RECORD_TYPE_REFERENCE);
        this.asterisk = asterisk;
        this.type = type;
        this.semicolonToken = semicolonToken;

        this.bucketCount = 3;
        this.childBuckets = new STNode[this.bucketCount];
        this.addChildNode(asterisk, 0);
        this.addChildNode(type, 1);
        this.addChildNode(semicolonToken, 2);
    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {
        // TODO
        return null;
    }
}
