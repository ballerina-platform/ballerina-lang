/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.model.tree;

/**
 * Used to identify the type of backticked reference in Markdown Documentation strings.
 *
 * @since 1.1.0
 */
public enum DocumentationReferenceType {
    TYPE("type"), // Match for type reference in documentation
    SERVICE("service"), // Match for service reference in documentation
    VARIABLE("variable"), // Match for variable references in documentation
    VAR("var"), // Match for var references in documentation
    ANNOTATION("annotation"), // Match for annotation references in documentation
    CONST("const"), // Match for const references in documentation
    MODULE("module"), // Match for module references in documentation
    FUNCTION("function"), // Match for function references in documentation
    PARAMETER("parameter"), // Match for parameter in documentation
    BACKTICK_CONTENT("function"); // This content is extracted from only backticks. Hence should validate for a function

    private String value;

    DocumentationReferenceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
