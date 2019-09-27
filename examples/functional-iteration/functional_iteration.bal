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

    // The `length()` function returns the number of elements in the container.
    io:println("Number of elements in 'words': ", words.length());

    // The `map()` function applies the given function to each item of the container
    // and returns a new container of the same type and length.
    map<string> animals = words.map(toUpper);
    io:println(animals);

    // Defines an array of `int` values.
    int[] numbers = [-5, -3, 2, 7, 12];

    // The `filter()` function returns a new container of the same type with
    // all the elements that satisfy the input predicate function.
    int[] positive = numbers.filter(function (int i) returns boolean {
        return i >= 0;
    });
    io:println("Positive numbers: ", positive);

    // The `forEach()` function executes a specified function once for each of
    // the elements in the container.
    numbers.forEach(function(int i) {
        io:println(i);
    });

    // The `reduce()` function uses the given combining function to produce
    // a single value. The combining function takes the combined value so far
    // and an element of the container and returns a new combined value.
    int total = numbers.reduce(sum, 0);
    io:println("Total: ", total);

    int totalWithInitialValue = numbers.reduce(sum, 5);
    io:println("Total with initial value: ", totalWithInitialValue);

    io:println("\nExecution Order:-");
    // The `forEach()` function applies the given function to each item of the container.
    map<json> j = {name: "apple", colors: ["red", "green"], price: 5};
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
