import ballerina/io;

public function main() {
    // JSON Arrays are arrays including any JSON values.
    json j1 = [1, false, null, "foo", { first: "John", last: "Pala" }];
    io:println(j1);

    // Accesses the JSON array elements by index.
    json j2 = j1[4];
    io:println(j2.first);

    // Adds or changes the elements in a JSON array.
    j1[4] = 8.00;
    io:println(j1);

    // JSON array in an object literal.
    json p = {
        fname: "John", lname: "Stallone",
        family: [{ fname: "Peter", lname: "Stallone" },
        { fname: "Emma", lname: "Stallone" },
        { fname: "Jena", lname: "Stallone" },
        { fname: "Paul", lname: "Stallone" }]
    };
    p.family[2].fname = "Alisha";
    io:println(p);

    // Gets the length of the JSON array.
    json family = p.family;
    int l = family.length();
    io:println("length of array: " + l);

    // Loops through the array.
    int i = 0;
    while (i < l) {
        io:println(family[i]);
        i = i + 1;
    }
}
