import ballerina/io;

function main(string... args) {
    // This creates an integer array with several integer elements.
    int[] intArray = [1, 2, 3];
    int length;

    // This prints the length of the integer array created. The length is calculated using the `lengthof` unary operator.
    length = lengthof intArray;
    io:println("Integer array size: ", length);

    // This creates a JSON array with several JSON elements.
    json jsonArray = [
        { "name": "John", "age": 31 },
        { "name": "Neal", "age": 22 }
    ];

    // This prints the length of the created JSON array.
    length = lengthof jsonArray;
    io:println("JSON array size: ", length);

    // This creates an `int` constrained map.
    map<int> strMap;
    strMap["A"] = 1;
    strMap["B"] = 2;
    strMap["C"] = 3;

    // This prints the length of the created map.
    length = lengthof jsonArray;
    io:println("Map size: ", length);

    // This creates a string.
    string myStr = "My name is Peter Parker";
    
    // This prints the length of the string.
    length = lengthof myStr;
    io:println("String size: ", length);

    xml x1 = xml `<book>
                    <name>Sherlock Holmes</name>
                    <author>Sir Arthur Conan Doyle</author>
                    <!--Price: $10-->
                  </book>`;
    length = lengthof x1.*.elements();
    io:println("XML child elements size: ", length);
 
    // This creates a `blob` value.
    blob bl = myStr.toBlob("UTF-8");
    // Print the length of the `blob` value.
    length = lengthof bl;
    io:println("BLOB size: ", length);
}
