/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.core.model.types;

/**
 * @since 0.87
 */
public class TypeSignature {
    public static final String SIG_INT = "I";
    public static final String SIG_BYTE = "W";
    public static final String SIG_FLOAT = "F";
    public static final String SIG_DECIMAL = "L";
    public static final String SIG_STRING = "S";
    public static final String SIG_BOOLEAN = "B";
    public static final String SIG_REFTYPE = "R";
    public static final String SIG_JSON = "J";
    public static final String SIG_TABLE = "D";
    public static final String SIG_FUTURE = "X";
    public static final String SIG_STREAM = "H";
    public static final String SIG_MAP = "M";
    public static final String SIG_CONNECTOR = "C";
    public static final String SIG_STRUCT = "T";
    public static final String SIG_FUNCTION = "U";
    public static final String SIG_ARRAY = "[";
    public static final String SIG_ANY = "A";
    public static final String SIG_ANYDATA = "K";
    public static final String SIG_TYPEDESC = "Y";
    public static final String SIG_VOID = "V";
    public static final String SIG_ANNOTATION = "@";
    public static final String SIG_UNION = "O";
    public static final String SIG_NULL = "N";
    public static final String SIG_TUPLE = "P";
    public static final String SIG_FINITE_TYPE = "G";
    public static final String SIG_CHANNEL = "Q";
}
