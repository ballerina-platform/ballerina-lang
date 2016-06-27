package org.wso2.siddhi.core.util.collection.expression;

import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Created by suho on 6/25/16.
 */
public class NullCollectionExpression implements CollectionExpression {

    private final Expression expression;
    private final CollectionScope collectionScope;
    private CollectionExpression collectionExpression;

    public NullCollectionExpression(Expression expression, CollectionScope collectionScope, CollectionExpression collectionExpression) {
        this.expression = expression;
        this.collectionScope = collectionScope;
        this.collectionExpression = collectionExpression;
    }

    @Override
    public Expression getExpression() {
        return expression;
    }

    @Override
    public CollectionScope getCollectionScope() {
        return collectionScope;
    }

    public CollectionExpression getCollectionExpression() {
        return collectionExpression;
    }
}
