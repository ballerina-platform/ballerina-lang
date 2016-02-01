/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.query.api.execution.query.input.handler;

import org.wso2.siddhi.query.api.expression.Expression;

import java.util.Arrays;

public class Window implements StreamHandler {

    private String function;
    private Expression[] parameters;

    public Window(String function, Expression[] parameters) {
        this.function = function;
        this.parameters = parameters;
    }

    public Window(String function) {
        this.function = function;
    }

    public String getFunction() {
        return function;
    }

    public Expression[] getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "Window{" +
                "function='" + function + '\'' +
                ", parameters=" + Arrays.toString(parameters) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Window)) return false;

        Window window = (Window) o;

        if (!function.equals(window.function)) return false;
        if (!Arrays.equals(parameters, window.parameters)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = function.hashCode();
        result = 31 * result + (parameters != null ? Arrays.hashCode(parameters) : 0);
        return result;
    }
}
