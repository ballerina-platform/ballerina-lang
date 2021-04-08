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

    // The `forEach()` function executes a specified function once for each of
    // the elements in the container.
    numbers.forEach(function(int i) {
        io:println(i);
    });

    // The `filter()` function returns a new container of the same type with
    // all the elements that satisfy the input predicate function.
    int[] positive = numbers.filter(function (int i) returns boolean {
        return i >= 0;
    });
    io:println("Positive numbers: ", positive);

    // The `reduce()` function uses the given combining function to produce
    // a single value. The combining function takes the combined value so far
    // and an element of the container and returns a new combined value.
    int total = numbers.reduce(sum, 0);
    io:println("Total: ", total);

    map<int> examScores = { "Jack": 80, "Tom": 65, "Anne": 90, "John": 50 };

    // The functional iteration operations can be chained together.
    // Here, we first filter the exam scores to only get the passed students,
    // and then we do a map operation to convert the scores to grades, and
    // we finally print out the grades of the students. 
    examScores.entries().filter((entry) => entry[1] >= 60).map(
        function ([string, int] entry) returns [string, string] {
            string grade;
            if entry[1] >= 90 { 
                grade = "A";
            } else if entry[1] >= 80 {
                grade = "B";
            } else if entry[1] >= 70 {
                grade = "C";
            } else {
                grade = "D";
            }
            return [entry[0], grade];
        }).forEach(function ([string, string] entry) {
            io:println("Student: ", entry[0], " Grade: ", entry[1]);
        });

}

function toUpper(string value) returns string {
    return value.toUpperAscii();
}

function sum(int accumulator, int currentValue) returns int {
    return accumulator + currentValue;
}
