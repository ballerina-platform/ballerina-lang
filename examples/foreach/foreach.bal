import ballerina/io;

public function main() {
    io:println("Iterating over a string array:-");
    string[] fruits = ["apple", "banana", "cherry"];
    // To fetch the values defined in the array, use one variable.
    // To fetch the index and value, use two variables as comma separated values. E.g., `foreach i, v in fruits`.
    foreach v in fruits {
        io:println("fruit: " + v);
    }

    io:println("\nIterating over a map:-");
    map<string> words = { a: "apple", b: "banana", c: "cherry" };
    // To fetch the values defined in the map, use one variable. To fetch both the key (`string`) and value, use two
    // variables.
    foreach k, v in words {
        io:println("letter: ", k, ", word: ", v);
    }

    io:println("\nIterating over a json object:-");
    json apple = { name: "apple", colors: ["red", "green"], price: 5 };
    // To fetch a JSON value, use only a single `json` typed variable.
    foreach j in apple {
        if (j is string) {
            io:println("string value: ", j);
        } else {
            // JSON is a union type for () or null | int | float | decimal | string | json[] | map<json>,
            // thus json array and int will be matched. `j` in else block is type `anydata`.
            io:println("non-string value: ", j);
        }
    }

    io:println("\nIterating over a json array:-");
    // To Iterate over a JSON array, you need to first cast it into an array of json (`json[]`).
    json[] colors = <json[]>apple.colors;
    foreach i, j in colors {
        io:println("color ", i, ": ", j);
    }

    io:println("\nIterating over an xml:-");
    xml book = xml `<book>
                        <name>Sherlock Holmes</name>
                        <author>Sir Arthur Conan Doyle</author>
                    </book>`;
    // To fetch the XML value, use one variable. To get both the index (`int`) and XML value, use two variables.
    foreach i, x in book.*.elements(){
        io:println("xml at ", i, ": ", x);
    }

    io:println("\nIterating over a closed integer range:-");
    int endValue = 10;
    int sum = 0;
    // A closed integer range in the `foreach` statement represents an incremental integer value range from the start
    // expression (`1`) to the end expression (`endValue`) inclusively.
    foreach i in 1 ... endValue {
        sum = sum + i;
    }
    io:println("summation from 1 to " + endValue + " is " + sum);

    io:println("\nIterating over a half open integer range:-");
    sum = 0;
    // A half open integer range in the `foreach` statement represents an incremental integer value range from the start
    // expression (`1`) inclusively, to the end expression (`endValue`) exclusively.
    foreach i in 1 ..< endValue {
        sum = sum + i;
    }
    io:println("summation from 1 to " + endValue + " excluding " + endValue + " is " + sum);
}
