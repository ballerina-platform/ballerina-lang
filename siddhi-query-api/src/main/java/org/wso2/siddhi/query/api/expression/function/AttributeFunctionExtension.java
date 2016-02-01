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
package org.wso2.siddhi.query.api.expression.function;

import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.extension.Extension;

import java.util.Arrays;

public class AttributeFunctionExtension extends AttributeFunction implements Extension {

    private String extensionName;

    public AttributeFunctionExtension(String extensionName, String functionName, Expression... parameters) {
        super(functionName, parameters);
        this.extensionName = extensionName;
    }

    public String getNamespace() {
        return extensionName;
    }

    @Override
    public String toString() {
        return "AttributeFunction{" +
                "extensionName='" + extensionName + '\'' +
                ", functionName='" + functionName + '\'' +
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

        AttributeFunctionExtension that = (AttributeFunctionExtension) o;

        if (extensionName != null ? !extensionName.equals(that.extensionName) : that.extensionName != null) {
            return false;
        }
        if (functionName != null ? !functionName.equals(that.functionName) : that.functionName != null) {
            return false;
        }
        if (!Arrays.equals(parameters, that.parameters)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = extensionName != null ? extensionName.hashCode() : 0;
        result = 31 * result + (functionName != null ? functionName.hashCode() : 0);
        result = 31 * result + (parameters != null ? Arrays.hashCode(parameters) : 0);
        return result;
    }

}