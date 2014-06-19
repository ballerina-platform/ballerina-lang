package org.wso2.siddhi.core.table.predicate.sql;

import org.wso2.siddhi.core.table.predicate.PredicateTreeNode;

import java.util.List;

public class SqlVariableNode implements PredicateTreeNode {
    private String variableExpression;

    public SqlVariableNode(String variableExpression) {
        this.variableExpression = variableExpression;
    }

    @Override
    public String buildPredicateString() {
        return variableExpression;
    }

    @Override
    public void populateParameters(List parametersList) {
        // do nothing.
    }

    @Override
    public void populateTokens(List tokenList) {
        tokenList.add("var:" + variableExpression);
    }

    @Override
    public String toString() {
        return buildPredicateString();
    }
}
