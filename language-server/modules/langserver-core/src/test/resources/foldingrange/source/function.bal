import ballerina/io;
import ballerina/math;

public function main() {
    io:println("Hello, World!");
}

# Prints PI value
# Refers math library
public function print(int value) {
    // Refer symbols of another module.
    // `math:PI` is a qualified identifier. Note the usage of the module prefix.
    float piValue = math:PI;

    // Use the explicit prefix `console` to invoke a function defined in the `ballerina/io` module.
    io:println(piValue);
    if value == 0 {
        io:println("Value cannot be zero.");
    } else if value < 0 {
        io:println("Value cannot be negative.");
    } else {
        io:println("Value is acceptable.");
    }

    map<string> countryCapitals = {
        "USA": "Washington, D.C.",
        "Sri Lanka": "Colombo",
        "England": "London"
    };
    foreach var [country, capital] in countryCapitals.entries() {
        io:println("Country: ", country, ", Capital: ", capital);
    }

    int[] numbers = [1, 3, 4, 7];
    while (numbers.length() > 0) {
        io:println(numbers.pop());
    }
}
