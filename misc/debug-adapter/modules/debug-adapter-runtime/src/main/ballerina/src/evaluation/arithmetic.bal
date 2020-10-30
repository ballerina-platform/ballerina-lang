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

function add(any a, any b) returns any|error {

    any|error result;
    if (a is int && b is int) {
        result = trap (a + b); // int + int
    } else if (a is float && b is float) {
        result = trap (a + b); // float + float
    } else if (a is decimal && b is decimal) {
        result = trap (a + b); // decimal + decimal
    } else if (a is int && b is float) {
        result = trap (a + b); // int + float
    } else if (a is float && b is int) {
        result = trap (a + b); // float + int
    } else if (a is int && b is decimal) {
        result = trap (a + b); // int + decimal
    } else if (a is decimal && b is int) {
        result = trap (a + b); // decimal + int
    } else if (a is float && b is decimal) {
        result = trap (a + b); // float + decimal
    } else if (a is decimal && b is float) {
        result = trap (a + b); // decimal + float
    } else if (a is string && b is string) {
        result = trap (a + b); // string + string
    } else {
        result = error("operator '+' not defined for '" + (typeof a).toBalString() + "' and '" + (typeof a)
                                        .toBalString() + "'.");
    }
    return result;
}

function substract(any a, any b) returns any|error {

    any|error result;
    if (a is int && b is int) {
        result = trap (a - b); // int - int
    } else if (a is float && b is float) {
        result = trap (a - b); // float - float
    } else if (a is decimal && b is decimal) {
        result = trap (a - b); // decimal - decimal
    } else if (a is int && b is float) {
        result = trap (a - b); // int - float
    } else if (a is float && b is int) {
        result = trap (a - b); // float - int
    } else if (a is int && b is decimal) {
        result = trap (a - b); // int - decimal
    } else if (a is decimal && b is int) {
        result = trap (a - b); // decimal - int
    } else if (a is float && b is decimal) {
        result = trap (a - b); // float - decimal
    } else if (a is decimal && b is float) {
        result = trap (a - b); // decimal - float
    } else {
        result = error("operator '-' not defined for '" + (typeof a).toBalString() + "' and '" + (typeof a)
                                        .toBalString() + "'.");
    }
    return result;
}

function multiply(any a, any b) returns any|error {

    any|error result;
    if (a is int && b is int) {
        result = trap (a * b); // int * int
    } else if (a is float && b is float) {
        result = trap (a * b); // float * float
    } else if (a is decimal && b is decimal) {
        result = trap (a * b); // decimal * decimal
    } else if (a is int && b is float) {
        result = trap (a * b); // int * float
    } else if (a is float && b is int) {
        result = trap (a * b); // float * int
    } else if (a is int && b is decimal) {
        result = trap (a * b); // int * decimal
    } else if (a is decimal && b is int) {
        result = trap (a * b); // decimal * int
    } else if (a is float && b is decimal) {
        result = trap (a * b); // float * decimal
    } else if (a is decimal && b is float) {
        result = trap (a * b); // decimal * float
    } else {
        result = error("operator '*' not defined for '" + (typeof a).toBalString() + "' and '" + (typeof a)
                                        .toBalString() + "'.");
    }
    return result;
}

function divide(any a, any b) returns any|error {

    any|error result;
    if (a is int && b is int) {
        result = trap (a / b); // int / int
    } else if (a is float && b is float) {
        result = trap (a / b); // float / float
    } else if (a is decimal && b is decimal) {
        result = trap (a / b); // decimal / decimal
    } else if (a is int && b is float) {
        result = trap (a / b); // int / float
    } else if (a is float && b is int) {
        result = trap (a / b); // float / int
    } else if (a is int && b is decimal) {
        result = trap (a / b); // int / decimal
    } else if (a is decimal && b is int) {
        result = trap (a / b); // decimal / int
    } else if (a is float && b is decimal) {
        result = trap (a / b); // float / decimal
    } else if (a is decimal && b is float) {
        result = trap (a / b); // decimal / float
    } else {
        result = error("operator '/' not defined for '" + (typeof a).toBalString() + "' and '" + (typeof a)
                                        .toBalString() + "'.");
    }
    return result;
}

function modulus(any a, any b) returns any|error {

    any|error result;
    if (a is int && b is int) {
        result = trap (a % b); // int % int
    } else if (a is float && b is float) {
        result = trap (a % b); // float % float
    } else if (a is decimal && b is decimal) {
        result = trap (a % b); // decimal % decimal
    } else if (a is int && b is float) {
        result = trap (a % b); // int % float
    } else if (a is float && b is int) {
        result = trap (a % b); // float % int
    } else if (a is int && b is decimal) {
        result = trap (a % b); // int % decimal
    } else if (a is decimal && b is int) {
        result = trap (a % b); // decimal % int
    } else {
        result = error("operator '%' not defined for '" + (typeof a).toBalString() + "' and '" + (typeof a)
                                .toBalString() + "'.");
    }
    return result;
}
