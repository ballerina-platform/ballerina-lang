/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.ballerina.core.model.values;

import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.expressions.Expression;

/**
 * The {@code BConnector} represents a Connector in Ballerina
 *
 * @since 1.0.0
 */
public final class BConnector implements BRefType<Connector> {

    private Connector connector;
    private Expression[] argExprs;

    public BConnector() {
        this(null, new Expression[0]);
    }

    public BConnector(Connector connector, Expression[] argExprs) {
        this.connector = connector;
        this.argExprs = argExprs;
    }

    public Expression[] getArgExprs() {
        return argExprs;
    }

    @Override
    public Connector value() {
        return connector;
    }

    @Override
    public String stringValue() {
        return null;
    }
}
