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
    int age;
    float salary;
};

type Person record {
    string name;
    int age;
};

type ExtendedEmployee record {
    string name;
    string status;
    string batch;
    Address address;
};

type Address object {
    public int no = 10;
    public string streetName = "Palm Grove";
    public string city = "colombo";
};

type EmployeeObject object {
    string name = "Mohan";
    string status = "Single";
    string batch = "LK2014";
};

function seaWithInvalidNoOrParameters() returns json {

    json jsonValue = [1, false, null, "foo", { first: "John", last: "Pala" }];
    json returnValue = jsonValue.cloneWithType(typedesc<json>, 34);

    return returnValue;
}

function stampStringValueToJson() returns json|error {
    string value = "mohan";
    json|error jsonValue = value.cloneWithType(json);

    return jsonValue;
}

function stampStringValueToAny() returns any|error {
    string[] stringArray = ["mohan", "mike"];
    any|error anyValue = stringArray.cloneWithType(any);

    return anyValue;
}

function stampAnyToString() returns string?|error {
    any value = "mohan";
    string?|error stringValue = value.cloneWithType(string);

    return stringValue;
}

function seaWithInvalidTypedesc() returns json|error {

    json jsonValue = [1, false, null, "foo", { first: "John", last: "Pala" }];
    json|error returnValue = jsonValue.cloneWithType(TestType);

    return returnValue;
}

function stampAnyArrayToObject() returns EmployeeObject|error {

    anydata[] anyArray = ["Mohan", "Single", "LK2014"];
    EmployeeObject|error objectValue = anyArray.cloneWithType(EmployeeObject);

    return objectValue;
}

function stampAnyArrayToMap() returns map<any>|error {

    anydata[] anyArray = ["Mohan", "Single", "LK2014"];
    map<any>|error mapValue = anyArray.cloneWithType(map<any>);

    return mapValue;
}

function stampExtendedRecordToAnydata() returns anydata|error {
    Address addressObj = new Address();
    ExtendedEmployee employee = { name: "Raja", status: "single", batch: "LK2014", address:addressObj};
    anydata|error anydataValue = employee.cloneWithType(anydata);

    return anydataValue;
}

