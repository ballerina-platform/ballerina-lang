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
    m.seal(IntRecord);

    return m;
}

function sealIntMapToJSON() returns json {
    map<int> m = { "a": 1, "b": 2 };
    m.seal(json);

    return m;
}

function sealIntMapToAny() returns any {
    map<int> m = { "a": 1, "b": 2 };
    m.seal(any);

    return m;
}

function sealIntMapToIntMap() returns map<int> {
    map<int> m = { "a": 1, "b": 2 };
    m.seal(map<int>);

    return m;
}

function sealIntMapToAnyMap() returns map<any> {
    map<int> m = { "a": 1, "b": 2 };
    m.seal(map<any>);

    return m;
}


function sealAnyMapToIntMap() returns map<int> {
    map<any> m = { "a": 1, "b": 2 };
    m.seal(map<int>);

    return m;
}


function sealAnyMapToStringMap() returns map<string> {
    map<any> m = { "firstName": "mohan", "lastName": "raj" };
    m.seal(map<string>);

    return m;
}


function sealAnyMapToStringMapWithoutExplicitConstraintType() returns map<string> {
    map<any> m = { "firstName": "mohan", "lastName": "raj" };
    m.seal(map<string>);

    return m;
}

function sealAnyMapToRecord() returns Teacher {
    map anyMap = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    anyMap.seal(Teacher);

    return anyMap;
}

function sealAnyMapToJSON() returns json {
    map anyMap = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    anyMap.seal(json);

    return anyMap;
}

function sealAnyMapToAny() returns any {
    map anyMap = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    anyMap.seal(any);

    return anyMap;
}

function sealAnyMapToSimilarOpenRecordMap() returns map<Employee> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<any> teacherMap = { "a": p1, "b": p2 };
    teacherMap.seal(map<Employee>);

    return teacherMap;
}


function sealAnyMapToRecordMap() returns map<Teacher> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<any> teacherMap = { "a": p1, "b": p2 };
    teacherMap.seal(map<Teacher>);

    return teacherMap;
}

function sealAnyMapToJSONMap() returns map<json> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<any> teacherMap = { "a": p1, "b": p2 };
    teacherMap.seal(map<json>);

    return teacherMap;
}


function sealRecordMapToAnyMap() returns map<any> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    teacherMap.seal(map<any>);

    return teacherMap;
}

function sealRecordMapToSimilarOpenRecordMap() returns map<Employee> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    teacherMap.seal(map<Employee>);

    return teacherMap;
}

function sealRecordMapToJSONMap() returns map<json> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    teacherMap.seal(map<json>);

    return teacherMap;

}

function sealJSONMapToRecordMap() returns map<Employee> {
    map<json> teacherMap = { "a": { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" },
        "b": { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" } };
    teacherMap.seal(map<Employee>);

    return teacherMap;
}

function sealRecordTypeMultiDimensionMap() returns map<map<Employee>> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    map<map<Teacher>> multiMap = { "aa": teacherMap, "bb": teacherMap };

    multiMap.seal(map<map<Employee>>);

    return multiMap;
}

function sealAnyToIntMultiDimensionMap() returns map<map<map<int>>> {
    map<map<map<any>>> m = { "a": { "aa": { "aa": 11, "bb": 22 }, "bb": { "aa": 11, "bb": 22 } }, "b": { "aaa": { "aa":
    11, "bb": 22 }, "bbb": { "aa": 11, "bb": 22 } } };
    m.seal(map<map<map<int>>>);

    return m;
}

function sealIntToAnyMultiDimensionMap() returns map<map<map<any>>> {
    map<map<map<int>>> m = { "a": { "aa": { "aa": 11, "bb": 22 }, "bb": { "aa": 11, "bb": 22 } }, "b": { "aaa": { "aa":
    11, "bb": 22 }, "bbb": { "aa": 11, "bb": 22 } } };
    m.seal(map<map<map<any>>>);

    return m;
}
