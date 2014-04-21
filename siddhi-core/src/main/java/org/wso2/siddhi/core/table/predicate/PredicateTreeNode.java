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

}
