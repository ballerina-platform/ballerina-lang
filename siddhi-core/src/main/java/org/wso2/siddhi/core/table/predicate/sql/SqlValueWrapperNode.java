package org.wso2.siddhi.core.table.predicate.sql;

import org.wso2.siddhi.core.table.predicate.PredicateTreeNode;

import java.util.List;

public class SqlValueWrapperNode implements PredicateTreeNode {

    private Object value;

    public SqlValueWrapperNode(Object value) {
        this.value = value;
    }

    @Override
    public String buildPredicateString() {
        return " ? ";
    }

    @Override
    public void populateParameters(List parametersList) {
        parametersList.add(value);
    }

    @Override
    public void populateTokens(List tokenList) {
        tokenList.add("val:" + value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
