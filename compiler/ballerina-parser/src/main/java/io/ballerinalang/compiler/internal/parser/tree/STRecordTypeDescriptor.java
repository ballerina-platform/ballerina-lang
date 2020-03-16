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
import io.ballerinalang.compiler.syntax.tree.RecordTypeDescriptorNode;

public class STRecordTypeDescriptor extends STNode {

    public final STNode recordKeyword;
    public final STNode bodyStartDelimiter;
    public final STNode fields;
    public final STNode bodyEndDelimiter;

    public STRecordTypeDescriptor(STNode recordKeyword,
                                  STNode bodyStartDelimiter,
                                  STNode fields,
                                  STNode bodyEndDelimiter) {
        super(SyntaxKind.RECORD_TYPE_DESCRIPTOR);
        this.recordKeyword = recordKeyword;
        this.bodyStartDelimiter = bodyStartDelimiter;
        this.fields = fields;
        this.bodyEndDelimiter = bodyEndDelimiter;

        this.bucketCount = 4;
        this.childBuckets = new STNode[this.bucketCount];
        this.addChildNode(recordKeyword, 0);
        this.addChildNode(bodyStartDelimiter, 1);
        this.addChildNode(fields, 2);
        this.addChildNode(bodyEndDelimiter, 3);
    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {
        return new RecordTypeDescriptorNode(this, position, parent);
    }
}
