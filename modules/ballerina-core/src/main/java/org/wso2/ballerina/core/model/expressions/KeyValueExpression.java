/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.model.expressions;

import org.wso2.ballerina.core.model.NodeVisitor;

/**
 * Class to hold map initialization data
 */
public class KeyValueExpression extends AbstractExpression {
    String key;
    Expression valueExpression;

    public KeyValueExpression(String key, Expression valueExpression) {
        this.key = key;
        this.valueExpression = valueExpression;
    }
    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
    public String getKey() {
        return key;
    }
    public Expression getValueExpression() {
        return valueExpression;
    }
}
