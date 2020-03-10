import ballerina/io;

public type Person record {
    string name;
    int age;
    // This is an anonymous record type descriptor.
    record {|
        string city;
        string country;
    |} address;
};

public function main() {
    // There is no difference in how we create normal records vs.
    // how we create anonymous records.
    Person john = {
        name: "John Doe",
        age: 25,
        address: {city: "Colombo", country: "Sri Lanka"}
    };
    io:println(john);

    // Since anonymous records do not have a type name associated with them,
    // the record descriptor itself has to be specified when declaring
    // variables of an anonymous record type.
    record {|
        string city;
        string country;
    |} adr = {city: "London", country: "UK"};

    Person jane = {name: "Jane Doe", age: 20, address: adr};
    io:println(jane);

    anydata[] fields = toFieldsArray(john);
    io:println(fields);
}

// This function accepts any record with `anydata`-typed fields.
// Anonymous record types are implicitly public.
function toFieldsArray(record {} anydataRecord) returns anydata[] {
    anydata[] fields = [];

    foreach var recField in anydataRecord {
        fields.push(recField);
    }

    return fields;
}
