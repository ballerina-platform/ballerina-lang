import ballerina/io;

public function main() {
    // Create an integer array with several integer elements.
    int[] intArray = [1, 2, 3];
    int length;

    // Print the length of the created integer array. The length is
    // retrieved using the `.length()` method.
    length = intArray.length();
    io:println("Integer array size: ", length);

    // Create a JSON array with several JSON elements.
    json[] jsonArray = [
        {"name": "John", "age": 31},
        {"name": "Neal", "age": 22}
    ];

    // Print the length of the created JSON array.
    length = jsonArray.length();
    io:println("JSON array size: ", length);

    // Create an `int`-constrained `map`.
    map<int> strMap = {};
    strMap["A"] = 1;
    strMap["B"] = 2;
    strMap["C"] = 3;

    // Print the length of the created `map`.
    length = strMap.length();
    io:println("Map size: ", length);

    // Define a `string`.
    string myStr = "My name is Peter Parker";
    
    // Print the length of the string.
    length = myStr.length();
    io:println("String size: ", length);

    // Create an XML element.
    xml x1 = xml `<book>
                    <name>Sherlock Holmes</name>
                    <author>Sir Arthur Conan Doyle</author>
                    <!--Price: $10-->
                  </book>`;

    // Print the number of content items in the XML element.
    length = (x1/<*>).length();
    io:println("XML child elements size: ", length);

    // Define a tuple with two members.
    [int, string] tupleVar = [10, "John"];

    // Print the size of the tuple (i.e., the number of members in the tuple).
    length = tupleVar.length();
    io:println("Tuple size: ", length);

    // Create a record of the `Student` type.
    Student stu = {id: 1, fname: "John", lname: "Doe", age: 17};

    // Print the number of fields in the created `Student` record.
    length = stu.length();
    io:println("Field size in `Student` record: ", length);
}

type Student record {|
    int id;
    string fname;
    string lname;
    int age;
    string...;
|};
