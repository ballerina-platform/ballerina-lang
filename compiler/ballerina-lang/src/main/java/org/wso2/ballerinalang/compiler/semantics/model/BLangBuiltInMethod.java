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

    IS_NAN("isNaN"),

    IS_INFINITE("isInfinite"),

    IS_FINITE("isFinite"),

    LENGTH("length"),

    REASON("reason"),

    DETAIL("detail"),

    STACKTRACE("stackTrace"),

    FREEZE("freeze"),

    IS_FROZEN("isFrozen"),

    CLONE("clone"),

    STAMP("stamp"),

    UNDEFINED("$undefined");

    private String name;

    BLangBuiltInMethod(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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
