/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bindgen.model;

/**
 * Define various kinds of syntax tree nodes, tokens and minutiae required for generating bindings.
 *
 * @since 2.0.0
 */
public enum BindgenSyntaxKind {

    REGULAR_FUNCTION("function"),
    EXTERNAL_FUNCTION("external"),
    INSTANCE_FUNCTION("instance"),
    STATIC_FUNCTION("static"),
    EXTERNAL_METHOD_FUNCTION("method"),
    EXTERNAL_FIELD_GET_FUNCTION("field_get"),
    EXTERNAL_FIELD_SET_FUNCTION("field_set"),
    EXTERNAL_FIELD_CONSTRUCTOR_FUNCTION("constructor");

    private final String strValue;

    BindgenSyntaxKind(String strValue) {
        this.strValue = strValue;
    }

    public String stringValue() {
        return strValue;
    }
}
