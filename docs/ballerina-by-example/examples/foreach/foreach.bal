function main (string[] args) {
    println("Iterating over a string array");
    string[] fruits = ["apple", "banana", "cherry"];
    // For arrays: Use 1 variable to get only the value. Use 2 variables to get both index (int) and value.
    foreach v in fruits {
        println("fruit: " + v);
    }

    println("\nIterating over a map.");
    map words = {a:"apple", b:"banana", c:"cherry"};
    // For maps: Use 1 variable to get only the value. Use 2 variables to get both key (string) and value.
    foreach k, v in words {
        var value, _ = (string)v;
        println("letter: " + k + ", word: " + value);
    }

    println("\nIterating over a json object");
    json apple = {name:"apple", colors:["red", "green"], price:5};
    // For json: Use only single json type variable
    foreach j in apple{
        println("value: " + j.toString());
    }

    println("\nIterating over a json array");
    // To Iterate over JSON array, first cast it into an array of json (json[]).
    var colors, _ = (json[]) apple.colors;
    foreach i, j in colors{
        println("color " + i + " : " + j.toString());
    }

    println("\nIterating over a xml");
    xml book = xml `<book>
                        <name>Sherlock Holmes</name>
                        <author>Sir Arthur Conan Doyle</author>
                    </book>`;
    // For xml: Use 1 variable to get the xml value. Use 2 variables to get both index (int) and xml value.
    foreach i, x in book.children().elements(){
        println("xml at " + i + " : " + <string> x);
    }

    println("\nIterating over an integer range");
    int endValue = 10;
    int sum;
    // An Integer range in foreach represents an incremental integer value range from the start expression (1) to the end expression (endValue) inclusively. 
    foreach i in 1..endValue {
        sum = sum + i;
    }
    println("summation from 1 to " + endValue + " is :" + sum);
}
