import ballerina/io;

public function main() {
    // JSON Arrays are arrays with JSON values as members.
    // The same could be written as
    // `json[] j1 = [1, false, null, "foo", { first: "John", last: "Pala" }];`
    json j1 = [1, false, null, "foo", {first: "John", last: "Pala"}];
    io:println(j1.toJsonString());

    // JSON array elements can be accessed by index.
    // The `json` value `j1` first needs to be cast to a `json[]` to use member access.
    // Alternatively, `j1` could have been defined as a `json[]`.
    json[] j2 = <json[]>j1;
    json j3 = j2[4];
    io:println(j3.toJsonString());

    // Similarly, member access could be used with `json[]`-typed variables to add or
    // change members in a `json` array.
    j2[4] = 8.00;
    io:println(j1.toJsonString());

    // JSON array in an object literal.
    map<json> p = {
        fname: "John",
        lname: "Stallone",
        family: [
            {fname: "Peter", lname: "Stallone"},
            {fname: "Emma", lname: "Stallone"},
            {fname: "Jena", lname: "Stallone"},
            {fname: "Paul", lname: "Stallone"}
        ]
    };

    json[] family = <json[]>p["family"];
    map<json> member2 = <map<json>>family[2];
    member2["fname"] = "Alisha";
    io:println(p.toJsonString());

    // Get the length of the JSON array.
    int l = family.length();
    io:println("length of the array: ", l);

    // Loop through the array.
    int i = 0;
    while (i < l) {
        io:println(family[i].toJsonString());
        i = i + 1;
    }
}
