package org.wso2.siddhi.core.util.collection.expression;

import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Created by suho on 6/25/16.
 */
public class NotCollectionExpression implements CollectionExpression {

    private final Expression expression;
    private final CollectionScope collectionScope;
    private CollectionExpression collectionExpression;

    public NotCollectionExpression(Expression expression, CollectionScope collectionScope, CollectionExpression collectionExpression) {
        this.expression = expression;
        this.collectionScope = collectionScope;
        this.collectionExpression = collectionExpression;
    }

    public CollectionExpression getCollectionExpression() {
        return collectionExpression;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public CollectionScope getCollectionScope() {
        return collectionScope;
    }
}
