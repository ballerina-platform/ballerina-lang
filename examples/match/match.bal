import ballerina/io;

public function main() {

    int[5] intArray = [0, 1, 2, 3, 4];
    foreach var counter in intArray {

        // `counter` variable value assigned with the foreach is checked with the value match.
        match counter {
            0 => io:println("value is: 0");
            1 => io:println("value is: 1");
            2 => io:println("value is: 2");
            3 => io:println("value is: 3");
            4 => io:println("value is: 4");
            5 => io:println("value is: 5");
        }
    }

    // The static value match also works with binary expressions
    string[] animals = ["Cat", "Canine", "Mouse", "Horse"];
    foreach string animal in animals {
        match animal {
            "Mouse" => io:println("Mouse");
            "Dog"|"Canine" => io:println("Dog");
            "Cat"|"Feline" => io:println("Cat");
            // `_` can be used as the  final static value match which will be matched to all values.
            _ => io:println("No Match");
        }
    }
}
