/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.tree.clauses;

import org.ballerinalang.model.clauses.GroupByClauseNode;
import org.ballerinalang.model.clauses.GroupingKeyNode;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Implementation of "group by" clause statement.
 *
 * @since 2201.3.0
 */
public class BLangGroupByClause extends BLangNode implements GroupByClauseNode {

    // BLangNodes
    public List<GroupingKeyNode> groupingKeyList = new ArrayList<>();

    // Semantic Data
    public SymbolEnv env;

    public BLangGroupByClause() {
    }

    @Override
    public void addGroupingKey(GroupingKeyNode groupingKey) {
        groupingKeyList.add(groupingKey);
    }

    @Override
    public List<GroupingKeyNode> getGroupingKeyList() {
        return groupingKeyList;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.GROUP_BY;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
        analyzer.visit(this, props);
    }

    @Override
    public <T, R> R apply(BLangNodeTransformer<T, R> modifier, T props) {
        return modifier.transform(this, props);
    }

    @Override
    public String toString() {
        StringJoiner declarations = new StringJoiner(", ");
        for (GroupingKeyNode groupingKey : groupingKeyList) {
            declarations.add(groupingKey.toString());
        }
        return "group by " + declarations.toString();
    }
}
