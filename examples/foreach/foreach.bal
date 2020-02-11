import ballerina/io;

public function main() {
    io:println("Iterating a string array :");
    string[] fruits = ["apple", "banana", "cherry"];

    // The `foreach` statement can be used to iterate an array. Each iteration returns an element in the array.
    // The index of the corresponding element is not returned.
    foreach var v in fruits {
        io:println("fruit: ", v);
    }

    io:println("\nIterating a map :");
    map<string> words = {a: "apple", b: "banana", c: "cherry"};
    // Iterating a map will return the values in the map.
    foreach var fruit in words {
        io:println(fruit);
    }
    // Calling the `.entries()' method on a `map` and iterating it will return the key (`string`) and the value as a
    // `tuple` variable. Tuple destructuring can be used to split the tuple variable into two variables.
    foreach var [k, v] in words.entries() {
        io:println("letter: ", k, ", word: ", v);
    }

    io:println("\nIterating a JSON object :");
    json apple = {name: "apple", colors: ["red", "green"], price: 5};
    // Iterating a JSON is only supported with `map<json>` or `json[]`.
    // To iterate a JSON, first cast it to the relevant iterable type.
    map<json> mapValue = <map<json>>apple;
    foreach var value in mapValue {
        if (value is string) {
            io:println("string value: ", value);
        } else if (value is int) {
            io:println("int value: ", value);
        } else if (value is json[]) {
            io:println("json array value: ", value);
        } else {
            // JSON is a union type for `()` or `null`|`int`|`float`|`decimal`|`string`|`json[]`|`map<json>`.
            // The else block will be reached if `j` is neither `string`, `int`, or `json[]`.
            io:println("non-string value: ", value);
        }
    }

    io:println("\nIterating a JSON array :");
    // To iterate a JSON array, you need to first cast it into a JSON array (`json[]`).
    json[] colors = <json[]>apple.colors;
    int counter = 0;
    foreach var j in colors {
        io:println("color ", counter, ": ", j);
        counter += 1;
    }

    io:println("\nIterating XML :");
    xml book = xml `<book>
                        <name>Sherlock Holmes</name>
                        <author>Sir Arthur Conan Doyle</author>
                    </book>`;
    // Iterating an XML will return each element in each iteration.
    counter = 0;
    foreach var x in book.getChildren().elements() {
        io:println("xml at ", counter, ": ", x);
        counter += 1;
    }

    io:println("\nIterating a closed integer range :");
    int endValue = 10;
    int sum = 0;
    // A closed integer range in the `foreach` statement represents an incremental integer value range from the start
    // expression (`1`) to the end expression (`endValue`) inclusively.
    foreach var i in 1 ... endValue {
        sum = sum + i;
    }
    io:println("summation from 1 to ", endValue, " is ", sum);

    io:println("\nIterating a half open integer range :");
    sum = 0;
    // A half-open integer range in the `foreach` statement represents an incremental integer value range from the start
    // expression (`1`) inclusively, to the end expression (`endValue`) exclusively.
    foreach var i in 1 ..< endValue {
        sum = sum + i;
    }
    io:println("summation from 1 to ", endValue,
                        " excluding ", endValue, " is ", sum);
}
