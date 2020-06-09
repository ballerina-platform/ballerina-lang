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

type Person record {|
    string name;
    string status;
    string batch;
    string school;
|};

//-----------------------Tuple Type Stamp Negative Test cases----------------------------------------------------------

function stampTupleToRecord() returns Employee|error {
    [string, string, string] tupleValue = ["Mohan", "single", "LK2014"];

    Employee|error returnValue = tupleValue.cloneWithType(Employee);
    return returnValue;
}

function stampTupleToJSON() returns json|error {
    [string, string, string] tupleValue = ["Mohan", "single", "LK2014"];

    json|error jsonValue = tupleValue.cloneWithType(json);
    return jsonValue;
}

function stampTupleToXML() returns xml|error {
    [string, string, string] tupleValue = ["Mohan", "single", "LK2014"];

    xml|error xmlValue = tupleValue.cloneWithType(xml);
    return xmlValue;
}

function stampTupleToMap() returns map<anydata>|error {
    [string, string, string] tupleValue = ["Mohan", "single", "LK2014"];

    map<anydata>|error mapValue = tupleValue.cloneWithType(map<anydata>);
    return mapValue;
}
