import ballerina/io;

public function main() {
    // This creates an integer array with several integer elements.
    int[] intArray = [1, 2, 3];
    int lengthVal;

    // This prints the length of the integer array created. The length is calculated using the `lengthof` unary operator.
    lengthVal = lengthof intArray;
    io:println("Integer array size: ", lengthVal);

    // This creates a JSON array with several JSON elements.
    json jsonArray = [
        { "name": "John", "age": 31 },
        { "name": "Neal", "age": 22 }
    ];

    // This prints the length of the created JSON array.
    lengthVal = lengthof jsonArray;
    io:println("JSON array size: ", lengthVal);

    // This creates an `int` constrained map.
    map<int> strMap;
    strMap["A"] = 1;
    strMap["B"] = 2;
    strMap["C"] = 3;

    // This prints the lengthVal of the created map.
    lengthVal = lengthof strMap;
    io:println("Map size: ", lengthVal);

    // This creates a string.
    string myStr = "My name is Peter Parker";
    
    // This prints the length of the string.
    lengthVal = lengthof myStr;
    io:println("String size: ", lengthVal);

    xml x1 = xml `<book>
                    <name>Sherlock Holmes</name>
                    <author>Sir Arthur Conan Doyle</author>
                    <!--Price: $10-->
                  </book>`;
    lengthVal = lengthof x1.*.elements();
    io:println("XML child elements size: ", lengthVal);
}
