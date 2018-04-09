import ballerina/io;

public function main (string[] args) {
    map words = { a:"ant", b:"bear", c:"cat", d:"dear", e:"elephant" };
    // Count operation returns the number of elements in any collection type.
    io:println("total words count "+ words.count());

    // Map operation applies given function to each item of the iterable collection and returns a new iterable collection of equal length.
    // The result of the map operation is assigned to a string[] as it returns a collection of a string variable.
    string[] animals = words.map(toUpper);
    io:println(animals);

    int[] numbers = [-5, -3, 2, 7, 12];
    // Filter operation returns a collection containing all elements that satisfy the input predicate function.
    float avg = numbers.filter((int i) => boolean {
                                   return i >= 0;
                               })
    // Average operation returns the average of the int/float collection. Other support operations are max(), min() and sum().
                .average();
    io:println("Average of positive numbers " + avg);

    io:println("\nExecution Order");
    // Example of multiple iterable operations.
    json j = {name:"apple", colors:["red", "green"], price:5};
    j.map((json j) => string {
                    string s = j.toString() but {() => ""};
                    io:println("- map operation's value :" + s);
                    return s;
                })
    // Foreach operation applies the given function to each item of the iterable collection
            .foreach((string s) => (){
                    io:println("-- foreach operation's value :" + s);
                });

}

function toUpper (any value) returns string {
    var word = <string> value;
    match word {
        string x => { return x.toUpperCase();}
    }
}
