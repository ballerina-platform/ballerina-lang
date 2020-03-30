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

/**
 * 
 * @since 1.3.0
 */
public class STImportDeclaration extends STNode {

    public final STNode importKeyword;
    public final STNode orgName;
    public final STNode moduleName;
    public final STNode version;
    public final STNode alias;
    public final STNode semicolon;

    STImportDeclaration(STNode importKeyword,
                        STNode orgName,
                        STNode moduleName,
                        STNode version,
                        STNode alias,
                        STNode semicolon) {

        super(SyntaxKind.IMPORT_DECLARATION);
        this.importKeyword = importKeyword;
        this.orgName = orgName;
        this.moduleName = moduleName;
        this.version = version;
        this.alias = alias;
        this.semicolon = semicolon;

        this.bucketCount = 6;
        this.childBuckets = new STNode[this.bucketCount];
        this.addChildNode(importKeyword, 0);
        this.addChildNode(orgName, 1);
        this.addChildNode(moduleName, 2);
        this.addChildNode(version, 3);
        this.addChildNode(alias, 4);
        this.addChildNode(semicolon, 5);
    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {
        return null;
    }
}
