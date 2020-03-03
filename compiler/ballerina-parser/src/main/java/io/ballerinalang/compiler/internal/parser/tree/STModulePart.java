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

import io.ballerinalang.compiler.syntax.tree.ModulePart;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;

public class STModulePart extends STNode {
    public final STNode imports;
    public final STNode members;
    public final STNode eofToken;

    public STModulePart(STNode imports, STNode members, STNode eofToken) {
        super(SyntaxKind.MODULE_PART);
        this.imports = imports;
        this.members = members;
        this.eofToken = eofToken;

        this.bucketCount = 3;
        this.childBuckets = new STNode[3];
        this.addChildNode(imports, 0);
        this.addChildNode(members, 1);
        this.addChildNode(eofToken, 2);
    }

    public NonTerminalNode createFacade(int position, NonTerminalNode parent) {
        return new ModulePart(this, position, parent);
    }
}
