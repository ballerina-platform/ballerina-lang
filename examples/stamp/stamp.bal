import ballerina/io;

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

type Foo record {
    string [] a;
    !...;
};

public function main() {

    // Stamp an `anydata` array as an `int` array.
    anydata[] anydataArray = [1, 2, 3, 4];
    int[]|error intArray = int[].stamp(anydataArray);
    if (intArray is int[]) {
        io:println("Element type of the array is 'int'");
    }

    // Stamp an `int` array as `json`.
    int[] intArrayValue = [1, 2, 3, 4];
    json|error jsonValue = json.stamp(intArrayValue);
    if (jsonValue is json[]) {
        io:println("Element type of the array is 'json'");
    }

    // Stamp a `Teacher` record as an `Employee` record.
    // This would not return an `error` since all fields match.
    Teacher t1 = { name: "Raja", age: 25, status: "single",
                            batch: "LK2014", school: "Hindu College" };
    Employee e = Employee.stamp(t1);
    string school = <string> e.school;
    io:println("School of the Employee is " + school);

    // Stamp an `anydata` map as a `Teacher` record.
    // This could return an `error` since all required fields may not be present.
    map<anydata> anydataMap = { name: "Raja", age: 25, status: "single",
                            batch: "LK2014", school: "Hindu College" };
    Teacher|error teacherValue = Teacher.stamp(anydataMap);
    if (teacherValue is Teacher) {
        io:println("'map<anydata>' is stamped as a 'Teacher'");
    }

    // Stamp a tuple as an `int` array
    (int, int) intTuple = (1, 2);
    int[]|error valueArray = int[].stamp(intTuple);
    if (valueArray is int[]) {
        io:println("Tuple is stamped as an 'int' array");
    }

    // Stamp a `json` value as a `Foo` record.
    json j1 = { a: ["a", "b"] };
    Foo|error recordValue = Foo.stamp(j1);
    if (recordValue is Foo) {
        io:println("JSON value is stamped as 'Foo'");
    }
}
