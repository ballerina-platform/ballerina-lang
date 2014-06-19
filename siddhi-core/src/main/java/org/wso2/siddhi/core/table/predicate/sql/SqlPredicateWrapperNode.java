package org.wso2.siddhi.core.table.predicate.sql;

import org.wso2.siddhi.core.table.predicate.PredicateTreeNode;

import java.util.List;

public class SqlPredicateWrapperNode implements PredicateTreeNode {
    private PredicateTreeNode child;

    public SqlPredicateWrapperNode(PredicateTreeNode child) {
        this.child = child;
    }

    @Override
    public String buildPredicateString() {
        return "(" + child.buildPredicateString() + ")";
    }

    @Override
    public void populateParameters(List list) {
        child.populateParameters(list);
    }

    @Override
    public void populateTokens(List list) {
        child.populateTokens(list);
    }

    @Override
    public String toString() {
        return buildPredicateString();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
