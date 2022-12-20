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

function unaryPlus(any value) returns int|float|decimal|error {
    // Todo - Add proper syntax for for int and float, after fixing runtime exception.
    int|float|decimal|error result;
    if value is int {
        result = value;
    } else if value is float {
        result = value;
    } else if value is decimal {
        result = trap +value;
    } else {
        result = error("operator '+' not defined for '" + check getType(value) + "'");
    }
    return result;
}

function unaryMinus(any value) returns int|float|decimal|error {
    int|float|decimal|error result;
    if value is int {
        result = trap -value;
    } else if value is float {
        result = trap -value;
    } else if value is decimal {
        result = trap -value;
    } else {
        result = error("operator '-' not defined for '" + check getType(value) + "'");
    }
    return result;
}

function unaryInvert(any value) returns int|error {
    int|error result;
    if value is int {
        result = trap ~value;
    } else {
        result = error("operator '~' not defined for '" + check getType(value) + "'");
    }
    return result;
}

function unaryNot(any value) returns boolean|error {
    boolean|error result;
    if value is boolean {
        result = trap !value;
    } else {
        result = error("operator '!' not defined for '" + check getType(value) + "'");
    }
    return result;
}
