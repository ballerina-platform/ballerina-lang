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

//----------------------------Map Stamp -------------------------------------------------------------

function stampIntMapToRecord() returns IntRecord {
    map<int> m = { "a": 1, "b": 2 };
    IntRecord intRecord = m.stamp(IntRecord);

    return intRecord;
}

function stampIntMapToJSON() returns json {
    map<int> m = { "a": 1, "b": 2 };
    json jsonValue = m.stamp(json);

    return jsonValue;
}

function stampIntMapToAny() returns any {
    map<int> m = { "a": 1, "b": 2 };
    any anyValue = m.stamp(any);

    return anyValue;
}

function stampIntMapToIntMap() returns map<int> {
    map<int> m = { "a": 1, "b": 2 };
    map<int> mapValue = m.stamp(map<int>);

    return mapValue;
}

function stampIntMapToAnyMap() returns map<any> {
    map<int> m = { "a": 1, "b": 2 };
    map<any> mapValue = m.stamp(map<any>);

    return mapValue;
}


function stampAnyMapToIntMap() returns map<int> {
    map<any> m = { "a": 1, "b": 2 };
    map<int> mapValue = m.stamp(map<int>);

    return mapValue;
}


function stampAnyMapToStringMap() returns map<string> {
    map<any> m = { firstName: "mohan", lastName: "raj" };
    map<string> mapValue = m.stamp(map<string>);

    return mapValue;
}


function stampAnyMapToStringMapWithoutExplicitConstraintType() returns map<string> {
    map<any> m = { firstName: "mohan", lastName: "raj" };
    map<string> mapValue = m.stamp(map<string>);

    return mapValue;
}

function stampAnyMapToRecord() returns Teacher {
    map anyMap = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher teacherValue = anyMap.stamp(Teacher);

    return teacherValue;
}

function stampAnyMapToJSON() returns json {
    map anyMap = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    json jsonValue = anyMap.stamp(json);

    return jsonValue;
}

function stampAnyMapToAny() returns any {
    map anyMap = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    any anyValue = anyMap.stamp(any);

    return anyValue;
}

function stampAnyMapToSimilarOpenRecordMap() returns map<Employee> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<any> teacherMap = { "a": p1, "b": p2 };
    map<Employee> mapValue = teacherMap.stamp(map<Employee>);

    return mapValue;
}


function stampAnyMapToRecordMap() returns map<Teacher> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<any> teacherMap = { "a": p1, "b": p2 };
    map<Teacher> mapValue = teacherMap.stamp(map<Teacher>);

    return mapValue;
}

function stampAnyMapToJSONMap() returns map<json> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<any> teacherMap = { "a": p1, "b": p2 };
    map<json> jsonValue = teacherMap.stamp(map<json>);

    return jsonValue;
}


function stampRecordMapToAnyMap() returns map<any> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    map<any> mapValue = teacherMap.stamp(map<any>);

    return mapValue;
}

function stampRecordMapToSimilarOpenRecordMap() returns map<Employee> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    map<Employee> mapValue = teacherMap.stamp(map<Employee>);

    return mapValue;
}

function stampRecordMapToJSONMap() returns map<json> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    map<json> mapValue = teacherMap.stamp(map<json>);

    return mapValue;

}

function stampJSONMapToRecordMap() returns map<Employee> {
    map<json> teacherMap = { "a": { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" },
        "b": { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" } };
    map<Employee> mapValue = teacherMap.stamp(map<Employee>);

    return mapValue;
}

function stampRecordTypeMultiDimensionMap() returns map<map<Employee>> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    map<map<Teacher>> multiMap = { "aa": teacherMap, "bb": teacherMap };

    map<map<Employee>> mapValue = multiMap.stamp(map<map<Employee>>);

    return mapValue;
}

function stampAnyToIntMultiDimensionMap() returns map<map<map<int>>> {
    map<map<map<any>>> m = { "a": { "aa": { "aa": 11, "bb": 22 }, "bb": { "aa": 11, "bb": 22 } }, "b": { "aaa": { "aa":
    11, "bb": 22 }, "bbb": { "aa": 11, "bb": 22 } } };
    map<map<map<int>>> mapValue = m.stamp(map<map<map<int>>>);

    return mapValue;
}

function stampIntToAnyMultiDimensionMap() returns map<map<map<any>>> {
    map<map<map<int>>> m = { "a": { "aa": { "aa": 11, "bb": 22 }, "bb": { "aa": 11, "bb": 22 } }, "b": { "aaa": { "aa":
    11, "bb": 22 }, "bbb": { "aa": 11, "bb": 22 } } };
    map<map<map<any>>> mapValue = m.stamp(map<map<map<any>>>);

    return mapValue;
}

function stampConstraintMapToAnydata() returns anydata {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    anydata anydataValue = teacherMap.stamp(anydata);

    return anydataValue;
}

//---------------------------------- Negative Test cases -----------------------------------------------------------


type EmployeeClosedRecord record {
    string name;
    string status;
    string batch;
    !...
};

function stampIntMapToStringMap() returns map<string> {
    map<int> m = { "a": 1, "b": 2 };
    map<string> mapValue = m.stamp(map<string>);

    return mapValue;
}

function stampMapToRecordNegative() returns EmployeeClosedRecord {
    map<string> m = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };
    EmployeeClosedRecord employee = m.stamp(EmployeeClosedRecord);

    return employee;
}
