package org.wso2.siddhi.core.table.predicate.sql;

import org.wso2.siddhi.core.table.predicate.PredicateToken;
import org.wso2.siddhi.core.table.predicate.PredicateTreeNode;

import java.util.List;

public class SQLUnaryConditionNode implements PredicateTreeNode {
    String operator;
    PredicateTreeNode child;

    public SQLUnaryConditionNode(String operator, PredicateTreeNode child) {
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
        tokenList.add(new PredicateToken(PredicateToken.Type.OPERATOR, operator));
        child.populateTokens(tokenList);
    }

    @Override
    public String toString() {
        return buildPredicateString();
    }
}
