package org.wso2.siddhi.core.util.collection.expression;

import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Created by suho on 6/25/16.
 */
public interface CollectionExpression {
    enum CollectionScope {NON, INDEXED_ATTRIBUTE, INDEXED_RESULT_SET, OPTIMISED_RESULT_SET, EXHAUSTIVE}

    public Expression getExpression();

    public CollectionScope getCollectionScope();

//    public void setAttributeNames(List<String> attributeNames);

//    public List<String> getAttributeNames();
}
