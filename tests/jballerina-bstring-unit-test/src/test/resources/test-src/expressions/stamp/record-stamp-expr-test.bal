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

type Student record {|
    string name;
    string status;
    string batch;
    string school;
|};

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

type Teacher record {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

type NonAcademicStaff record {|
    string name;
    int age;
    string status;
    string batch;
    string...;
|};

type AcademicStaff record {|
    string name;
    string status;
    string batch;
    int...;
|};

type ExtendedEmployee record {
    string name;
    string status;
    string batch;
    Address address;
};

type Address record {
    int no;
    string streetName;
    string city;
};

type ExtendedEmployeeWithMap record {
    string name;
    string status;
    string batch;
    map<anydata> address;
};

type ExtendedEmployeeWithRecord record {
    string name;
    string status;
    string batch;
    Address|string address;
};

type ExtendedEmployeeWithUnion record {
    string name;
    string status;
    string batch;
    map<anydata>|string address;
};

type ExtendedEmployeeWithUnionRest record {|
    string name;
    string status;
    string batch;
    map<anydata>|string...;
|};

//-----------------------Record Stamp -------------------------------------------------------------------

function stampWithOpenRecords() returns Employee|error {
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    Employee|error e = Employee.constructFrom(t1);
    return e;
}

function stampWithOpenRecordsNonAssignable() returns Teacher|Employee {
    Employee e1 = { name: "Raja", "age": 25, status: "single", batch: "LK2014", "school": "Hindu College" };

    Teacher|error t = Teacher.constructFrom(e1);
    if (t is Teacher) {
        return t;
    }

    return e1;
}

function stampClosedRecordWithOpenRecord() returns Employee|error {
    Person p1 = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };

    Employee|error e = Employee.constructFrom(p1);
    return e;
}

function stampClosedRecordWithClosedRecord() returns Student|error {
    Person p1 = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };

    Student|error e = Student.constructFrom(p1);
    return e;
}

function stampRecordToJSON() returns json|error {
    Employee employee = { name: "John", status: "single", batch: "LK2014", "school": "Hindu College" };

    json|error jsonValue = json.constructFrom(employee);

    return jsonValue;
}

function stampRecordToMap() returns map<anydata>|error {
    Employee employee = { name: "John", status: "single", batch: "LK2014", "school": "Hindu College" };
    map<anydata>|error mapValue = map<anydata>.constructFrom(employee);

    return mapValue;
}

function stampRecordToMapV2() returns map<string>|error {
    Employee employee = { name: "John", status: "single", batch: "LK2014", "school": "Hindu College" };
    map<string>|error mapValue = map<string>.constructFrom(employee);

    return mapValue;
}

function stampRecordToMapV3() returns map<anydata>|error {
    Employee employee = { name: "John", status: "single", batch: "LK2014" };
    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College",
                            "emp": employee };
    map<anydata>|error mapValue = map<anydata>.constructFrom(teacher);

    return mapValue;
}

function stampRecordToAnydata() returns anydata|error {
    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    anydata|error anydataValue = anydata.constructFrom(teacher);

    return anydataValue;
}

function stampFunctionReferenceWithOpenRecords() returns Employee|error {
    Employee|error e = Employee.constructFrom(getTeacherRecord());
    return e;
}

function getTeacherRecord() returns Teacher {
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    return t1;
}

function stampFunctionReferenceWithArgs() returns Employee|error {
    Employee|error e = Employee.constructFrom(getTeacherRecordWithArgs(23));
    return e;
}

function getTeacherRecordWithArgs(int i) returns Teacher {
    Teacher t1 = { name: "Raja", age: i, status: "single", batch: "LK2014", school: "Hindu College" };
    return t1;
}

function stampOpenRecordToTypeClosedRecord() returns NonAcademicStaff|error {
    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    NonAcademicStaff|error returnValue = NonAcademicStaff.constructFrom(teacher);

    return returnValue;
}

function stampExtendedRecordToOpenRecord() returns Employee|error {
    Address addressValue = { no: 23, streetName: "Palm Grove", city: "Colombo" };
    ExtendedEmployee extendedEmployee = { name: "Raja", status: "single", batch: "LK2014", address: addressValue };

    Employee|error employee = Employee.constructFrom(extendedEmployee);

    return employee;
}

function stampExtendedRecordToOpenRecordV2() returns ExtendedEmployeeWithMap|error {
    Address addressValue = { no: 23, streetName: "Palm Grove", city: "Colombo" };
    ExtendedEmployee extendedEmployee = { name: "Raja", status: "single", batch: "LK2014", address: addressValue };

    ExtendedEmployeeWithMap|error employee = ExtendedEmployeeWithMap.constructFrom(extendedEmployee);

    return employee;
}

function stampExtendedRecordToOpenRecordV3() returns ExtendedEmployeeWithRecord|error {
    Address addressValue = { no: 23, streetName: "Palm Grove", city: "Colombo" };
    ExtendedEmployee extendedEmployee = { name: "Raja", status: "single", batch: "LK2014", address: addressValue };

    ExtendedEmployeeWithRecord|error employee = ExtendedEmployeeWithRecord.constructFrom(extendedEmployee);

    return employee;
}

function stampExtendedRecordToOpenRecordV4() returns ExtendedEmployee|error {
    map<anydata> addressValue = { no: 23, streetName: "Palm Grove", city: "Colombo" };
    ExtendedEmployeeWithMap extendedWithMap = { name: "Raja", status: "single", batch: "LK2014", address: addressValue }
    ;

    ExtendedEmployee|error employee = ExtendedEmployee.constructFrom(extendedWithMap);

    return employee;
}

