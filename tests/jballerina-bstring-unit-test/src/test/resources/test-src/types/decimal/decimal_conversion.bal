// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

// Decimal to other types (int, float, string, boolean, any, json) conversion.
import ballerina/lang.'decimal as decimals;

function testDecimalToOtherTypesConversion() returns [int, float, string, any, json] {
    decimal d = 23.456;
    int i = <int> d;
    float f = <float> d;
    string s = d.toString();
    any a = d;
    json j = d;

    return [i, f, s, a, j];
}

// Other types (int, float, string) to decimal conversion.
function testOtherTypesToDecimalConversion() returns [decimal, decimal, decimal]|error {
    int i = 12;
    decimal d1 = <decimal> i;

    float f = -12.34;
    decimal d2 = <decimal> f;

    string s = "23.456";
    decimal d3 = check decimals:fromString(s);

    return [d1, d2, d3];
}
