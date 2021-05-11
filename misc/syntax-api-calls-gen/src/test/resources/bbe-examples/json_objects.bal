import ballerina/io;

public function main() {
    // Create a JSON object. A JSON object in Ballerina is equivalent to a `map<json>`.
    // Thus, the same can be written as
    // `map<json> j = { name: "apple", color: "red", price: j2 };`
    json j = {name: "apple", color: "red", price: 100};
    io:println(j.toJsonString());

    // Since a JSON object is a `map<json>`, this type test evaluates to true.
    io:println("j is map<json>: ", j is map<json>);

    // Create an empty JSON object. This is equivalent to an empty `map<json>`.
    json empty = {};

    int age = 30;
    // Create a JSON object. Keys can be defined with or without quotes.
    // Values can be any `json` expression.
    json p = {fname: "John", lname: "Stallone", "age": age};
    io:println(p.toJsonString());

    // You can add or change JSON object values using member access (i.e., the `[expr]` operator).
    // In order to change fields of an object value, it needs to be accessed
    // as a `map<json>`. This is done by casting `p` to `map<json>`.
    // Alternatively, `p` could have been defined as a `map<json>`.
    map<json> mp = <map<json>>p;
    mp["age"] = 31;
    io:println(p.toJsonString());

    // Create a nested JSON object.
    // This could also be defined as a `json` variable instead of `map<json>`.
    map<json> p2 = {
        fname: "Peter",
        lname: "Stallone",
        address: {
            line: "20 Palm Grove",
            city: "Colombo 03",
            country: "Sri Lanka"
        }
    };
    io:println(p2.toJsonString());

    // Member access expressions with `string` keys can be used to access fields of a
    // mapping of `json`.
    json lastName = p2["lname"];
    io:println(lastName);
}
