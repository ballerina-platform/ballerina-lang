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
package generated.facade;


import generated.internal.SyntaxNode;

public class BLNodeList<T extends BLNode> extends BLNonTerminalNode {
    public BLNodeList(SyntaxNode node, int position, BLNonTerminalNode parent) {
        super(node, position, parent);
    }

    public T childInBucket(int bucket) {
        // TODO Fix warnings with generics
        T childNode = (T) super.childInBucket(bucket);
        if (childNode != null) {
            return childNode;
        }

        SyntaxNode internalChild = node.childInBucket(bucket);
        if (internalChild == null) {
            return null;
        }

        childNode = (T) internalChild.createFacade(getChildPosition(bucket), this);
        this.childBuckets[bucket] = childNode;
        return childNode;
    }

    public int size() {
        return this.node.bucketCount();
    }
}
