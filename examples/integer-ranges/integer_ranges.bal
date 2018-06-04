import ballerina/io;

function main(string... args) {
    // A closed integer range `x ... y` generates an array of integers containing values `x` to `y`, `x` and `y`
    // inclusive
    io:println("Iterating over an array created by a Closed Integer Range");
    int[] closedRangeArray = 25 ... 30;
    foreach value in closedRangeArray {
        io:println(value);
    }

    // A half open integer range `x ..< y` generates an array of integers containing values `x` to `y`, including `x`
    // but excluding `y`
    io:println("\nIterating over an array created by a Half Open Integer Range");
    int[] halfOpenRangeArray = 25 ..< 30;
    foreach value in halfOpenRangeArray {
        io:println(value);
    }

    // Integer ranges could also be used directly in `foreach` statements.
    io:println("\nUsing an Integer Range in a foreach statement");
    string[] stringArray = ["fruit", "tree", "basket"];
    foreach index in 0 ..< lengthof stringArray {
        io:println("Index: ", index, ", Value: ", stringArray[index]);
    }
}
