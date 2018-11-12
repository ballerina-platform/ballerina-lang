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

type Address object {
    public int no = 10;
    public string streetName = "Palm Grove";
    public string city = "colombo";
};

//-----------------------Record Stamp -------------------------------------------------------------------

function stampWithOpenRecords() returns Employee {
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    Employee e = t1.stamp(Employee);
    return e;
}

function stampWithOpenRecordsNonAssignable() returns Teacher {
    Employee e1 = { name: "Raja", status: "single", batch: "LK2014" };

    Teacher t = e1.stamp(Teacher);
    return t;
}

function stampClosedRecordWithOpenRecord() returns Employee {
    Person p1 = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };

    Employee e = p1.stamp(Employee);
    return e;
}

function stampRecordToAny() returns any {
    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    any anyValue = teacher.stamp(any);

    return anyValue;
}

function stampRecordToJSON() returns json {

    Employee employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    json jsonValue = employee.stamp(json);

    return jsonValue;
}

function stampRecordToMap() returns map {

    Employee employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    map mapValue = employee.stamp(map);

    return mapValue;
}

function stampRecordToMapV2() returns map<string> {

    Employee employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    map<string> mapValue = employee.stamp(map<string>);

    return mapValue;
}

function stampRecordToMapV3() returns map {

    Employee employee = { name: "John", status: "single", batch: "LK2014" };
    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College", emp: employee
    };
    map mapValue = teacher.stamp(map);

    return mapValue;
}

function stampRecordToAnydata() returns anydata {
    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    anydata anydataValue = teacher.stamp(anydata);

    return anydataValue;
}

function stampExtendedRecordToAny() returns any {
    Address addressObj = new Address();
    ExtendedEmployee employee = { name: "Raja", status: "single", batch: "LK2014", address:addressObj};
    any anyValue = employee.stamp(any);

    return anyValue;
}

function stampFunctionReferenceWithOpenRecords() returns Employee {

    Employee e = getTeacherRecord().stamp(Employee);
    return e;
}

function getTeacherRecord() returns Teacher {
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    return t1;
}


//function stampOpenRecordToTypeClosedRecord() returns NonAcademicStaff {
//    //Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
//    //NonAcademicStaff returnValue = teacher.stamp(NonAcademicStaff);
//
//    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
//    NonAcademicStaff returnValue = teacher;
//
//    return returnValue;
//}

//-------------------------------- Negative Test cases ------------------------------------------------------------

function stampOpenRecordToMap() returns map<string> {

    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    map<string> mapValue = teacher.stamp(map<string>);

    return mapValue;
}

function stampExtendedRecordToOpenRecord() returns Employee {
    Address addressObj = new Address();
    ExtendedEmployee extendedEmployee = { name: "Raja", status: "single", batch: "LK2014", address:addressObj};
    Employee employee = extendedEmployee.stamp(Employee);

    return employee;
}

//function stampNegativeOpenRecordToTypeClosedRecord() returns AcademicStaff {
//    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
//    AcademicStaff returnValue = teacher.stamp(AcademicStaff);
//
//    return returnValue;
//}



