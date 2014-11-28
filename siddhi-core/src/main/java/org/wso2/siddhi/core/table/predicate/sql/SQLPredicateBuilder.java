package org.wso2.siddhi.core.table.predicate.sql;

import org.wso2.siddhi.core.table.predicate.PredicateBuilder;
import org.wso2.siddhi.core.table.predicate.PredicateTreeNode;

/**
 * SQL specific implementation of the PredicateBuilder
 */
public class SQLPredicateBuilder implements PredicateBuilder {


    @Override
    public PredicateTreeNode wrapPredicate(PredicateTreeNode predicate) {
        return new SQLPredicateWrapperNode(predicate);
    }

    @Override
    public PredicateTreeNode buildVariableExpression(String variable) {
        return new SQLVariableNode(variable);
    }

    @Override
    public PredicateTreeNode buildNotCondition(PredicateTreeNode subPredicate) {
        return new SQLUnaryConditionNode("!", subPredicate);
    }

    @Override
    public PredicateTreeNode buildBinaryCondition(PredicateTreeNode leftSubPredicate, PredicateTreeNode rightSubPredicate, BinaryOperator operator) {
        switch (operator) {
            case AND:
                return new SQLBinaryConditionNode(leftSubPredicate, rightSubPredicate, " AND ");
            case OR:
                return new SQLBinaryConditionNode(leftSubPredicate, rightSubPredicate, " OR ");
        }
        return new SQLBinaryConditionNode(leftSubPredicate, rightSubPredicate, " OR ");
    }

    @Override
    public PredicateTreeNode buildCompareCondition(PredicateTreeNode leftPredicate, PredicateTreeNode rightPredicate, ComparisonType comparisonType) {
        String operator = "";
        switch (comparisonType) {
            case EQUALS:
                operator = " = ";
                break;
            case LESS_THAN:
                operator = " < ";
                break;
            case LESS_THAN_OR_EQUALS:
                operator = " <= ";
                break;
            case GREATER_THAN:
                operator = " > ";
                break;
            case GREATER_THAN_OR_EQUALS:
                operator = " >= ";
                break;
            case NOT_EQUALS:
                operator = " <> ";
                break;

        }
        return new SQLBinaryConditionNode(leftPredicate, rightPredicate, operator);
    }

    @Override
    public PredicateTreeNode buildValue(Object value) {

        return new SQLValueWrapperNode(value);
    }

}
