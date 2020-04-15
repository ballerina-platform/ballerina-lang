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
package io.ballerinalang.compiler.internal.treegen.model.template;

/**
 * Represents a instance field of a syntax tree node.
 *
 * @since 1.3.0
 */
public class Field {
    private final String fieldClassName;
    private final String fieldName;
    private final int index;
    private final boolean isLast;

    private final boolean isToken;
    private final boolean isNode;
    private final boolean isList;

    public Field(String fieldClassName, String fieldName, int index, boolean isList, boolean isLast) {
        this.fieldClassName = fieldClassName;
        this.fieldName = fieldName;
        this.index = index;
        this.isLast = isLast;

        this.isList = isList;
        this.isToken = "Token".equals(fieldClassName);
        this.isNode = !(isList || isToken);
    }

    public String fieldClassName() {
        return fieldClassName;
    }

    public String fieldName() {
        return fieldName;
    }

    public int index() {
        return index;
    }

    public boolean isLast() {
        return isLast;
    }

    public boolean isToken() {
        return isToken;
    }

    public boolean isNode() {
        return isNode;
    }

    public boolean isList() {
        return isList;
    }
}
