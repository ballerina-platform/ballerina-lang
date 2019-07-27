import ballerina/encoding;
import ballerina/io;

public function main() {

    string statement = "Lion in Town. Catch the Lion";

    string s1 = statement.toUpperAscii();
    io:println("ToUpper: ", s1);

    string s2 = statement.toLowerAscii();
    io:println("ToLower: ", s2);

    // Creates a new `string`, which is a substring of the specified string. 
    // You must provide the original `string`
    // and the starting and ending indexes of the substring.
    string s3 = statement.substring(0, 4);
    io:println("SubString: " + s3);

    // Retrieves the starting index of the first occurrence of the substring "on" within the `statement` string.
    int? index = statement.indexOf("on");
    if (index is int) {
        io:println("IndexOf: ", index);
    }
    
    // Retrieves the length of the `string`.
    int length = statement.length();
    io:println("Length: ", length);

    // Removes any leading and trailing white spaces.
    string s8 = statement.trim();
    io:println("Trim: " + s8);

    // Checks whether the given `string` ends with the suffix "Lion".
    boolean hasSuffix = statement.endsWith("Lion");
    io:println("HasSuffix: ", hasSuffix);

    // Checks whether the given `string` starts with the prefix "Lion".
    boolean hasPrefix = statement.startsWith("Lion");
    io:println("HasPrefix: ", hasPrefix);

    // Converts the `string` to a `byte` array.
    byte[] bytes = statement.toBytes();

    // Converts a `byte` array to a string.
    string s10 = encoding:byteArrayToString(bytes);
    io:println("Bytes: " + s10);

    // Formats a `string` according to the given format arguments.
    string name = "Sam";
    int marks = 90;
    string[] subjects = ["English", "Science"];
    float average = 71.5;
    string s11 = io:sprintf("%s scored %d for %s and has an average of %.2f.",
     name, marks, subjects[0], average);
    io:println("Sprintf: " + s11);
}
