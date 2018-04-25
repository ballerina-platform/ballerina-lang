import ballerina/io;

type address {
    string line01;
    string line02;
    string city;
    string state;
    string zipcode;
};

// Here this `addr` and `guardian` fields may or may not contain a value.
type person {
    string name;
    int age;
    address? addr;
    person? guardian;
};

// Now this function optionally returns a person value.
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

    // The field access operator is a lifted operator in Ballerina. As you can see here,
    // the type of both `p1` and `p1.addr` are optional types.
    // You would see the value "Santa Clara" in the console.
    string? city1 = p1.addr.city;
    io:println(city1);

    // Here the city2 variable will not contain a value.
    person? p2 = getPerson("");
    string? city2 = p2.addr.city;
    io:println(city2);

    // Here we've used the Elvis operator to eliminate nil.
    // The Elvis operator returns value of the first expression if it's value is not nil,
    // else the operator returns the value of the second expression.
    string defaultCity = "San Jose";
    string city = city2 ?: defaultCity;
    io:println(city);
}