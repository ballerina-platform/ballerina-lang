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
     * @param variable
     * @return
     */
    public PredicateTreeNode buildVariableExpression(String variable);

    /**
     * Boolean not conditions
     *
     * @param subPredicate
     * @return
     */
    public PredicateTreeNode buildNotCondition(PredicateTreeNode subPredicate);


    /**
     * To be used for two sided Boolean operations
     *
     * @param leftSubPredicate
     * @param rightSubPredicate
     * @param operator
     * @return
     */
    public PredicateTreeNode buildBinaryCondition(PredicateTreeNode leftSubPredicate, PredicateTreeNode rightSubPredicate, BinaryOperator operator);

    /**
     * To be used for Numerical comparisons
     *
     * @param leftPredicate
     * @param rightPredicate
     * @param comparisonType
     * @return
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
     * @param predicate
     * @return
     */
    public PredicateTreeNode wrapPredicate(PredicateTreeNode predicate);


}
