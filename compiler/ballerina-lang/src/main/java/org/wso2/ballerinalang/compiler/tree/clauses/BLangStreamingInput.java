/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.tree.clauses;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.clauses.StreamingInput;
import org.ballerinalang.model.tree.clauses.WhereNode;
import org.ballerinalang.model.tree.clauses.WindowClauseNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.965.0
 *
 * Implementation of {@link StreamingInput}.
 */
public class BLangStreamingInput extends BLangNode implements StreamingInput {

    private List<WhereNode> streamingConditions = new ArrayList<>();
    private WindowClauseNode windowClause;
    private ExpressionNode tableReference;
    private String alias;
    @Override
    public void addStreamingCondition(WhereNode where) {
        this.streamingConditions.add(where);
    }

    @Override
    public List<? extends WhereNode> getStreamingConditions() {
        return this.streamingConditions;
    }

    @Override
    public void setWindowClause(WindowClauseNode windowClause) {
        this.windowClause = windowClause;
    }

    @Override
    public WindowClauseNode getWindowClause() {
        return this.windowClause;
    }

    public ExpressionNode getTableReference() {
        return tableReference;
    }

    public void setTableReference(ExpressionNode tableReference) {
        this.tableReference = tableReference;
    }

    @Override
    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public String getAlias() {
        return this.alias;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.STREAMING_INPUT;
    }
}
