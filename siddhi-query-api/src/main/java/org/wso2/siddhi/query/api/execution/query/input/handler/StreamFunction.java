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
package org.wso2.siddhi.query.api.execution.query.input.handler;

import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.extension.Extension;

import java.util.Arrays;

/**
 * Siddhi stream function
 */
public class StreamFunction implements StreamHandler, Extension {

    private static final long serialVersionUID = 1L;
    private String namespace = "";
    private String function;
    private Expression[] parameters;

    public StreamFunction(String namespace, String function, Expression[] parameters) {
        this.namespace = namespace;
        this.function = function;
        this.parameters = parameters;

    }

    public StreamFunction(String namespace, String function) {
        this.namespace = namespace;
        this.function = function;
    }

    public StreamFunction(String function, Expression[] parameters) {
        this.function = function;
        this.parameters = parameters;
    }

    public StreamFunction(String function) {
        this.function = function;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return function;
    }

    public Expression[] getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "StreamFunction{" +
                "namespace='" + namespace + '\'' +
                ", function='" + function + '\'' +
                ", parameters=" + Arrays.toString(parameters) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StreamFunction that = (StreamFunction) o;

        if (namespace != null ? !namespace.equals(that.namespace) : that.namespace != null) {
            return false;
        }
        if (function != null ? !function.equals(that.function) : that.function != null) {
            return false;
        }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        int result = namespace != null ? namespace.hashCode() : 0;
        result = 31 * result + (function != null ? function.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(parameters);
        return result;
    }
}
