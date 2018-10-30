import ballerina/io;

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

function testSealWithOpenRecords() returns Employee {
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    t1.seal(Employee);
    return t1;
}

function testSealWithOpenRecordsNonAssignable() returns Teacher {
    Employee e1 = { name: "Raja", status: "single", batch: "LK2014" };

    e1.seal(Teacher);
    return e1;
}

function testSealClosedRecordWithOpenRecord() returns Employee {
    Person p1 = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };

    p1.seal(Employee);
    return p1;
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

function sealAnyMapToStringMapWithoutExplicitConstraintType() returns map<string> {
    map<any> m = { "firstName": "mohan", "lastName": "raj" };
    m.seal(map<string>);

    return m;
}

function sealObjectsV1() returns EmployeeObj {
    PersonObj p = new PersonObj();
    p.seal(EmployeeObj);

    return p;
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

function sealAnyMapToSimilarOpenRecordMap() returns map<Employee> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<any> teacherMap = { "a": p1, "b": p2 };
    teacherMap.seal(map<Employee>);

    return teacherMap;
}


function sealAnyToRecordMap() returns map<Teacher> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<any> teacherMap = { "a": p1, "b": p2 };
    teacherMap.seal(map<Teacher>);

    return teacherMap;
}

function sealRecordToAnyArray() returns any[] {

    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    teacherArray.seal(any[]);

    return teacherArray;
}

function sealAnyToRecordArray() returns Teacher[] {

    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    any[] teacherArray = [p1, p2];
    teacherArray.seal(Teacher[]);

    return teacherArray;
}

function sealAnyToSimilarOpenRecordArray() returns Employee[] {

    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    any[] teacherArray = [p1, p2];
    teacherArray.seal(Employee[]);

    return teacherArray;
}

function sealRecordToSimilarOpenRecordArray() returns Employee[] {

    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    teacherArray.seal(Employee[]);

    return teacherArray;
}

