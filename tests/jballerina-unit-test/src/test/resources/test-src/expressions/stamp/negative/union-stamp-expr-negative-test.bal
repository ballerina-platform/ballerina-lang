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

type Employee record {
    string name;
    string status;
    string batch;
};

//-----------------------Union Type Stamp Negative Test cases --------------------------------------------------

function stampUnionToXML() returns Employee|error {
    int|float|xml unionVar = xml `<book>The Lost World</book>`;

    Employee|error employeeValue = unionVar.cloneWithType(Employee);
    return employeeValue;
}

function stampUnionToConstraintMapToUnionNegative() returns int|float|decimal|[string, int]|error  {
    int|float|[string, string] unionVar = 2;
    float|decimal|[string, int]|error  tupleValue = unionVar.cloneWithType(typedesc<float|decimal|[string, int]>);

    return tupleValue;
}

