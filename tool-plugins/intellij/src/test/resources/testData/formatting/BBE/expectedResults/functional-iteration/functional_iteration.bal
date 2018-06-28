import ballerina/io;

function main(string... args) {
    map words = { a: "ant", b: "bear", c: "cat", d: "dear",
        e: "elephant" };
    // The count operation returns the number of elements in any collection type.
    io:println("Total words count: " + words.count());

    // The map operation applies the given function to each item of the iterable collection and returns a new iterable
    // collection of equal length. The result of the map operation is assigned to a `string[]` as it returns a
    // collection of string variables.
    string[] animals = words.map(toUpper);
    io:println(animals);

    int[] numbers = [-5, -3, 2, 7, 12];
    // The filter operation returns a collection of all the elements that satisfy the input predicate function.
    // The average operation returns the average of the int/float collection. Other support operations are max(), min(),
    // and sum().
    float avg = numbers.filter((int i) => boolean {
            return i >= 0;
        }).average();
    io:println("Average of positive numbers: " + avg);

    io:println("\nExecution Order:-");
    // This is an example for multiple iterable operations.
    // The foreach operation applies the given function to each item of the iterable collection.
    json j = { name: "apple", colors: ["red", "green"], price: 5 };
    j.map((json j) => string {
            string s = j.toString();
            io:println("- map operation's value: ", s);
            return s;
        }).foreach((string s) => {
            io:println("-- foreach operation's value: ", s);
        });

}

function toUpper(any value) returns string {
    var word = <string>value;
    return word.toUpper();
}
