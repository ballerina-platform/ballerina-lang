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
import org.wso2.siddhi.query.api.extension.Extension;

public class WindowExtension extends Window implements Extension {

    private String namespace;


    public WindowExtension(String namespace, String functionName, Expression[] parameters) {
        super(functionName, parameters);
        this.namespace = namespace;
    }

    public WindowExtension(String namespace, String functionName) {
        super(functionName);
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    @Override
    public String toString() {
        return "WindowExtension{" +
                "namespace='" + namespace + '\'' +
                "} ";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WindowExtension)) return false;

        WindowExtension that = (WindowExtension) o;

        if (!namespace.equals(that.namespace)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return namespace.hashCode();
    }
}