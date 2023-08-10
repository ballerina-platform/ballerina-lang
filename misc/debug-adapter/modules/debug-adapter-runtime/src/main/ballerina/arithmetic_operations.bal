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

function add(any lhs, any rhs) returns int|float|decimal|string|xml|error {

    int|float|decimal|string|xml|error result;
    if lhs is int && rhs is int {
        result = trap (lhs + rhs); // int + int
    } else if lhs is float && rhs is float {
        result = trap (lhs + rhs); // float + float
    } else if lhs is decimal && rhs is decimal {
        result = trap (lhs + rhs); // decimal + decimal
    } else if lhs is string && rhs is string {
        result = trap (lhs + rhs); // string + string
    } else if lhs is xml && rhs is xml {
        result = trap (lhs + rhs); // xml + xml
    } else {
        result = error("operator '+' not defined for '" + check getType(lhs) + "' and '" + check getType(rhs) + "'.");
    }
    return result;
}

function subtract(any lhs, any rhs) returns int|float|decimal|error {

    int|float|decimal|error result;
    if lhs is int && rhs is int {
        result = trap (lhs - rhs); // int - int
    } else if lhs is float && rhs is float {
        result = trap (lhs - rhs); // float - float
    } else if lhs is decimal && rhs is decimal {
        result = trap (lhs - rhs); // decimal - decimal
    } else {
        result = error("operator '-' not defined for '" + check getType(lhs) + "' and '" + check getType(rhs) + "'.");
    }
    return result;
}

function multiply(any lhs, any rhs) returns int|float|decimal|error {

    int|float|decimal|error result;
    if lhs is int && rhs is int {
        result = trap (lhs * rhs); // int * int
    } else if lhs is float && rhs is float {
        result = trap (lhs * rhs); // float * float
    } else if lhs is decimal && rhs is decimal {
        result = trap (lhs * rhs); // decimal * decimal
    } else {
        result = error("operator '*' not defined for '" + check getType(lhs) + "' and '" + check getType(rhs) + "'.");
    }
    return result;
}

function divide(any lhs, any rhs) returns int|float|decimal|error {

    int|float|decimal|error result;
    if lhs is int && rhs is int {
        result = trap (lhs / rhs); // int / int
    } else if lhs is float && rhs is float {
        result = trap (lhs / rhs); // float / float
    } else if lhs is decimal && rhs is decimal {
        result = trap (lhs / rhs); // decimal / decimal
    } else {
        result = error("operator '/' not defined for '" + check getType(lhs) + "' and '" + check getType(rhs) + "'.");
    }
    return result;
}

function modulus(any lhs, any rhs) returns int|float|decimal|error {

    int|float|decimal|error result;
    if lhs is int && rhs is int {
        result = trap (lhs % rhs); // int % int
    } else if lhs is float && rhs is float {
        result = trap (lhs % rhs); // float % float
    } else if lhs is decimal && rhs is decimal {
        result = trap (lhs % rhs); // decimal % decimal
    } else {
        result = error("operator '%' not defined for '" + check getType(lhs) + "' and '" + check getType(rhs) + "'.");
    }
    return result;
}
