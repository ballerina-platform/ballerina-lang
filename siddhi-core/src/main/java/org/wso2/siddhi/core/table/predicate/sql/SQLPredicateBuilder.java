package org.wso2.siddhi.core.table.predicate.sql;

import org.wso2.siddhi.core.table.predicate.PredicateBuilder;
import org.wso2.siddhi.core.table.predicate.PredicateTreeNode;

/**
 * SQL specific implementation of the PredicateBuilder
 */
public class SQLPredicateBuilder implements PredicateBuilder {


    @Override
    public PredicateTreeNode wrapPredicate(PredicateTreeNode predicate) {
        return new SqlPredicateWrapperNode(predicate);
    }

    @Override
    public PredicateTreeNode buildVariableExpression(String variable) {
        return new SqlVariableNode(variable);
    }

    @Override
    public PredicateTreeNode buildNotCondition(PredicateTreeNode subPredicate) {
        return new SqlUnaryConditionNode("!", subPredicate);
    }

    @Override
    public PredicateTreeNode buildBinaryCondition(PredicateTreeNode leftSubPredicate, PredicateTreeNode rightSubPredicate, BinaryOperator operator) {
        switch (operator) {
            case AND:
                return new SqlBinaryConditionNode(leftSubPredicate, rightSubPredicate, " AND ");
            case OR:
                return new SqlBinaryConditionNode(leftSubPredicate, rightSubPredicate, " OR ");
        }
        return new SqlBinaryConditionNode(leftSubPredicate, rightSubPredicate, " OR ");
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
        return new SqlBinaryConditionNode(leftPredicate, rightPredicate, operator);
    }

    @Override
    public PredicateTreeNode buildValue(Object value) {

        return new SqlValueWrapperNode(value);
    }

}
