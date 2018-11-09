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

//----------------------------Map Seal -------------------------------------------------------------

function sealIntMapToRecord() returns IntRecord {
    map<int> m = { "a": 1, "b": 2 };
    IntRecord intRecord = m.seal(IntRecord);

    return intRecord;
}

function sealIntMapToJSON() returns json {
    map<int> m = { "a": 1, "b": 2 };
    json jsonValue = m.seal(json);

    return jsonValue;
}

function sealIntMapToAny() returns any {
    map<int> m = { "a": 1, "b": 2 };
    any anyValue = m.seal(any);

    return anyValue;
}

function sealIntMapToIntMap() returns map<int> {
    map<int> m = { "a": 1, "b": 2 };
    map<int> mapValue = m.seal(map<int>);

    return mapValue;
}

function sealIntMapToAnyMap() returns map<any> {
    map<int> m = { "a": 1, "b": 2 };
    map<any> mapValue = m.seal(map<any>);

    return mapValue;
}


function sealAnyMapToIntMap() returns map<int> {
    map<any> m = { "a": 1, "b": 2 };
    map<int> mapValue = m.seal(map<int>);

    return mapValue;
}


function sealAnyMapToStringMap() returns map<string> {
    map<any> m = { firstName: "mohan", lastName: "raj" };
    map<string> mapValue = m.seal(map<string>);

    return mapValue;
}


function sealAnyMapToStringMapWithoutExplicitConstraintType() returns map<string> {
    map<any> m = { firstName: "mohan", lastName: "raj" };
    map<string> mapValue = m.seal(map<string>);

    return mapValue;
}

function sealAnyMapToRecord() returns Teacher {
    map anyMap = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher teacherValue = anyMap.seal(Teacher);

    return teacherValue;
}

function sealAnyMapToJSON() returns json {
    map anyMap = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    json jsonValue = anyMap.seal(json);

    return jsonValue;
}

function sealAnyMapToAny() returns any {
    map anyMap = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    any anyValue = anyMap.seal(any);

    return anyValue;
}

function sealAnyMapToSimilarOpenRecordMap() returns map<Employee> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<any> teacherMap = { "a": p1, "b": p2 };
    map<Employee> mapValue = teacherMap.seal(map<Employee>);

    return mapValue;
}


function sealAnyMapToRecordMap() returns map<Teacher> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<any> teacherMap = { "a": p1, "b": p2 };
    map<Teacher> mapValue = teacherMap.seal(map<Teacher>);

    return mapValue;
}

function sealAnyMapToJSONMap() returns map<json> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<any> teacherMap = { "a": p1, "b": p2 };
    map<json> jsonValue = teacherMap.seal(map<json>);

    return jsonValue;
}


function sealRecordMapToAnyMap() returns map<any> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    map<any> mapValue = teacherMap.seal(map<any>);

    return mapValue;
}

function sealRecordMapToSimilarOpenRecordMap() returns map<Employee> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    map<Employee> mapValue = teacherMap.seal(map<Employee>);

    return mapValue;
}

function sealRecordMapToJSONMap() returns map<json> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    map<json> mapValue = teacherMap.seal(map<json>);

    return mapValue;

}

function sealJSONMapToRecordMap() returns map<Employee> {
    map<json> teacherMap = { "a": { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" },
        "b": { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" } };
    map<Employee> mapValue = teacherMap.seal(map<Employee>);

    return mapValue;
}

function sealRecordTypeMultiDimensionMap() returns map<map<Employee>> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    map<map<Teacher>> multiMap = { "aa": teacherMap, "bb": teacherMap };

    map<map<Employee>> mapValue = multiMap.seal(map<map<Employee>>);

    return mapValue;
}

function sealAnyToIntMultiDimensionMap() returns map<map<map<int>>> {
    map<map<map<any>>> m = { "a": { "aa": { "aa": 11, "bb": 22 }, "bb": { "aa": 11, "bb": 22 } }, "b": { "aaa": { "aa":
    11, "bb": 22 }, "bbb": { "aa": 11, "bb": 22 } } };
    map<map<map<int>>> mapValue = m.seal(map<map<map<int>>>);

    return mapValue;
}

function sealIntToAnyMultiDimensionMap() returns map<map<map<any>>> {
    map<map<map<int>>> m = { "a": { "aa": { "aa": 11, "bb": 22 }, "bb": { "aa": 11, "bb": 22 } }, "b": { "aaa": { "aa":
    11, "bb": 22 }, "bbb": { "aa": 11, "bb": 22 } } };
    map<map<map<any>>> mapValue = m.seal(map<map<map<any>>>);

    return mapValue;
}

function sealConstraintMapToAnydata() returns anydata {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    anydata anydataValue = teacherMap.seal(anydata);

    return anydataValue;
}

//---------------------------------- Negative Test cases -----------------------------------------------------------


type EmployeeClosedRecord record {
    string name;
    string status;
    string batch;
    !...
};

function sealIntMapToStringMap() returns map<string> {
    map<int> m = { "a": 1, "b": 2 };
    map<string> mapValue = m.seal(map<string>);

    return mapValue;
}

function sealMapToRecordNegative() returns EmployeeClosedRecord {
    map<string> m = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };
    EmployeeClosedRecord employee = m.seal(EmployeeClosedRecord);

    return employee;
}
