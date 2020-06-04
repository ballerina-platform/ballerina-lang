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

type Teacher record {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

type IntRecord record{
    int a;
    int b;
};

type Person record {
    string name = "";
    int age = 0;
    Person? parent = ();
};

type Engineer record {
    string name = "";
    int age = 0;
    Engineer? parent = ();
};

//----------------------------Map Stamp -------------------------------------------------------------

function stampIntMapToRecord() returns IntRecord|error {
    map<int> m = { "a": 1, "b": 2 };
    IntRecord|error intRecord = m.cloneWithType(IntRecord);

    return intRecord;
}

function stampIntMapToJSON() returns json|error {
    map<int> m = { "a": 1, "b": 2 };
    json|error jsonValue = m.cloneWithType(json);

    return jsonValue;
}

function stampIntMapToAnydata() returns anydata|error {
    map<int> m = { "a": 1, "b": 2 };
    anydata|error anydataValue = m.cloneWithType(anydata);

    return anydataValue;
}

function stampIntMapToIntMap() returns map<int>|error {
    map<int> m = { "a": 1, "b": 2 };
    map<int>|error mapValue = m.cloneWithType(map<int>);

    return mapValue;
}

function stampIntMapToAnydataMap() returns map<anydata>|error {
    map<int> m = { "a": 1, "b": 2 };
    map<anydata>|error mapValue = m.cloneWithType(map<anydata>);

    return mapValue;
}


function stampAnydataMapToIntMap() returns map<int>|error {
    map<anydata> m = { "a": 1, "b": 2 };
    map<int>|error mapValue = m.cloneWithType(map<int>);

    return mapValue;
}


function stampAnydataMapToStringMap() returns map<string>|error {
    map<anydata> m = { firstName: "mohan", lastName: "raj" };
    map<string>|error mapValue = m.cloneWithType(map<string>);

    return mapValue;
}


function stampAnydataMapToStringMapWithoutExplicitConstraintType() returns map<string>|error {
    map<anydata> m = { firstName: "mohan", lastName: "raj" };
    map<string>|error mapValue = m.cloneWithType(map<string>);

    return mapValue;
}

function stampAnydataMapToRecord() returns Teacher|error {
    map<anydata> anydataMap = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher|error teacherValue = anydataMap.cloneWithType(Teacher);

    return teacherValue;
}

function stampAnydataMapToJSON() returns json|error {
    map<anydata> anydataMap = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    json|error jsonValue = anydataMap.cloneWithType(json);

    return jsonValue;
}

function stampAnydataMapToAnydata() returns anydata|error {
    map<anydata> anydataMap = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    anydata|error anydataValue = anydataMap.cloneWithType(anydata);

    return anydataValue;
}

function stampAnydataMapToSimilarOpenRecordMap() returns map<Employee>|error {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<anydata> teacherMap = { "a": p1, "b": p2 };
    map<Employee>|error mapValue = teacherMap.cloneWithType(map<Employee>);

    return mapValue;
}


function stampAnydataMapToRecordMap() returns map<Teacher>|error {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<anydata> teacherMap = { "a": p1, "b": p2 };
    map<Teacher>|error mapValue = teacherMap.cloneWithType(map<Teacher>);

    return mapValue;
}

function stampAnydataMapToJSONMap() returns map<json>|error {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<anydata> teacherMap = { "a": p1, "b": p2 };
    map<json>|error jsonValue = teacherMap.cloneWithType(map<json>);

    return jsonValue;
}


function stampRecordMapToAnydataMap() returns map<anydata>|error {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    map<anydata>|error mapValue = teacherMap.cloneWithType(map<anydata>);

    return mapValue;
}

function stampRecordMapToSimilarOpenRecordMap() returns map<Employee>|error {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    map<Employee>|error mapValue = teacherMap.cloneWithType(map<Employee>);

    return mapValue;
}

function stampRecordMapToJSONMap() returns map<json>|error {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    map<json>|error mapValue = teacherMap.cloneWithType(map<json>);

    return mapValue;

}

function stampJSONMapToRecordMap() returns map<Employee>|error {
    map<json> teacherMap = { "a": { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" },
        "b": { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" } };
    map<Employee>|error mapValue = teacherMap.cloneWithType(map<Employee>);

    return mapValue;
}

function stampRecordTypeMultiDimensionMap() returns map<map<Employee>>|error {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    map<map<Teacher>> multiMap = { "aa": teacherMap, "bb": teacherMap };

    map<map<Employee>>|error mapValue = multiMap.cloneWithType(map<map<Employee>>);

    return mapValue;
}

function stampAnydataToIntMultiDimensionMap() returns map<map<map<int>>>|error {
    map<map<map<anydata>>> m = { "a": { "aa": { "aa": 11, "bb": 22 }, "bb": { "aa": 11, "bb": 22 } }, "b": { "aaa": { "aa":
    11, "bb": 22 }, "bbb": { "aa": 11, "bb": 22 } } };
    map<map<map<int>>>|error mapValue = m.cloneWithType(map<map<map<int>>>);

    return mapValue;
}

function stampIntToAnydataMultiDimensionMap() returns map<map<map<anydata>>>|error {
    map<map<map<int>>> m = { "a": { "aa": { "aa": 11, "bb": 22 }, "bb": { "aa": 11, "bb": 22 } }, "b": { "aaa": { "aa":
    11, "bb": 22 }, "bbb": { "aa": 11, "bb": 22 } } };
    map<map<map<anydata>>>|error mapValue = m.cloneWithType(map<map<map<anydata>>>);

    return mapValue;
}

function stampConstraintMapToAnydata() returns anydata|error {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    anydata|error anydataValue = teacherMap.cloneWithType(anydata);

    return anydataValue;
}

function stampConstraintMapToUnion() returns map<Teacher>|xml|error {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
   map<Teacher>|xml|error anydataValue = teacherMap.cloneWithType(typedesc<map<Teacher>|xml>);

    return anydataValue;
}

//---------------------------------- Negative Test cases -----------------------------------------------------------

type EmployeeClosedRecord record {|
    string name;
    string status;
    string batch;
|};

function stampMapToRecordNegative() returns EmployeeClosedRecord|error {
    map<string> m = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };
    EmployeeClosedRecord|error employee = m.cloneWithType(EmployeeClosedRecord);

    return employee;
}

function testStampRecordToRecordWithCyclicValueReferences() returns Engineer|error {
    Person p = { name: "Waruna", age: 25, parent: () };
    Person p2 = { name: "Milinda", age: 25, parent:p };
    p.parent = p2;
    Engineer|error e =  trap p.cloneWithType(Engineer); // Cyclic value will be check with isLikeType method.
    return e;
}

function testStampRecordToJsonWithCyclicValueReferences() returns json|error {
    Person p = { name: "Waruna", age: 25, parent: () };
    Person p2 = { name: "Milinda", age: 25, parent:p };
    p.parent = p2;
    json|error j =  trap p.cloneWithType(json); // Cyclic value will be check with isLikeType method.
    return j;
}

function testStampRecordToMapWithCyclicValueReferences() returns map<anydata>|error {
    Person p = { name: "Waruna", age: 25, parent: () };
    Person p2 = { name: "Milinda", age: 25, parent:p };
    p.parent = p2;
    map<anydata>|error m =  trap p.clone().cloneWithType(map<anydata>); // Cyclic value will be check when stamping the value.
    return m;
}
