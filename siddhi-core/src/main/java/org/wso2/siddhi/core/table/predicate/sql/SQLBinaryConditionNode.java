package org.wso2.siddhi.core.table.predicate.sql;

import org.wso2.siddhi.core.table.predicate.PredicateToken;
import org.wso2.siddhi.core.table.predicate.PredicateTreeNode;

import java.util.List;

public class SQLBinaryConditionNode implements PredicateTreeNode {

    private PredicateTreeNode leftChild;
    private PredicateTreeNode rightChild;
    private String operator;

    public SQLBinaryConditionNode(PredicateTreeNode leftSubPredicate, PredicateTreeNode rightSubPredicate, String operator) {
        this.operator = operator;
        leftChild = leftSubPredicate;
        rightChild = rightSubPredicate;
    }

    @Override
    public String buildPredicateString() {
        return leftChild.buildPredicateString() + " " + operator + " " + rightChild.buildPredicateString();
    }

    @Override
    public void populateParameters(List parametersList) {
        // inorder traverse.
        leftChild.populateParameters(parametersList);
        rightChild.populateParameters(parametersList);
    }

    @Override
    public void populateTokens(List tokensList) {
        // inorder traverse.
        leftChild.populateTokens(tokensList);
        tokensList.add(new PredicateToken(PredicateToken.Type.OPERATOR, this.operator));
        rightChild.populateTokens(tokensList);
    }

    @Override
    public String toString() {
        return buildPredicateString();
    }
}
