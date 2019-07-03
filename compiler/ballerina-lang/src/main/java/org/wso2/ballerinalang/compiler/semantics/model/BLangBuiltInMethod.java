/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.model;

/**
 * Ballerina Built-in function list.
 *
 * @since 0.983.0
 */
public enum BLangBuiltInMethod {

    IS_NAN("isNaN", true),

    IS_INFINITE("isInfinite", true),

    IS_FINITE("isFinite", true),

    STRING_LENGTH("string.length", true),

    LENGTH("length", true),

    REASON("reason", true),

    DETAIL("detail", true),

    STACKTRACE("stackTrace", true),

    FREEZE("freeze", true),

    IS_FROZEN("isFrozen", true),

    CLONE("clone", true),

    STAMP("stamp", true),

    CONVERT("convert", true),

    SIMPLE_VALUE_CONVERT("simpleValueConvert", true),
    
    CALL("call", true),

    ITERATE("iterator", false),

    NEXT("next", false),

    UNDEFINED("$undefined", true);

    private String name;
    private boolean isExternal;

    BLangBuiltInMethod(String name, boolean isExternal) {
        this.name = name;
        this.isExternal = isExternal;
    }

    public String getName() {
        return name;
    }

    public boolean isExternal() {
        return isExternal;
    }

    @Override
    public String toString() {
        return name;
    }

    public static BLangBuiltInMethod getFromString(String name) {
        for (BLangBuiltInMethod function : BLangBuiltInMethod.values()) {
            if (function.name.equals(name)) {
                return function;
            }
        }
        return BLangBuiltInMethod.UNDEFINED;
    }
}
