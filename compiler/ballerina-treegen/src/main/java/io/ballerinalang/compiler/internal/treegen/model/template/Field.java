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

import io.ballerinalang.compiler.internal.treegen.model.OccurrenceKind;

import java.util.Locale;

/**
 * Represents a instance field of a syntax tree node.
 *
 * @since 1.3.0
 */
public class Field {
    private static final String LIST_CN = "NodeList";
    private static final String SEPARATED_LIST_CN = "SeparatedNodeList";
    private final String fieldClassName;
    private final String fieldTSClassName;
    private final String fieldName;
    private final int index;
    private final boolean isLast;

    private final boolean isToken;
    private final boolean isNode;
    private final boolean isList;
    private final boolean isOptional;

    private final OccurrenceKind occurrenceKind;

    public Field(String fieldClassName,
                 String fieldName,
                 int index,
                 OccurrenceKind occurrenceKind,
                 boolean isOptional,
                 boolean isLast) {
        this.fieldClassName = fieldClassName;
        this.fieldTSClassName = "Node".equals(fieldClassName)
                ? ("ST" + fieldClassName)
                : fieldClassName.replaceAll("Node", "");
        this.fieldName = fieldName;
        this.index = index;
        this.occurrenceKind = occurrenceKind;
        this.isLast = isLast;

        this.isList = occurrenceKind == OccurrenceKind.MULTIPLE ||
                occurrenceKind == OccurrenceKind.MULTIPLE_SEPARATED;
        this.isOptional = isOptional;
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

    public boolean isOptional() {
        return isOptional;
    }

    public String titleCaseFieldName() {
        String firstChar = fieldName.substring(0, 1);
        return fieldName.replaceFirst(firstChar, firstChar.toUpperCase(Locale.ENGLISH));
    }

    public String listClassName() {
        if (!isList()) {
            throw new IllegalStateException("This method should only be invoked when the field type is a list");
        }
        return occurrenceKind == OccurrenceKind.MULTIPLE ? LIST_CN : SEPARATED_LIST_CN;
    }

    public String fieldTSClassName() {
        return fieldTSClassName;
    }
}
