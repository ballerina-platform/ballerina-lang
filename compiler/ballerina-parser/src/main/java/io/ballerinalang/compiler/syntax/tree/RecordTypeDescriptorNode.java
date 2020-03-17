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
package io.ballerinalang.compiler.syntax.tree;

import io.ballerinalang.compiler.internal.parser.tree.STNode;

public class RecordTypeDescriptorNode extends NonTerminalNode {

    private Token recordKeyword;
    private Token bodyStartDelimiter;
    private Node fields;
    private Token bodyEndDelimiter;

    public RecordTypeDescriptorNode(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }

    public Token recordKeyword() {
        if (recordKeyword != null) {
            return recordKeyword;
        }

        recordKeyword = createToken(0);
        return recordKeyword;
    }

    public Token bodyStartDelimiter() {
        if (bodyStartDelimiter != null) {
            return bodyStartDelimiter;
        }

        bodyStartDelimiter = createToken(1);
        return bodyStartDelimiter;
    }

    public Node fields() {
        if (fields != null) {
            return fields;
        }

        fields = createListNode(2);
        return fields;
    }

    public Token bodyEndDelimiter() {
        if (bodyEndDelimiter != null) {
            return bodyEndDelimiter;
        }

        bodyEndDelimiter = createToken(3);
        return bodyEndDelimiter;
    }

    public Node childInBucket(int bucket) {
        switch (bucket) {
            case 0:
                return recordKeyword();
            case 1:
                return bodyStartDelimiter();
            case 2:
                return fields();
            case 3:
                return bodyEndDelimiter();
        }
        return null;
    }
}
