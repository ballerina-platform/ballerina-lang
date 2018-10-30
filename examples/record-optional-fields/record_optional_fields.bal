import ballerina/io;

type Gender "male"|"female";

type Person record {
    // This is a required field with an explicit default value specified.
    string fname = "default";
    // This is a required field without an explicit default value. However, `string` has an implicit initial value (empty string).
    // Therefore, the compiler will not complain if we omit this field when creating the record.
    string lname;
    // This is a non-defaultable required field.
    Gender gender;
    // Adding `?` following the identifier marks the field as an optional field.
    int age?;
};

public function main() {
    // The `gender` must be provided when creating the record since `Gender` is not a type with an implicit initial value
    // and an explicit default value is not specified for it in the record descriptor.
    Person p = {gender: "male"};

    // Note that the `age` field is not present in the record since it is an optional field.
    io:println("Person with non-defaultable required field set: ", p);

    // Before accessing/using an optional field, it must added to the record.
    p.age = 25;
    io:println("Age: ", p.age);
    io:println("Updated person with optional field set: ", p);

    p = {fname: "Jane", lname: "Doe", gender: "female"};

    // Field values provided when creating a record takes highest precedence.
    io:println("Person with values assigned to required fields: ", p);
}
