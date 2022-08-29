/*
*  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree.clauses;

import org.ballerinalang.model.clauses.GroupingKeyNode;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.tree.*;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;

/**
 * Implementation of grouping key specified in "group by" clause statement.
 *
 * @since 2201.3.0
 */
public class BLangGroupingKey extends BLangNode implements GroupingKeyNode {

    // BLangNodes
    public BLangSimpleVariableDef variableDef;
    public BLangSimpleVarRef variableRef;

    @Override
    public void setGroupingKey(Node groupingKey) {
        if (groupingKey.getKind() == NodeKind.VARIABLE_DEF) {
            this.variableDef = (BLangSimpleVariableDef) groupingKey;
        }
        this.variableRef = (BLangSimpleVarRef) groupingKey;
    }

    @Override
    public Node getGroupingKey() {
        if (this.variableDef != null) {
            return this.variableDef;
        }
        return this.variableRef;
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
    public NodeKind getKind() {
        return NodeKind.GROUPING_KEY;
    }
    
    @Override
    public String toString() {
        if (this.variableDef != null) {
            return this.variableDef.toString();
        }
        return this.variableRef.toString();
    }
}
