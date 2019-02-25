import ballerina/io;

type Student record {
    string name;
    int age;
    Grades grades;
    string...;
};

type Grades record {
    int maths;
    int physics;
    int chemistry;
    !...;
};

public function main() {
    // This creates a `Student` record. Since all the fields are required and none of the fields
    // have explicit default values assigned to them, values must be specified for all the fields
    // when creating the record.
    Student john = {name: "John Doe", age: 17,
                    grades: {maths: 80, physics: 75, chemistry: 65}};
    io:println(john);

    // This is an example of field-based access of `record` fields. The return type of this expression is the
    // type of the field. If it is an open record and the specified key is not present in the record at run time,
    // it will result in a `panic`. If it is a closed `record`, accessing an undefined key will result in a compilation error.
    io:println(john.name);

    // This is an example of index-based access of `record` fields. The return type of this expression is `T?`, where
    // `T` is the type of the field. If it is an open `record` and the specified key is not present in the `record` at run time,
    // `()` will be returned. If it is a closed `record`, accessing an undefined key will result in a compilation error.
    io:println(john["name"]);

    // This fetches a field of a nested `record`.
    io:println(john.grades.maths);

    Student peter = {name: "Peter", age: 19,
                     grades: {maths: 40, physics: 35, chemistry: 35}};
    // This modifies the value of the `age` field.
    peter.age = 16;

    io:println(peter);
    io:println(john);

    // This adds an additional field not defined in the record type descriptor above.
    // Note that an attempt to add additional fields to a closed `record` results in compile errors.
    // e.g., `peter.grades.ict = 77;` will result in a compile error.
    peter.department = "Computer Science";
    io:println(peter);
}
