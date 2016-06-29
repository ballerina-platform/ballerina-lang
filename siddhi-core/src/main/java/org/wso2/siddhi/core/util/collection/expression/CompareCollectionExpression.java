package org.wso2.siddhi.core.util.collection.expression;

import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.condition.Compare;

public class CompareCollectionExpression implements CollectionExpression {

    private final Compare compareExpression;
    private final CollectionScope collectionScope;
    private CollectionExpression attributeCollectionExpression;
    private CollectionExpression valueCollectionExpression;

    public CompareCollectionExpression(Compare compareExpression, CollectionScope collectionScope, CollectionExpression attributeCollectionExpression, Compare.Operator operator, CollectionExpression valueCollectionExpression) {
        this.compareExpression = compareExpression;
        this.collectionScope = collectionScope;
        this.attributeCollectionExpression = attributeCollectionExpression;
        this.valueCollectionExpression = valueCollectionExpression;
    }

    public CollectionExpression getAttributeCollectionExpression() {
        return attributeCollectionExpression;
    }

    public CollectionExpression getValueCollectionExpression() {
        return valueCollectionExpression;
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
