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

type PersonObj object {
    public int age = 10;
    public string name = "mohan";

    public int year = 2014;
    public string month = "february";
};

type EmployeeObj object {
    public int age = 10;
    public string name = "raj";

};

type Employee record {
    string name;
    int age;
};

//----------------------------Object Stamp Negative Test Cases -------------------------------------------------------------


function stampObjectsToRecord() returns Employee {
    PersonObj p = new PersonObj();
    Employee employee = Employee.stamp(p);

    return employee;
}


function stampObjectsToJSON() returns json {
    PersonObj p = new PersonObj();
    json jsonValue = json.stamp(p);

    return jsonValue;
}

function stampObjectsToXML() returns xml {
    PersonObj p = new PersonObj();
    xml xmlValue = xml.stamp(p);

    return xmlValue;
}

function stampObjectsToMap() returns map {
    PersonObj p = new PersonObj();
    map mapValue = map.stamp(p);

    return mapValue;
}

function stampObjectsToArray() returns any[] {
    PersonObj p = new PersonObj();
    any[] anyValue = any[].stamp(p);

    return anyValue;
}

function stampObjectsToTuple() returns (int,string) {
    PersonObj p = new PersonObj();
    (int, string) tupleValue = (int,string).stamp(p);

    return tupleValue;
}
