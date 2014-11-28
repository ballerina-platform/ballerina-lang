package org.wso2.siddhi.core.table.predicate.sql;

import org.wso2.siddhi.core.table.predicate.PredicateToken;
import org.wso2.siddhi.core.table.predicate.PredicateTreeNode;

import java.util.List;

public class SQLValueWrapperNode implements PredicateTreeNode {

    private Object value;

    public SQLValueWrapperNode(Object value) {
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
        // todo - check if needed to add value as an object
        tokenList.add(new PredicateToken(PredicateToken.Type.VALUE, value.toString()));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
