// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//

function getType(any value) returns string|error {
    // Need to handle simple values separately, since `typeof` operation returns the singleton type for simple values.
    if value is () {
        return "nil";
    } else if value is int {
        return "int";
    } else if value is float {
        return "float";
    } else if value is decimal {
        return "decimal";
    } else if value is boolean {
        return "boolean";
    } else if value is byte {
        return "byte";
    } else if value is string {
        return "string";
    } else {
        var result = trap (typeof value);
        if (result is typedesc) {
            string typeString = result.toString();
            return typeString.startsWith("typedesc ") ? typeString.substring(9) : typeString;
        } else {
            return result;
        }
    }
}

function getTrapResult(any|error value) returns any|error {
    return trap value;
}
