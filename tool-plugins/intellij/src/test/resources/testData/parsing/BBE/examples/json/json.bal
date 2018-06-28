import ballerina/io;

function main(string... args) {
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

    // Create a JSON Objects.
    json j6 = { name: "apple", color: "red", price: j2 };
    io:println(j6);

    // Create a JSON Array. They are arrays of any JSON value.
    json j7 = [1, false, null, "foo", { first: "John", last: "Pala" }];
    io:println(j7);

    // Create an empty JSON Object.
    json empty = {};

    int age = 30;
    // Create a JSON object. Keys can be defined with or without quotes.
    // Values can be any expression.
    json p = { fname: "John", lname: "Stallone", "age": age };
    io:println(p);

    // You can access the object values by using dot (.) notation or array index notation.
    json firstName = p.fname;
    io:println(firstName);

    // Array index notation allows you to use any string valued expression as the index.
    json lastName = p["lname"];
    io:println(lastName);

    // You can add or change object values.
    p.lname = "Silva";
    p["age"] = 31;
    io:println(p);

    // Create a Nested JSON object.
    json p2 = {
         fname: "Peter",
         lname: "Stallone",
         "age": age,
         address: {
             line: "20 Palm Grove",
             city: "Colombo 03",
             country: "Sri Lanka"
         }
    };
    io:println(p2);

    p2.address.province = "Western";
    io:println(p2);
}
