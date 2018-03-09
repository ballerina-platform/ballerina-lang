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
package org.ballerinalang.siddhi.query.api.execution.query.input.handler;

import org.ballerinalang.siddhi.query.api.SiddhiElement;
import org.ballerinalang.siddhi.query.api.expression.Expression;
import org.ballerinalang.siddhi.query.api.extension.Extension;

import java.util.Arrays;

/**
 * Siddhi window handler.
 */
public class Window implements StreamHandler, Extension, SiddhiElement {

    private static final long serialVersionUID = 1L;
    private String namespace = "";
    private String function;
    private Expression[] parameters;
    private int[] queryContextStartIndex;
    private int[] queryContextEndIndex;

    public Window(String namespace, String functionName, Expression[] parameters) {
        this.function = functionName;
        this.parameters = Arrays.copyOfRange(parameters, 0, parameters.length);
        this.namespace = namespace;
    }

    public Window(String namespace, String functionName) {
        this.function = functionName;
        this.namespace = namespace;
    }

    public Window(String functionName, Expression[] parameters) {
        this.function = functionName;
        this.parameters = Arrays.copyOfRange(parameters, 0, parameters.length);
    }

    public Window(String functionName) {
        this.function = functionName;
    }

    public String getName() {
        return function;
    }

    public Expression[] getParameters() {
        return Arrays.copyOfRange(parameters, 0, parameters.length);
    }

    public String getNamespace() {
        return namespace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Window window = (Window) o;

        if (function != null ? !function.equals(window.function) : window.function != null) {
            return false;
        }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(parameters, window.parameters)) {
            return false;
        }
        return namespace != null ? namespace.equals(window.namespace) : window.namespace == null;
    }

    @Override
    public int hashCode() {
        int result = function != null ? function.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(parameters);
        result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Window{" +
                "namespace='" + namespace + '\'' +
                ", function='" + function + '\'' +
                ", parameters=" + Arrays.toString(parameters) +
                '}';
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
}
