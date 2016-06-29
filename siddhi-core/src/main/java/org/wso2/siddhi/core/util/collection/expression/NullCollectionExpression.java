package org.wso2.siddhi.core.util.collection.expression;

import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Created by suho on 6/25/16.
 */
public class NullCollectionExpression implements CollectionExpression {

    private final Expression expression;
    private final CollectionScope collectionScope;
    private String attribute;

    public NullCollectionExpression(Expression expression, CollectionScope collectionScope, String attribute) {

        this.expression = expression;
        this.collectionScope = collectionScope;
        this.attribute = attribute;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public CollectionScope getCollectionScope() {
        return collectionScope;
    }

    public String getAttribute() {
        return attribute;
    }
}
