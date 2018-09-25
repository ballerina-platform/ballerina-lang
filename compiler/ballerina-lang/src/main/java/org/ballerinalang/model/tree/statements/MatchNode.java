/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.tree.statements;

import org.ballerinalang.model.tree.RecordVariableNode;
import org.ballerinalang.model.tree.TupleVariableNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;

import java.util.List;

/**
 * {@code MatchNode} represents a type switch statement in Ballerina.
 *
 * @since 0.966.0
 */
public interface MatchNode {

    /**
     * {@code MatchStatementSimpleBindingPatternNode} represents a pattern inside a type switch statement.
     *
     * @since 0.966.0
     */
    interface MatchStatementSimpleBindingPatternNode {

        SimpleVariableNode getVariableNode();

        StatementNode getStatement();
    }

    /**
     * @since 0.982.0
     */
    interface MatchStatementTupleBindingPatternNode {

        TupleVariableNode getTupleVariableNode();

        StatementNode getStatement();
    }

    /**
     * @since 0.982.0
     */
    interface MatchStatementRecordBindingPatternNode {

        RecordVariableNode getRecordVariableNode();

        StatementNode getStatement();
    }


    ExpressionNode getExpression();

    List<? extends MatchStatementSimpleBindingPatternNode> getSimplePatternClauses();

    List<? extends MatchStatementTupleBindingPatternNode> getTuplePatternClauses();

    List<? extends MatchStatementRecordBindingPatternNode> getRecordPatternClauses();
}