function stampExtendedRecordToOpenRecordV5() returns ExtendedEmployee|error {
    map<anydata> addressValue = { no: 23, streetName: "Palm Grove", city: "Colombo" };
    ExtendedEmployeeWithUnion extendedEmployee = { name: "Raja", status: "single", batch: "LK2014", address:
    addressValue };

    ExtendedEmployee|error employee = ExtendedEmployee.constructFrom(extendedEmployee);

    return employee;
}

function stampExtendedRecordToOpenRecordV6() returns ExtendedEmployeeWithUnionRest|error {
    map<anydata> addressValue = { no: 23, streetName: "Palm Grove", city: "Colombo" };
    Employee employee = { name: "Raja", status: "single", batch: "LK2014", "address": addressValue };

    ExtendedEmployeeWithUnionRest|error outputValue = ExtendedEmployeeWithUnionRest.constructFrom(employee);

    return outputValue;
}

function stampExtendedRecordToRecordWithUnionV7() returns ExtendedEmployeeWithRecord|error {
    map<anydata> addressValue = { no: 23, streetName: "Palm Grove", city: "Colombo" };
    ExtendedEmployeeWithMap extendedWithMap =
    { name: "Raja", status: "single", batch: "LK2014", address: addressValue };

    ExtendedEmployeeWithRecord|error employee = ExtendedEmployeeWithRecord.constructFrom(extendedWithMap);

    return employee;
}

type OpenEmployee record {
    string name;
    int age;
    string status;
};

type TeacherWithAnyRestType record {|
    string name;
    int age;
    string status;
    string batch;
    string school;
    anydata...;
|};

function stampAnyRecordToRecord() returns OpenEmployee|error {

    TeacherWithAnyRestType p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    OpenEmployee|error e1 = OpenEmployee.constructFrom(p1);

    return e1;
}

//--------------------------------- Nil type related scenarios ---------------------------------------------

type ExtendedEmployeeWithNilMap record {
    string name;
    string status;
    string batch;
    map<anydata>? address;
};

type ExtendedEmployeeWithNilRecord record {
    string name;
    string status;
    string batch;
    Address? address;
};

function stampRecordToRecordWithNilValues() returns ExtendedEmployeeWithNilRecord|error {
    ExtendedEmployeeWithNilMap extendedWithMap = { name: "Raja", status: "single", batch: "LK2014", address: () };

    ExtendedEmployeeWithNilRecord|error employee = ExtendedEmployeeWithNilRecord.constructFrom(extendedWithMap);

    return employee;
}

function stampNilTypeToOpenRecord() returns Employee|error {
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    Employee|error e = Employee.constructFrom(t1);
    return e;
}

type EmployeeWithNil record {
    string name;
    string status;
    string batch;
    string? school;
};

type TeacherWithNil record {
    string name;
    string status;
    string batch;
    string? school;
};

function stampRecordWithNilValues() returns Employee|error {
    TeacherWithNil t1 = { name: "Raja", status: "single", batch: "LK2014", school: () };

    Employee|error e = Employee.constructFrom(t1);
    return e;
}

function stampRecordWithNilValuesV2() returns Employee|error {
    TeacherWithNil t1 = { name: "Raja", status: "single", batch: "LK2014", school: () };

    EmployeeWithNil|error e = EmployeeWithNil.constructFrom(t1);
    return e;
}

type ComplexPerson record {|
    string name = "";
    int age = 0;
    ComplexPerson? parent = ();
    json info?;
    map<anydata>? address?;
    int[][]? marks?;
    anydata a = ();
    float score = 0.0;
    boolean alive = false;
    ComplexPerson[]? children?;
|};

function stampComplexRecordToJSON() returns map<anydata>|error {
    int[] value = [67, 38, 91];
    int[] value2 = [55, 33, 44];


    ComplexPerson p = { name: "Child",
        age: 25,
        parent: { name: "Parent", age: 50 },
        address: { "city": "Colombo", "country": "SriLanka" },
        info: { status: "single" },
        marks: [value, value2]
    };

    map<anydata>|error m = map<anydata>.constructFrom(p);
    return m;
}

//------------------------------- Optional field related scenarios ----------------------------------------------

type TeacherWithOptionalField record {
    string name;
    int age?;
    string status;
    string batch;
    string school?;
};

function stampRecordToRecordWithOptionalFields() returns TeacherWithOptionalField|error {
    Employee emp = { name: "Raja", status: "single", batch: "LK2014" };

    TeacherWithOptionalField|error teacher = TeacherWithOptionalField.constructFrom(emp);
    return teacher;
}

//-------------------------------- Negative Test cases ------------------------------------------------------------

function stampOpenRecordToMap() returns map<string>|error {
    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    map<string>|error mapValue = map<string>.constructFrom(teacher);

    return mapValue;
}

function stampOpenRecordToTypeClosedRecordNegative() returns NonAcademicStaff|error {
    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College", "postalCode":
    600 };
    NonAcademicStaff|error returnValue = NonAcademicStaff.constructFrom(teacher);

    return returnValue;
}

function stampWithOpenRecordsNonAssignableNegative() returns Teacher|error {
    Employee e1 = { name: "Raja", status: "single", batch: "LK2014" };

    Teacher|error t = Teacher.constructFrom(e1);
    return t;
}

function stampOpenRecordWithInvalidValues() returns Teacher|error {
    Employee e1 = { name: "Raja", "age": 25, status: "single", batch: "LK2014", "school": 789 };

    Teacher|error t = Teacher.constructFrom(e1);

    return t;
}
