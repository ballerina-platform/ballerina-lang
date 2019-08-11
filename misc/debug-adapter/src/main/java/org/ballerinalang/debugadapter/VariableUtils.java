/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter;

import com.sun.jdi.LocalVariable;

/**
 * Variable utils.
 */
public class VariableUtils {
    public static String getVarType(LocalVariable variable) {
        String balType;
        switch (variable.signature()) {
            case "J":
                balType = "Int";
                break;
            case "I":
                balType = "Byte";
                break;
            case "D":
                balType = "Float";
                break;
            case "Z":
                balType = "Boolean";
                break;
            case "Ljava/lang/String;":
                balType = "String";
                break;
            case "Lorg/ballerinalang/jvm/values/DecimalValue;":
                balType = "Decimal";
                break;
            case "Lorg/ballerinalang/jvm/values/MapValue;":
                balType = "Map";
                break;
            case "Lorg/ballerinalang/jvm/values/TableValue;":
                balType = "Table";
                break;
            case "Lorg/ballerinalang/jvm/values/StreamValue;":
                balType = "Stream";
                break;
            case "Lorg/ballerinalang/jvm/values/ArrayValue;":
                balType = "Array";
                break;
            case "Ljava/lang/Object;":
                balType = "Object";
                break;
            case "Lorg/ballerinalang/jvm/values/ErrorValue;":
                balType = "Error";
                break;
            case "Lorg/ballerinalang/jvm/values/FutureValue;":
                balType = "Future";
                break;
            case "Lorg/ballerinalang/jvm/values/FPValue;":
                balType = "Invokable";
                break;
            case "Lorg/ballerinalang/jvm/values/TypedescValue;":
                balType = "Desc";
                break;
            default:
                balType = "Object";
                break;
        }

        return balType;
    }
}
