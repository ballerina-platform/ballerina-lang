package org.wso2.siddhi.core.table.predicate;

import java.util.List;

public interface PredicateTreeNode {

    /**
     * Resulting predicate of the tree starting from this node
     *
     * @return
     */
    public String buildPredicateString();

    /**
     * In-order traversed parameter list
     *
     * @return
     */
    public void populateParameters(List parametersList);


    /**
     * In-order traversed list of all tokens.
     * e.g. price > 12.50 will result in a token list of [price, >, 12.50]
     * @param tokens
     */
    public void populateTokens(List tokens);

}
