/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.model.clauses.OnClauseNode;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * Implementation of "join" clause statement.
 *
 * @since 1.3.0
 */
public class BLangJoinClause extends BLangInputClause {

    public boolean isOuterJoin;
    public OnClauseNode onClause;
    public SymbolEnv env;

    public void setOuterJoin(boolean outerJoin) {
        isOuterJoin = outerJoin;
    }

    public OnClauseNode getOnClause() {
        return onClause;
    }

    public void setOnClause(OnClauseNode onClause) {
        this.onClause = onClause;
    }

    public boolean isOuterJoin() {
        return isOuterJoin;
    }

    public void setIsOuterJoin(boolean isOuterJoin) {
        this.isOuterJoin = isOuterJoin;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.JOIN;
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
    public <T, R> R accept(BLangNodeTransformer<T, R> transformer, T props) {
        return transformer.transform(this, props);
    }

    @Override
    public String toString() {
        if (isOuterJoin()) {
            return "outer join " + variableDefinitionNode + " in " + collection;
        }
        return "join " + variableDefinitionNode + " in " + collection;
    }
}
