import ballerina/io;

type address record {
    string line01;
    string line02;
    string city;
    string state;
    string zipcode;
};

// According to this, `addr` and `guardian` fields may or may not contain a value.
type person record {
    string name;
    int age;
    address? addr;
    person? guardian;
};

// This function optionally returns a person value.
function getPerson(string name) returns person? {
    if (name == "") {
        return ();
    } else {
        person p = {name: name, addr:
                            {line01: "61 brandon stree", city: "Santa Clara",
                                state: "CA", zipcode: "95134"}};
        return p;
    }
}

function main(string... args) {
    person? p1 = getPerson("John");
    io:println(p1);

    // The field access operator is a lifted operator in Ballerina. As shown here,
    // the type of both `p1` and `p1.addr` are optional types.
    // The value "Santa Clara" is displayed in the console.
    string? city1 = p1.addr.city;
    io:println(city1);

    // According to this, the `city2` variable will not contain a value.
    person? p2 = getPerson("");
    string? city2 = p2.addr.city;
    io:println(city2);

    // Here, the Elvis operator is used to eliminate `nil`.
    // If the value of the first expression is not `nil`, the Elvis operator returns that value.
    // If the value of the first expression is `nil`, the operator returns the value of the second expression.
    string defaultCity = "San Jose";
    string city = city2 ?: defaultCity;
    io:println(city);
}