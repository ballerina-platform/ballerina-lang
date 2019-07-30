import ballerina/io;

public function main() {
    // Defines a `map` of `string` values.
    map<string> words = {
        a: "ant",
        b: "bear",
        c: "cat",
        d: "dear",
        e: "elephant"
    };

    // The `.length()` operation returns the number of elements in any collection.
    io:println("Number of elements in 'words': ", words.length());

    // The `.map()` operation applies the given function to each item of the iterable
    /// collection and returns a new iterable collection of the same length.
    // The result of the map operation is assigned to a new `map<string>` as it returns a
    // a new map with modified values.
    map<string> animals = words.map(toUpper);
    io:println(animals);

    // Defines an `array` of `int` values.
    int[] numbers = [-5, -3, 2, 7, 12];

    // The `.filter()` operation returns a collection of all the elements that satisfy the
    // input predicate function.
    int[] positive = numbers.filter(function (int i) returns boolean {
                                  return i >= 0;
                              });

    // The `.forEach()` function executes a specified function once for each array element.
    numbers.forEach(function(int i) {
        io:println(i);
    });

    // The `.reduce()` function executes a reducer function on each element
    // of the array resulting in a single output value.
    int total = numbers.reduce(sum, 0);
    io:println("Total: ", total);

    int totalWithInitialValue = numbers.reduce(sum, 5);
    io:println("Total with initial value: ", totalWithInitialValue);

    io:println("\nExecution Order:-");
    // This is an example for multiple iterable operations.
    // The `foreach()` operation applies the given function to each item of the iterable collection.
    map<json> j = { name: "apple", colors: ["red", "green"], price: 5 };
    j.map(function (json value) returns string {
            string result = value.toString();
            io:println("- map operation's value: ", result);
            return result;
        }).forEach(function (string s) {
            io:println("-- foreach operation's value: ", s);
        });

}

function toUpper(string value) returns string {
    return value.toUpperAscii();
}

function sum(int accumulator, int currentValue) returns int {
    return accumulator + currentValue;
}
