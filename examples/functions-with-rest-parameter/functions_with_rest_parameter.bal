import ballerina/io;

// This function takes one required parameter, one defaultable parameter, and
// one rest parameter of the type `string`. A function can have only one rest
// parameter. The rest parameter can take any number of values and is
// equivalent to an array of the same type.
function printFruits(string separator,
                     string title = "Fruits: ",
                     string... concatStrings) {

    string finalStr = "";
    int index = 0;
    foreach var str in concatStrings {
        if (index == 0) {
            finalStr = str;
        } else {
            finalStr = finalStr + separator + str;
        }
        index += 1;
    }

    io:println(title + finalStr);
}

public function main() {
    // Calls the function by passing only the required parameter.
    printFruits(",");

    // Calls the function by passing the required parameter and
    // one value for the rest parameter.
    printFruits(",", "Apples");

    // Calls the function by passing the required parameter, defaultable 
    // parameter, and one value for the rest parameter.
    printFruits(",", title = "Available Fruits: ", "Apples");

    // Calls the function by passing the required parameter and multiple values
    // for the rest parameter.
    printFruits(",", "Apples", "Oranges");

    // Calls the function by passing all three parameters.
    printFruits(",", title = "Available Fruits: ", "Apples", "Oranges",
                "Grapes");

    // The placement of the defaultable parameters can be mixed with the rest
    // parameters when invoking the function.
    printFruits(",", "Apples", "Oranges", title = "Available Fruits: ",
                "Grapes");

    // Passes an array as the rest parameter instead of calling the 
    // function by passing each value separately. 
    string[] fruits = ["Apples", "Oranges", "Grapes"];
    printFruits(",", title = "Available Fruits: ", ...fruits);
}
