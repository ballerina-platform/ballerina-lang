import ballerina/io;

// This function takes one required parameter and one rest parameter of type 
// string. A function can have only one rest parameter.
// The rest parameter can take any number of values, and is equivalent to an array of the same type. 
function printFruits(string separator,
                     string title = "Fruits: ",
                     string... concatStrings) {

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

function main(string... args) {
    // Call the function by passing only the required parameter.
    printFruits(",");

    // Call the function by passing the required parameter, and 
    // one value for the rest parameter.
    printFruits(",", "Apples");

    // Call the function by passing the required parameter, defaultable 
    // parameter, and one value for the rest parameter.
    printFruits(",", title = "Available Fruits: ", "Apples");

    // Call the function by passing a separator, and multiple values for the rest
    // parameter.
    printFruits(",", "Apples", "Oranges");
    printFruits(",", title = "Available Fruits: ", "Apples", "Oranges",
        "Grapes");

    // The placement of defaultable parameters can be mixed with rest 
    // parameters 
    // when invoking the function.
    printFruits(",", "Apples", "Oranges", title = "Available Fruits: ",
        "Grapes");

    // Pass an array as the rest parameter instaed of calling the 
    // function by passing each value separately. 
    string[] fruits = ["Apples", "Oranges", "Grapes"];
    printFruits(",", title = "Available Fruits: ", ...fruits);
}
