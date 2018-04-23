import ballerina/io;

// This function takes a one required parameter and a rest parameter of type string.
// A function can have only one rest parameter. But the rest parameter can take any 
// number of values, and is equivalent to an array of the same type. 
function printFruits(string separator, string title = "Fruits: ", string... concatStrings) {
    string finalStr;
    foreach index, str in concatStrings {
        if (index == 0) {
            finalStr = str;
        } else {
            finalStr = finalStr + ", " + str;
        }
    }

    io:println(title + finalStr);
}

function main (string... args) {
    // Call the function by passing only the required parameter.
    printFruits(",");

    // Call the function by passing the required parameter, and one value for rest parameter.
    printFruits(",", "Apples");

    // Call the function by passing the required parameter, defaultable parameter, and one value for rest parameter.
    printFruits(",", title = "Available Fruits: ", "Apples");

    // Call the function by passing the separator, and multiple values for rest parameter.
    printFruits(",", "Apples", "Oranges");
    printFruits(",", title = "Available Fruits: ", "Apples", "Oranges", "Grapes");

    // The position of the defaultable parameters can be mixed with rest parameters as well,
    // when invoking.
    printFruits(",", "Apples", "Oranges", title = "Available Fruits: ", "Grapes");

    // An array can be passed as the rest parameter, rather than calling the function by passing
    // each value separately. 
    string[] fruits = ["Apples", "Oranges", "Grapes"];
    printFruits(",", title = "Available Fruits: ", ...fruits);
}
