package org.wso2.siddhi.core.util.collection.expression;

import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.condition.Compare;

/**
 * Created by suho on 6/25/16.
 */
public class CompareCollectionExpression implements CollectionExpression {

    private final Compare compareExpression;
    private final CollectionScope collectionScope;
    private CollectionExpression leftCollectionExpression;
    private CollectionExpression rightCollectionExpression;

    public CompareCollectionExpression(Compare compareExpression, CollectionScope collectionScope, CollectionExpression leftCollectionExpression, CollectionExpression rightCollectionExpression) {
        this.compareExpression = compareExpression;
        this.collectionScope = collectionScope;
        this.leftCollectionExpression = leftCollectionExpression;
        this.rightCollectionExpression = rightCollectionExpression;
    }

    public CollectionExpression getLeftCollectionExpression() {
        return leftCollectionExpression;
    }

    public CollectionExpression getRightCollectionExpression() {
        return rightCollectionExpression;
    }

    public Expression getExpression() {
        return compareExpression;
    }

    public Compare.Operator getOperator() {
        return compareExpression.getOperator();
    }

    @Override
    public CollectionScope getCollectionScope() {
        return collectionScope;
    }
}
