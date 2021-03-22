import ballerina/module1;

function testFunction() {
    string[] animals = ["Cat", "Canine", "Mouse", "Horse"];
    foreach string animal in animals {
        match animal {
            "Mouse" => {
                // logic goes here
            }
            "Dog"|"Canine" => {
                // logic goes here
            }
            "Cat"|"Feline" => {
                // logic goes here
            }
            _ => {
                // logic goes here
            }
        } 
    }
}
