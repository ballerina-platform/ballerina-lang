import ballerina/io;

public function main() {
    // Define a `map` of `string` values.
    map<string> words = {
        a: "ant",
        b: "bear",
        c: "cat",
        d: "dear",
        e: "elephant"
    };

    // The `count()` operation returns the number of elements in any collection.
    io:println("Number of elements in 'words': ", words.count());

    // The `map()` operation applies the given function to each item of the iterable collection and returns a new
    // iterable collection of equal length. The result of the map operation is assigned to a `string[]` as it returns a
    // collection of string variables.
    string[] animals = words.map(toUpper);
    io:println(animals);

    // Define an `array` of `int` values.
    int[] numbers = [-5, -3, 2, 7, 12];
    // The `filter()` operation returns a collection of all the elements that satisfy the input predicate function.
    // The `average()` operation returns the average of a collection of `int` or `float`.
    // The `max()` operation returns the maximum element from a collection.
    // The `min()` operation returns the minimum element from a collection.
    // The `sum()` operation returns the sum of all elements in a collection.
    float avg = numbers.filter(function (int i) returns boolean {
                                  return i >= 0;
                              }).average();
    io:println("Average of positive numbers: ", avg);

    io:println("\nExecution Order:-");
    // This is an example for multiple iterable operations.
    // The `foreach()` operation applies the given function to each item of the iterable collection.
    map<json> j = { name: "apple", colors: ["red", "green"], price: 5 };
    j.map(function ((string, json) pair) returns string {
            var (key, value) = pair;
            string result = value.toString();
            io:println("- map operation's value: ", result);
            return result;
        }).foreach(function (string s) {
            io:println("-- foreach operation's value: ", s);
        });

}

function toUpper((string, string) pair) returns string {
    var (key, value) = pair;
    return value.toUpper();
}
