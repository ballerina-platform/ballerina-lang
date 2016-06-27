package org.wso2.siddhi.core.util.collection.expression;

import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Created by suho on 6/25/16.
 */
public class BasicCollectionExpression implements CollectionExpression {
    private final Expression expression;
    private final CollectionScope collectionScope;

    public BasicCollectionExpression(Expression expression, CollectionScope collectionScope) {
        this.expression = expression;
        this.collectionScope = collectionScope;
    }


    public Expression getExpression() {
        return expression;
    }

    public CollectionScope getCollectionScope() {
        return collectionScope;
    }

}
