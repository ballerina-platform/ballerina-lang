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

type Person record {
    string name;
    string status;
    string batch;
    string school;
    !...
};

type EmployeeObj object {
    public string name = "Mohan";
    public int age = 30;

};

//-----------------------Tuple Type Stamp Negative Test cases----------------------------------------------------------

function stampTupleToRecord() returns Employee {
    (string, string, string) tupleValue = ("Mohan", "single", "LK2014");

    Employee returnValue = Employee.stamp(tupleValue);
    return returnValue;
}

function stampTupleToJSON() returns json {
    (string, string, string) tupleValue = ("Mohan", "single", "LK2014");

    json jsonValue = json.stamp(tupleValue);
    return jsonValue;
}

function stampTupleToXML() returns xml {
    (string, string, string) tupleValue = ("Mohan", "single", "LK2014");

    xml xmlValue = xml.stamp(tupleValue);
    return xmlValue;
}

function stampTupleToObject() returns EmployeeObj {
    (string, int) tupleValue = ("Mohan", 30);

    EmployeeObj objectValue = EmployeeObj.stamp(tupleValue);
    return objectValue;
}

function stampTupleToMap() returns map {
    (string, string, string) tupleValue = ("Mohan", "single", "LK2014");

    map mapValue = map.stamp(tupleValue);
    return mapValue;
}

function stampTupleToArray() returns string[] {
    (string, string, string) tupleValue = ("Mohan", "single", "LK2014");

    string[] arrayValue = string[].stamp(tupleValue);
    return arrayValue;
}

