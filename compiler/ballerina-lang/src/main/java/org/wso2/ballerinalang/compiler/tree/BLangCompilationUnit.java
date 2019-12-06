/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.94
 */
public class BLangCompilationUnit extends BLangNode implements CompilationUnitNode {

    public String name;

    // Fields for caching.
    public int hash;
    public int length;

    public List<TopLevelNode> topLevelNodes;

    public BLangCompilationUnit() {
        this.topLevelNodes = new ArrayList<>();
    }

    @Override
    public void addTopLevelNode(TopLevelNode node) {
        this.topLevelNodes.add(node);
    }

    @Override
    public List<TopLevelNode> getTopLevelNodes() {
        return topLevelNodes;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.COMPILATION_UNIT;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("'" + this.getName() + "' -> Top Level Elements:-\n");
        this.getTopLevelNodes().stream().forEach(e -> builder.append("\t" + e + "\n"));
        return builder.toString();
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }
}
