import ballerina/io;

public function main() {
    // This creates an integer array with several integer elements.
    int[] intArray = [1, 2, 3];
    int length;

    // This prints the length of the integer array created. The length is calculated using the `length` function.
    length = intArray.length();
    io:println("Integer array size: ", length);

    // This creates a JSON array with several JSON elements.
    json jsonArray = [
        { "name": "John", "age": 31 },
        { "name": "Neal", "age": 22 }
    ];

    // This prints the length of the created JSON array.
    length = jsonArray.length();
    io:println("JSON array size: ", length);

    // This creates an `int` constrained map.
    map<int> strMap = {};
    strMap["A"] = 1;
    strMap["B"] = 2;
    strMap["C"] = 3;

    // This prints the length of the created map.
    length = strMap.length();
    io:println("Map size: ", length);

    // This creates a string.
    string myStr = "My name is Peter Parker";
    
    // This prints the length of the string.
    length = myStr.length();
    io:println("String size: ", length);

    xml x1 = xml `<book>
                    <name>Sherlock Holmes</name>
                    <author>Sir Arthur Conan Doyle</author>
                    <!--Price: $10-->
                  </book>`;
    length = x1.*.elements().length();
    io:println("XML child elements size: ", length);
}
