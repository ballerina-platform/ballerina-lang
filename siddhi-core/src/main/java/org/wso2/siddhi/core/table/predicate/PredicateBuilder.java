package org.wso2.siddhi.core.table.predicate;

/**
 * Intended to be used for database type specific query predicate creation.
 */
public interface PredicateBuilder {

    /**
     * Numerical comparison types
     */
    public enum ComparisonType {
        EQUALS, GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUALS, LESS_THAN_OR_EQUALS, NOT_EQUALS;
    }

    /**
     * Two sided boolean operations
     */
    public enum BinaryOperator {
        AND, OR;
    }


    /**
     * For creating expression that includes a variable
     *
     * @param variable variable name
     * @return the built predicate tree node
     */
    public PredicateTreeNode buildVariableExpression(String variable);

    /**
     * Boolean not conditions
     *
     * @param subPredicate predicate tree node with sub predicate
     * @return predicate tree with not condition
     */
    public PredicateTreeNode buildNotCondition(PredicateTreeNode subPredicate);


    /**
     * To be used for two sided Boolean operations
     *
     * @param leftSubPredicate  predicate tree for left condition
     * @param rightSubPredicate predicate tree for right condition
     * @param operator          operator to be applied
     * @return predicate tree with the binary condition applied
     */
    public PredicateTreeNode buildBinaryCondition(PredicateTreeNode leftSubPredicate, PredicateTreeNode rightSubPredicate, BinaryOperator operator);

    /**
     * To be used for Numerical comparisons
     *
     * @param leftPredicate  predicate tree for left expression
     * @param rightPredicate predicate tree for right expression
     * @param comparisonType the comparison type GREATER_THAN, EQUALS etc.
     * @return the predicate tree with the comparison
     */
    public PredicateTreeNode buildCompareCondition(PredicateTreeNode leftPredicate, PredicateTreeNode rightPredicate, ComparisonType comparisonType);

    /**
     * Converting the values to the format expected by DBMS
     *
     * @param value native java value
     * @return value expected by DBMS
     */
    public PredicateTreeNode buildValue(Object value);

    /**
     * wrap with parenthesis or other database specific means.
     *
     * @param predicate predicate tree to be wrapped
     * @return wrapped predicate tree
     */
    public PredicateTreeNode wrapPredicate(PredicateTreeNode predicate);


}
