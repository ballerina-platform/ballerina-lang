import ballerina/io;

public function main() {
    // Create a JSON string value.
    json j1 = "Apple";
    io:println(j1);

    // Create a JSON number value.
    json j2 = 5.36;
    io:println(j2);

    // Create a JSON true value.
    json j3 = true;
    io:println(j3);

    // Create a JSON false value.
    json j4 = false;
    io:println(j4);

    // Create a JSON null value.
    json j5 = null;

    // Creates a JSON Object. This is equivalent to a `map<json>`.
    json j6 = { name: "apple", color: "red", price: j2 };
    io:println(j6);

    // Create a JSON Array. This is equivalent to a `json[]`.
    json j7 = [1, false, null, "foo", { first: "John", last: "Pala" }];
    io:println(j7);
}
