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

/**
 * @since 1.3.0
 */
public class ImportPrefix extends NonTerminalNode {

    private Token asKeyword;
    private Token prefix;

    public ImportPrefix(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }

    public Token asKeyword() {
        if (asKeyword != null) {
            return asKeyword;
        }

        asKeyword = createToken(0);
        return asKeyword;
    }

    public Token prefix() {
        if (prefix != null) {
            return prefix;
        }

        prefix = createToken(1);
        return prefix;
    }

    public Node childInBucket(int bucket) {
        switch (bucket) {
            case 0:
                return asKeyword();
            case 1:
                return prefix();
        }
        return null;
    }

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(SyntaxNodeTransformer<T> visitor) {
        return visitor.transform(this);
    }
}
