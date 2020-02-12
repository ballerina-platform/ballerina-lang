import ballerina/io;

type Gender "male"|"female";

type Person record {
    // This is a required field without an explicit default value.
    // The compiler will not assign default values. Therefore, a value should be specified
    // for this field when creating the record.
    string fname;
    // This is a required field with an explicit default value specified.
    string lname = "";

    Gender gender;
    // Adding `?` following the identifier marks the field as an optional field.
    int age?;
};

public function main() {
    // The required fields `fname` and `gender` are not given default values in the record type descriptor.
    // Therefore, values must be specified for `fname` and `gender` when creating the record.
    Person john = {fname: "John", gender: "male"};

    // The `age` field is not present in the record since it is an optional field.
    io:println("Person with the non-defaultable required field set: ", john);

    // Optional fields of the record can be accessed using the `?.` operator.
    // This returns the value if the field is present in the record. Returns `()` if not.
    // Since the `age` field is not set, optional field access returns `()` here.
    int? age = john?.age;
    io:println("Age before setting: ", age);

    // Now set a value for the `age` field. Field access can be used to set the value since
    // `age` is a field defined in the type descriptor for `Person`.
    john.age = 25;
    // Since the `age` field is set, optional field access returns the `int`-typed value.
    age = john?.age;
    io:println("Age after setting: ", age);

    // Create a `Person`-typed value specifying a value for the defaultable field `lname`.
    Person jane = {fname: "Jane", lname: "Doe", gender: "female"};

    // Field values provided when creating a record takes highest precedence.
    io:println("Person with values assigned to required fields: ", jane);
}
