// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/test;

(anydata|error)[] anydataOrErrorArray = [a, b, errValOne, c];
map<anydata|error> anydataOrErrorMap = {
    one: a,
    two: b,
    three: c,
    four: errValOne,
    five: errValTwo
};

// anydata-type-descriptor := anydata

// The type descriptor anydata describes the type of all pure values other than errors. 
// The type anydata contains a shape if and only if the shape is pure and is not the shape of an error value. 

// Thus the type anydata|error is the supertype of all pure types. The type anydata is equivalent to the union

// () | boolean | int | float | decimal | string
// | (anydata|error)[] | map<anydata|error>
// | xml | table
@test:Config {
    dataProvider: "anydataValueDataProvider"
}
function testAnydataTypeDescriptor(anydata av) {
    anydata av2 = av;
    test:assertEquals(av2, av, msg = "expected variable to hold the assigned value");
}

function anydataValueDataProvider() returns anydata[][] {
    return [
        [a],
        [b],
        [c],
        [d],
        [e],
        [f],
        [k],
        [l],
        [anydataOrErrorArray],
        [anydataOrErrorMap]
    ];
}
