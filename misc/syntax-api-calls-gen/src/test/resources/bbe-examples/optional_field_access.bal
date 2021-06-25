import ballerina/io;

type Address record {
    string line01;
    string line02;
    string city;
    string state;
    string zipcode;
};

// According to the `Person` record type descriptor, the `addr` field could either hold an `Address` record or the value
// nil (`()`). Moreover, the `age` field is an optional field that may or may not be specified.
type Person record {
    string name;
    int age?;
    Address? addr;
};

// This function optionally returns a `Person` value.
function getPerson(string name, int age) returns Person? {
    if (name == "" || age == 0) {
        return;
    } else {
        // Defines a `Person` record.
        Person p = {
            name: name,
            age: age,
            addr: {
                line01: "No. 61",
                line02: "Brandon street",
                city: "Santa Clara",
                state: "CA",
                zipcode: "95134"
            }
        };
        return p;
    }
}

public function main() {

    // Create a `Person` value without specifying the `age` field.
    Person p1 = {name: "Anne", addr: ()};
    // The optional field `age` can be accessed using the optional field access operator.
    // The return type will be the union of the type of the field and `()`.
    // If the value is present, the value will be returned.
    // If not, `()` will be returned.
    // `()` is returned here since `age` is not set.
    int? age = p1?.age;
    io:println("Age: ", age);

    // Create a `Person` value with the `age` field.
    p1 = {name: "Anne", age: 24, addr: ()};
    // Now, `age` will be the `int` value that is set.
    age = p1?.age;
    io:println("Age: ", age);

    Person? p2 = getPerson("John", 30);
    io:println(p2);

    // The optional field access operator is a lifted operator in Ballerina.
    // Both `p2` and `p2.addr` are optional record types.
    // If the type of either `p2` or `p2.addr` is `()` at runtime, `()` would
    // be assigned to the `city1` variable. Else, the `string` value of the `city` field
    // of the `address` field of the `Person` record `p2` would be assigned.
    string? city1 = p2?.addr?.city;
    // The value "Santa Clara" is displayed in the console.
    io:println(city1);

    // Invoking `getPerson()` as follows will result in `()` being assigned to `p3`.
    Person? p3 = getPerson("", 0);
    // Thus, the `city2` variable will contain `()` as the value.
    string? city2 = p3?.addr?.city;
    io:println(city2);

    // Now, assign a `Person` record to `p3` setting the `address` field to `()`.
    p3 = {name: "George", age: 20, addr: ()};
    // Again, the `city2` variable will contain `()`, since the value of the `address` field is `()`.
    city2 = p3?.addr?.city;
    io:println(city2);

    // In this example, the Elvis operator is used to eliminate `nil`.
    // If the value of the first expression is not `nil`, the Elvis operator returns that value.
    // If the value of the first expression is `nil`, the operator returns the value of the second expression.
    string defaultCity = "San Jose";
    string city = city2 ?: defaultCity;
    io:println(city);
}
