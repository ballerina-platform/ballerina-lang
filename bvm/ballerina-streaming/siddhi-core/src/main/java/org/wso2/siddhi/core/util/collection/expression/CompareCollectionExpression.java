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
import org.wso2.siddhi.query.api.expression.condition.Compare;

import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of {@link CollectionExpression} which represent Compare expressions.
 */
public class CompareCollectionExpression implements CollectionExpression {

    private final Compare compareExpression;
    private final CollectionScope collectionScope;
    private final HashSet<String> multiPrimaryKeys = new HashSet<>();
    private CollectionExpression attributeCollectionExpression;
    private Compare.Operator operator;
    private CollectionExpression valueCollectionExpression;


    public CompareCollectionExpression(Compare compareExpression, CollectionScope collectionScope,
                                       CollectionExpression attributeCollectionExpression, Compare.Operator operator,
                                       CollectionExpression valueCollectionExpression) {
        this.compareExpression = compareExpression;
        this.collectionScope = collectionScope;
        this.attributeCollectionExpression = attributeCollectionExpression;
        this.operator = operator;
        this.valueCollectionExpression = valueCollectionExpression;
        if (operator == Compare.Operator.EQUAL) {
            multiPrimaryKeys.addAll(attributeCollectionExpression.getMultiPrimaryKeys());
            multiPrimaryKeys.addAll(valueCollectionExpression.getMultiPrimaryKeys());
        }
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
        return operator;
    }

    @Override
    public CollectionScope getCollectionScope() {
        return collectionScope;
    }

    @Override
    public Set<String> getMultiPrimaryKeys() {
        return multiPrimaryKeys;
    }
}
