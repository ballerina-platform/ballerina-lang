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

import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of {@link CollectionExpression} which represent attribute expressions.
 */
public class AttributeCollectionExpression implements CollectionExpression {
    private final Expression expression;
    private final CollectionScope collectionScope;
    private final HashSet<String> multiPrimaryKeys = new HashSet<>();
    private String attribute;

    public AttributeCollectionExpression(Expression expression, String attribute, CollectionScope collectionScope) {
        this.expression = expression;
        this.attribute = attribute;
        this.collectionScope = collectionScope;
        if (collectionScope == CollectionScope.PRIMARY_KEY_ATTRIBUTE
                || collectionScope == CollectionScope.PARTIAL_PRIMARY_KEY_ATTRIBUTE) {
            multiPrimaryKeys.add(attribute);
        }
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

    @Override
    public Set<String> getMultiPrimaryKeys() {
        return multiPrimaryKeys;
    }

}
