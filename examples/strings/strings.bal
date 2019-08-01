import ballerina/io;
import ballerina/lang.'string as strings;

public function main() {

    string statement = "Lion in Town. Catch the Lion";

    string s1 = statement.toUpperAscii();
    io:println("ToUpper: ", s1);

    string s2 = statement.toLowerAscii();
    io:println("ToLower: ", s2);

    // Create a new `string`, which is a substring of the specified string.
    // You must provide the original `string`
    // and the starting and ending indexes of the substring.
    string s3 = statement.substring(0, 4);
    io:println("SubString: ", s3);

    // Retrieve the starting index of the first occurrence of the substring "on" within the `statement` string.
    int? index = statement.indexOf("on");
    if (index is int) {
        io:println("IndexOf: ", index);
    }

    // Retrieve the length of the `string`.
    int length = statement.length();
    io:println("Length: ", length);

    string hello = "Hello";
    string ballerina = "Ballerina!";

    // Concat multiple strings.
    string s4 = hello.concat(" ", ballerina);
    io:println("Concat: ", s4);

    // Join strings with a separator.
    string s5 = ",".'join(hello, ballerina);
    io:println("Join: ", s5);

    // Convert `hello` to a `byte` array.
    byte[] bArray = hello.toBytes();

    // Convert a `byte` array to a `string`.
    string|error s6 = strings:fromBytes(bArray);
    if (s6 is string) {
        io:println("From bytes: ", s6);
    }

    // Remove leading and trailing white spaces.
    string toTrim = "  Ballerina Programming Language  ";
    string s7 = toTrim.trim();
    io:println("Trim: ", s7);

    // Check whether the given `string` ends with the suffix "Lion".
    boolean hasSuffix = statement.endsWith("Lion");
    io:println("HasSuffix: ", hasSuffix);

    // Check whether the given `string` starts with the prefix "Lion".
    boolean hasPrefix = statement.startsWith("Lion");
    io:println("HasPrefix: ", hasPrefix);

    // Format a `string` according to the given format arguments.
    string name = "Sam";
    int marks = 90;
    string[] subjects = ["English", "Science"];
    float average = 71.5;
    string s8 = io:sprintf("%s scored %d for %s and has an average of %.2f.",
     name, marks, subjects[0], average);
    io:println("Sprintf: ", s8);

    // Member access is allowed with strings to access individual characters
    // of a string. Member access panics if the integer index is out of range.
    string country = "Sri Lanka";
    string c = country[4];
    io:println("Member Access: ", c);
}
