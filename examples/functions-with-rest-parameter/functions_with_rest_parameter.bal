import ballerina/io;

// This function takes one required parameter, one defaultable parameter, and
// one rest parameter of the type `string`. A function can have only one rest
// parameter. The rest parameter can take any number of values and is
// equivalent to a list of the same type.
function printDetails(string name,
int age = 18,
string... modules) {
    string detailString = "Name: " + name + ", Age: " + age.toString();

    if (modules.length() == 0) {
        io:println(detailString);
        return;
    }

    int index = 0;
    string moduleString = "Module(s): ";

    foreach string module in modules {
        if (index == 0) {
            moduleString += module;
        } else {
            moduleString += ", " + module;
        }
        index += 1;
    }

    io:println(detailString, ", ", moduleString);
}

public function main() {
    // Call the function by passing only the required parameter.
    printDetails("Alice");

    // Call the function by passing the required parameter and
    // the defaultable parameter. Named arguments can also be used
    // since values are not passed for the rest parameter.
    printDetails("Bob", 20);

    // Call the function by passing the required parameter, the defaultable
    // parameter, and one value for the rest parameter.
    // Arguments cannot be passed as named arguments since values are specified
    // for the rest parameter.
    printDetails("Corey", 19, "Math");

    // Call the function by passing the required parameter, defaultable parameter,
    // and multiple values for the rest parameter.
    printDetails("Diana", 20, "Math", "Physics");

    // Pass an array as the rest parameter instead of calling the
    // function by passing each value separately.
    string[] modules = ["Math", "Physics"];
    printDetails("Diana", 20, ...modules);
}
