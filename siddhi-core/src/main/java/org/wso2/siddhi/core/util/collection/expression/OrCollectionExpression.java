/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.util.collection.expression;

import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Implementation of {@link CollectionExpression} which represent Or expressions.
 */
public class OrCollectionExpression implements CollectionExpression {

    private final Expression expression;
    private final CollectionScope collectionScope;
    private CollectionExpression leftCollectionExpression;
    private CollectionExpression rightCollectionExpression;

    public OrCollectionExpression(Expression expression, CollectionScope collectionScope, CollectionExpression
            leftCollectionExpression, CollectionExpression rightCollectionExpression) {
        this.expression = expression;
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
        return expression;
    }

    @Override
    public CollectionScope getCollectionScope() {
        return collectionScope;
    }

}
