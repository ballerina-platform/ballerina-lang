/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.siddhi.query.api.execution.query.selection;

import org.ballerinalang.siddhi.query.api.SiddhiElement;
import org.ballerinalang.siddhi.query.api.expression.Variable;

/**
 * Query order by attribute.
 */
public class OrderByAttribute implements SiddhiElement {

    private static final long serialVersionUID = 1L;
    private Order order = Order.ASC;
    private Variable variable;
    private int[] queryContextStartIndex;
    private int[] queryContextEndIndex;

    public OrderByAttribute(Variable variable, Order order) {
        this.order = order;
        this.variable = variable;
    }

    public OrderByAttribute(Variable variable) {
        this.variable = variable;
    }

    public Order getOrder() {
        return order;
    }

    public Variable getVariable() {
        return variable;
    }

    @Override
    public int[] getQueryContextStartIndex() {
        return queryContextStartIndex;
    }

    @Override
    public void setQueryContextStartIndex(int[] lineAndColumn) {
        queryContextStartIndex = lineAndColumn;
    }

    @Override
    public int[] getQueryContextEndIndex() {
        return queryContextEndIndex;
    }

    @Override
    public void setQueryContextEndIndex(int[] lineAndColumn) {
        queryContextEndIndex = lineAndColumn;
    }

    /**
     * enum for ascending and descending.
     */
    public enum Order {
        ASC,
        DESC
    }
}
