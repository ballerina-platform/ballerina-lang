package org.wso2.siddhi.core.table.predicate;

import java.util.List;

public interface PredicateTreeNode {

    /**
     * Resulting predicate of the tree starting from this node
     *
     * @return the string for the predicate string
     */
    public String buildPredicateString();

    /**
     * In-order traversed parameter list
     *
     * @param parametersList the list of parameters to populate
     */
    public void populateParameters(List parametersList);


    /**
     * In-order traversed list of all tokens.
     * e.g. price &gt; 12.50 will result in a token list of [price, &gt;, 12.50]
     *
     * @param tokens tokens to traverse
     */
    public void populateTokens(List tokens);

}
