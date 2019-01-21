import ballerina/io;

public function main() {
    // This creates an integer array with several integer elements.
    int[] intArray = [1, 2, 3];
    int length;

    // This prints the length of the integer array created. The length is 
    // calculated using the `length()` function.
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

    // This creates an `int` constrained `map`.
    map<int> strMap = {};
    strMap["A"] = 1;
    strMap["B"] = 2;
    strMap["C"] = 3;

    // This prints the length of the created `map`.
    length = strMap.length();
    io:println("Map size: ", length);

    // This creates a `string`.
    string myStr = "My name is Peter Parker";
    
    // This prints the length of the `string`.
    length = myStr.length();
    io:println("String size: ", length);

    // This creates an XML element.
    xml x1 = xml `<book>
                    <name>Sherlock Holmes</name>
                    <author>Sir Arthur Conan Doyle</author>
                    <!--Price: $10-->
                  </book>`;

    // This prints the number of content items in the XML element.
    length = x1.*.elements().length();
    io:println("XML child elements size: ", length);

    // This creates a tuple with two members.
    (int, string) tupleVar = (10, "John");

    // This prints the size of the tuple.
    length = tupleVar.length();
    io:println("Tuple size: ", length);

    // This creates a `record` of `Student` type
    Student stu = { id: 1, fname: "John", lname: "Doe", age: 17};

    // This prints the number of fields of the `Student` record type.
    length = stu.length();
    io:println("Field size in `Student` record: ", length);

    // This creates an in-memory `table` constrained by the `Student` type.
    table<Student> tbStudent = table {
        { key id, fname, lname, age },
        [ { 1, "Mary", "Popins" , 17 },
          { 2, "John", "Doe", 16 },
          { 3, "Jim", "Carter", 17 }
        ]
    };

    // This prints the number of rows of the in-memory table constrained table.
    length = tbStudent.length();
    io:println("Row count in table : ", length);
}

type Student record {
    int id;
    string fname;
    string lname;
    int age;
    string...;
};
