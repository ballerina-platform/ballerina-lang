package org.wso2.siddhi.core.util.collection.expression;

import org.wso2.siddhi.query.api.expression.Expression;

import java.util.List;

/**
 * Created by suho on 6/25/16.
 */
public class AttributeCollectionExpression implements CollectionExpression {
    private final Expression expression;
    private final CollectionScope collectionScope = CollectionScope.INDEXED_ATTRIBUTE;
    private String attribute;

    public AttributeCollectionExpression(Expression expression, String attribute) {

        this.expression = expression;
        this.attribute = attribute;
    }

    public Expression getExpression() {
        return expression;
    }

    public CollectionScope getCollectionScope() {
        return collectionScope;
    }

    public String getAttribute() {
        return attribute;
    }
}
