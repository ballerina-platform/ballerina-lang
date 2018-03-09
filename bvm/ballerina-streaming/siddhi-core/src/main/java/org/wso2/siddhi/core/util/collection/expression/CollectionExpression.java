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

import java.util.Set;

/**
 * Interface for expressions related to collection of events. These will be used by in-memory table implementation.
 */
public interface CollectionExpression {

    Expression getExpression();

    CollectionScope getCollectionScope();

    Set<String> getMultiPrimaryKeys();

    /**
     * Enums to hold collection expression scopes.
     */
    enum CollectionScope {
        NON,
        PRIMARY_KEY_ATTRIBUTE,
        PRIMARY_KEY_RESULT_SET,
        INDEXED_ATTRIBUTE,
        INDEXED_RESULT_SET,
        PARTIAL_PRIMARY_KEY_ATTRIBUTE,
        PARTIAL_PRIMARY_KEY_RESULT_SET,
        OPTIMISED_PRIMARY_KEY_OR_INDEXED_RESULT_SET,
        EXHAUSTIVE
    }

}
