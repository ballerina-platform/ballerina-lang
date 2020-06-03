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

type EmployeeObject object {
    string name = "Mohan";
    string status = "Single";
    string batch = "LK2014";
};

//----------------------------Array Stamp Negative Test cases -------------------------------------------------------------

function stampAnyArrayToRecord() returns Employee|error {

    anydata[] anyArray = ["Mohan", "Single", "LK2014"];
    Employee|error employee = anyArray.cloneWithType(Employee);

    return employee;
}

function stampAnyArrayToXML() returns xml|error {

    anydata[] anyArray = ["Mohan", "Single", "LK2014"];
    xml|error xmlValue = anyArray.cloneWithType(xml);

    return xmlValue;
}
