type Student record {
    string name;
    string status;
    string batch;
    string school;
    !...
};

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

type Teacher record {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

type NonAcademicStaff record {
    string name;
    int age;
    string status;
    string batch;
    string...
};

type AcademicStaff record {
    string name;
    string status;
    string batch;
    int...
};

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

type ExtendedEmployeeWithUnion record {
    string name;
    string status;
    string batch;
    map<anydata>|string address;
};

//-----------------------Record Stamp -------------------------------------------------------------------

function stampWithOpenRecords() returns Employee {
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    Employee e = Employee.stamp(t1);
    return e;
}

function stampWithOpenRecordsNonAssignable() returns Teacher {
    Employee e1 = { name: "Raja", status: "single", batch: "LK2014" };

    Teacher t = Teacher.stamp(e1);
    return t;
}

function stampClosedRecordWithOpenRecord() returns Employee {
    Person p1 = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };

    Employee e = Employee.stamp(p1);
    return e;
}

function stampRecordToJSON() returns json {

    Employee employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    //typedesc jsonType = json;
    json jsonValue = json.stamp(employee);

    return jsonValue;
}

function stampRecordToMap() returns map {

    Employee employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    map<anydata> mapValue = map<anydata>.stamp(employee);

    return mapValue;
}

function stampRecordToMapV2() returns map<string> {

    Employee employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    map<string> mapValue = map<string>.stamp(employee);

    return mapValue;
}

function stampRecordToMapV3() returns map {

    Employee employee = { name: "John", status: "single", batch: "LK2014" };
    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College", emp: employee
    };
    map<anydata> mapValue = map<anydata>.stamp(teacher);

    return mapValue;
}

function stampRecordToAnydata() returns anydata {
    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    anydata anydataValue = anydata.stamp(teacher);

    return anydataValue;
}

function stampFunctionReferenceWithOpenRecords() returns Employee {

    Employee e = Employee.stamp(getTeacherRecord());
    return e;
}

function getTeacherRecord() returns Teacher {
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    return t1;
}


function stampOpenRecordToTypeClosedRecord() returns NonAcademicStaff {
    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    NonAcademicStaff returnValue = NonAcademicStaff.stamp(teacher);

    return returnValue;
}

function stampExtendedRecordToOpenRecord() returns Employee {
    Address addressValue = {no: 23, streetName: "Palm Grove", city:"Colombo"};
    ExtendedEmployee extendedEmployee = { name: "Raja", status: "single", batch: "LK2014", address:addressValue};
    Employee employee = Employee.stamp(extendedEmployee);

    return employee;
}

function stampExtendedRecordToOpenRecordV2() returns ExtendedEmployeeWithMap {
    Address addressValue = {no: 23, streetName: "Palm Grove", city:"Colombo"};
    ExtendedEmployee extendedEmployee = { name: "Raja", status: "single", batch: "LK2014", address:addressValue};
    ExtendedEmployeeWithMap employee = ExtendedEmployeeWithMap.stamp(extendedEmployee);

    return employee;
}

function stampExtendedRecordToOpenRecordV3() returns ExtendedEmployeeWithUnion {
    Address addressValue = {no: 23, streetName: "Palm Grove", city:"Colombo"};
    ExtendedEmployee extendedEmployee = { name: "Raja", status: "single", batch: "LK2014", address:addressValue};
    ExtendedEmployeeWithUnion employee = ExtendedEmployeeWithUnion.stamp(extendedEmployee);

    return employee;
}

//-------------------------------- Negative Test cases ------------------------------------------------------------

function stampOpenRecordToMap() returns map<string> {

    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    map<string> mapValue = map<string>.stamp(teacher);

    return mapValue;
}

function stampOpenRecordToTypeClosedRecordNegative() returns NonAcademicStaff {
    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College", postalCode: 600};
    NonAcademicStaff returnValue = NonAcademicStaff.stamp(teacher);

    return returnValue;
}
