import ballerina/io;

public function main() {

    int[5] intArray = [0, 1, 2, 3, 4];

    foreach var counter in intArray {

        // The value of the `counter` variable is matched against the given value match patterns.
        match counter {
            0 => io:println("value is: 0");
            1 => io:println("value is: 1");
            2 => io:println("value is: 2");
            3 => io:println("value is: 3");
            4 => io:println("value is: 4");
            5 => io:println("value is: 5");
        }
    }

    string[] animals = ["Cat", "Canine", "Mouse", "Horse"];

    foreach string animal in animals {

        // The value match can also be used with binary OR expression.
        match animal {
            "Mouse" => io:println("Mouse");
            "Dog"|"Canine" => io:println("Dog");
            "Cat"|"Feline" => io:println("Cat");
            // The pattern `_` can be used as the final static value match pattern, which will be matched against all values.
            _ => io:println("Match All");
        }
    }
}
