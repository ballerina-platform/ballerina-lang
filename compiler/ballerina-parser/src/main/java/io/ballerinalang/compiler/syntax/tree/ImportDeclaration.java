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
public class ImportDeclaration extends NonTerminalNode {

    private Token importKeyword;
    private Token orgName;
    private Node moduleName;
    private Node version;
    private Node prefix;
    private Token semicolon;

    public ImportDeclaration(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }

    public Token importKeyword() {
        if (importKeyword != null) {
            return importKeyword;
        }

        importKeyword = createToken(0);
        return importKeyword;
    }

    public Token orgName() {
        if (orgName != null) {
            return orgName;
        }

        orgName = createToken(1);
        return orgName;
    }

    public Node moduleName() {
        if (moduleName != null) {
            return moduleName;
        }

        moduleName = createListNode(2);
        return moduleName;
    }

    public Node version() {
        if (version != null) {
            return version;
        }

        version = node.childInBucket(3).createFacade(getChildPosition(3), this);
        childBuckets[3] = version;
        return version;
    }

    public Node prefix() {
        if (prefix != null) {
            return prefix;
        }

        prefix = node.childInBucket(4).createFacade(getChildPosition(4), this);
        childBuckets[4] = prefix;
        return this.prefix;
    }

    public Token semicolon() {
        if (semicolon != null) {
            return semicolon;
        }

        semicolon = createToken(5);
        return semicolon;
    }

    public Node childInBucket(int bucket) {
        switch (bucket) {
            case 0:
                return importKeyword();
            case 1:
                return orgName();
            case 2:
                return moduleName();
            case 3:
                return version();
            case 4:
                return prefix();
            case 5:
                return semicolon();
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
