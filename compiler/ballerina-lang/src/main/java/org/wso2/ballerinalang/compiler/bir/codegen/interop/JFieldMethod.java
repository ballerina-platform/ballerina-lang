/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.bir.codegen.interop;

/**
 * This enum is used to indicate whether the given Ballerina function mutates or access the java field.
 *
 * @since 1.2.0
 */
public enum JFieldMethod {
    ACCESS("access"),
    MUTATE("mutate");

    private final String strValue;

    JFieldMethod(String strValue) {
        this.strValue = strValue;
    }

    public static JFieldMethod getKind(String value) {
        if ("access".equals(value)) {
            return ACCESS;
        }
        return MUTATE;
    }

    public String getStringValue() {
        return this.strValue;
    }
}
