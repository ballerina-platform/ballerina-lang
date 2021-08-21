/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.evaluation.engine;

import io.ballerina.compiler.syntax.tree.BindingPatternNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.FromClauseNode;
import io.ballerina.compiler.syntax.tree.JoinClauseNode;
import io.ballerina.compiler.syntax.tree.LetClauseNode;
import io.ballerina.compiler.syntax.tree.LimitClauseNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.OrderByClauseNode;
import io.ballerina.compiler.syntax.tree.QueryExpressionNode;
import io.ballerina.compiler.syntax.tree.SelectClauseNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.WhereClauseNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Syntax tree visitor implementation to capture all the variable references within query expressions.
 *
 * @since 2.0.0
 */
public class QueryReferenceFinder extends NodeVisitor {

    private boolean isWithinLetClause = false;
    private final QueryExpressionNode queryExpressionNode;
    private final Set<String> internalVariables = new HashSet<>();
    private final List<String> letVariables = new ArrayList<>();
    private final Set<String> capturedVariables = new HashSet<>();

    public QueryReferenceFinder(QueryExpressionNode node) {
        this.queryExpressionNode = node;
    }

    /**
     * @return Captures and returns all the variable references within the given query expression.
     */
    public Set<String> getCapturedVariables() {
        queryExpressionNode.accept(this);
        return capturedVariables;
    }

    @Override
    public void visit(FromClauseNode fromClauseNode) {
        BindingPatternNode bindingPattern = fromClauseNode.typedBindingPattern().bindingPattern();
        internalVariables.addAll(extractVariablesFromBindingPattern(bindingPattern));
        fromClauseNode.expression().accept(this);
    }

    @Override
    public void visit(WhereClauseNode whereClauseNode) {
        whereClauseNode.expression().accept(this);
    }

    @Override
    public void visit(JoinClauseNode joinClauseNode) {
        joinClauseNode.expression().accept(this);
    }

    @Override
    public void visit(LetClauseNode letClauseNode) {
        letClauseNode.letVarDeclarations().forEach(declarationNode -> {
            BindingPatternNode bindingPattern = declarationNode.typedBindingPattern().bindingPattern();
            letVariables.addAll(extractVariablesFromBindingPattern(bindingPattern));
        });

        // variables defined inside the let clause should be escaped when capturing external (local + global) variable
        // references
        isWithinLetClause = true;
        letClauseNode.letVarDeclarations().forEach(declarationNode -> declarationNode.expression().accept(this));
        isWithinLetClause = false;
    }

    @Override
    public void visit(LimitClauseNode limitClauseNode) {
        limitClauseNode.expression().accept(this);
    }

    @Override
    public void visit(OrderByClauseNode orderByClauseNode) {
    }

    @Override
    public void visit(SelectClauseNode selectClauseNode) {
        selectClauseNode.expression().accept(this);
    }

    @Override
    public void visit(SimpleNameReferenceNode simpleNameReferenceNode) {
        String variableRef = simpleNameReferenceNode.name().text().trim();
        if (isWithinLetClause && !internalVariables.contains(variableRef) && !letVariables.contains(variableRef)) {
            if (!internalVariables.contains(variableRef) && !letVariables.contains(variableRef)) {
                capturedVariables.add(variableRef);
            }
        } else if (!internalVariables.contains(variableRef)) {
            capturedVariables.add(variableRef);
        }
    }

    private List<String> extractVariablesFromBindingPattern(BindingPatternNode bindingPattern) {
        List<String> capturedVariableNames = new ArrayList<>();
        if (bindingPattern instanceof CaptureBindingPatternNode) {
            capturedVariableNames.add(((CaptureBindingPatternNode) bindingPattern).variableName().text().trim());
        }
        return capturedVariableNames;
    }
}
