import ballerina/io;

type address record {
    string line01;
    string line02;
    string city;
    string state;
    string zipcode;
};

// According to this, `addr` and `guardian` fields may or may not contain a value. And either all the fields must be
// assigned with default values e.g. `string name = ""` or be initialized before use.
type person record {
    string name;
    int age;
    address? addr;
    person? guardian;
};

// This function optionally returns a person value.
function getPerson(string name, int age) returns person? {
    if (name == "" || age == 0) {
        return ();
    } else {
        person p = {name: name, age: age, addr:
                            {line01: "No. 61", line02: "Brandon street",
                                city: "Santa Clara", state: "CA",
                                zipcode: "95134"}, guardian: ()};
        return p;
    }
}

public function main() {
    person? p1 = getPerson("John", 30);
    io:println(p1);

    // The field access operator is a lifted operator in Ballerina. As shown here,
    // the type of both `p1` and `p1.addr` are optional types.
    // The value "Santa Clara" is displayed in the console.
    string? city1 = p1.addr.city;
    io:println(city1);

    // According to this, the `city2` variable will not contain a value.
    person? p2 = getPerson("", 0);
    string? city2 = p2.addr.city;
    io:println(city2);

    // Here, the Elvis operator is used to eliminate `nil`.
    // If the value of the first expression is not `nil`, the Elvis operator returns that value.
    // If the value of the first expression is `nil`, the operator returns the value of the second expression.
    string defaultCity = "San Jose";
    string city = city2 ?: defaultCity;
    io:println(city);
}