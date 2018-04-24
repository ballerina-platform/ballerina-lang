import ballerina/io;

function main (string... args) {
    io:println("Iterating over a string array:-");
    string[] fruits = ["apple", "banana", "cherry"];
    // For arrays: use one variable to get the values defined in the array.
    // Use two variables as comma separated values to get the index and value. For example, `foreach i, v in fruits`.
    foreach v in fruits {
        io:println("fruit: " + v);
    }

    io:println("\nIterating over a map:-");
    map<string> words = { a: "apple", b: "banana", c: "cherry" };
    // For maps: use 1 variable to get the values defined in the map. Use 2 variables to get both the key (string) and
    // value.
    foreach k, v in words {
       io:println("letter: ", k, ", word: ", v);
    }

    io:println("\nIterating over a json object:-");
    json apple = { name: "apple", colors: ["red", "green"], price: 5 };
    // For json: use only a single json typed variable.
    foreach j in apple {
        match j {
            string js => {
                io:println("string value: ", js);
            }
            json jx => { 
                io:println("non-string value: ", jx);
            }
        }
    }

    io:println("\nIterating over a json array:-");
    // To Iterate over a JSON array, you need to first cast it into an array of json (json[]).
    json[] colors = check <json[]> apple.colors;
    foreach i, j in colors {
        io:println("color ", i, ": ", j);
    }

    io:println("\nIterating over an xml:-");
    xml book = xml `<book>
                        <name>Sherlock Holmes</name>
                        <author>Sir Arthur Conan Doyle</author>
                    </book>`;
    // For xml: use one variable to get the xml value. Use two variables to get both the index (int) and xml value.
    foreach i, x in book.*.elements(){
        io:println("xml at ", i, ": ", x);
    }

    io:println("\nIterating over an integer range:-");
    int endValue = 10;
    int sum;
    // An Integer range in the foreach represents an incremental integer value range from the start expression (1)
    // to the end expression (endValue) inclusively.
    foreach i in [1..endValue] {
        sum = sum + i;
    }
    io:println("summation from 1 to " + endValue + " is " + sum);
}
