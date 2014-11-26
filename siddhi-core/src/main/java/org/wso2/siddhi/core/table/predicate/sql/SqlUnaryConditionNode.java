package org.wso2.siddhi.core.table.predicate.sql;

import org.wso2.siddhi.core.table.predicate.PredicateTreeNode;

import java.util.List;

public class SqlUnaryConditionNode implements PredicateTreeNode {
    String operator;
    PredicateTreeNode child;

    public SqlUnaryConditionNode(String operator, PredicateTreeNode child) {
        this.operator = operator;
        this.child = child;
    }

    @Override
    public String buildPredicateString() {
        return operator + " " + child.buildPredicateString();
    }

    @Override
    public void populateParameters(List parametersList) {
        child.populateParameters(parametersList);
    }

    @Override
    public void populateTokens(List tokenList) {
        tokenList.add("op:" + operator);
        child.populateTokens(tokenList);
    }

    @Override
    public String toString() {
        return buildPredicateString();
    }
}
