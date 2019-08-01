import ballerina/io;

// Defines an open record type named `Student`. The `{` and `}` delimiters indicate that in addition to the defined fields,
// this record type allows additional fields with pure-typed (i.e., `anydata|error`) values.
// The descriptor `record { }` is equivalent to `record {| (anydata|error)...; |}`.
type Student record {
    string name;
    int age;
    Grades grades;
};

// Defines a closed record type named `Address`. The `{|` and `|}` delimiters indicate that this record type
// allows mapping values, which contain only the described fields.
type Address record {|
    string city;
    string country;
|};

// Defines an open record type named `Grades`. Although it is defined using the `{|` and `|}` delimiters, it has
// an `int` rest field as well. Therefore, this is an open record type.
type Grades record {|
    int maths;
    int physics;
    int chemistry;
    // This is a rest field of the type`int`. All additional fields should be of the type or a subtype of the rest field.
    int...;
|};


public function main() {
    // This creates a `Student` record. Since all the fields are required and none of the fields
    // have explicit default values assigned to them, values must be specified for all the fields
    // when creating the record.
    Student john = {name: "John Doe", age: 17,
                    grades: {maths: 80, physics: 75, chemistry: 65}};
    io:println(john);

    // This is an example of field-based access of record fields. The return type of this expression is the
    // type of the field. If it is an open record and the specified key is not present in the record at runtime,
    // it will result in a `panic`. If it is a closed record, accessing an undefined key will result in a compilation error.
    io:println(john.name);

    // This is an example of index-based access of record fields. The return type of this expression is `T?`, in which
    // `T` is the type of the field. If it is an open record and the specified key is not present in the record at runtime,
    // `()` will be returned. If it is a closed record, accessing an undefined key will result in a compilation error.
    io:println(john["name"]);

    // This fetches a field of a nested record.
    io:println(john.grades.maths);

    Student peter = {name: "Peter", age: 19,
                     grades: {maths: 40, physics: 35, chemistry: 35}};
    // This modifies the value of the `age` field.
    peter.age = 16;

    io:println(peter);
    io:println(john);

    // This adds an additional field, which is not defined in the record type descriptor above.
    // An attempt to add additional fields to a closed record results in compile errors.
    // E.g., `peter.address.street = "Palm Grove";` will result in a compile error.
    peter["address"] = <Address>{ city: "Colombo", country: "Sri Lanka" };
    io:println(peter);

    // Create a `Grades` record adding additional fields for the `int`-typed rest field.
    // The `english` field is not specified in the record, but is allowed since `Grades` is an
    // open record with an `int`-typed rest field.
    // Keys for such field should either be `string` literals or expressions (i.e., they
    // cannot be identifiers).
    Grades grades = { maths: 80, physics: 75, chemistry: 65, "english": 90 };
    io:println(grades);

    // Similarly, only member access can be used to access the fields that are possibly
    // added for the rest field. An `int` value is returned if the field is present in the
    // record, else `()` is returned.
    int? english = grades["english"];
    io:println(english);
}
