import ballerina/encoding;
import ballerina/io;

public function main() {

    string statement = "Lion in Town. Catch the Lion";

    string s1 = statement.toUpper();
    io:println("ToUpper: " + s1);

    string s2 = statement.toLower();
    io:println("ToLower: " + s2);

    // Compare stwo strings ignoring the case.
    // Returns `true` if the strings are equal or `false` if they are not equal.
    boolean isEqual =
            statement.equalsIgnoreCase("lion in town. catch the lion");
    io:println("EqualsIgnoreCase: " + isEqual);

    // Creates a new `string`, which is a substring of the specified string. 
    // You must provide the original `string`
    // and the starting and ending indexes of the substring.
    string s3 = statement.substring(0, 4);
    io:println("SubString: " + s3);

    // Checks if the `statement` string contains the string "Lion".
    boolean contains = statement.contains("Lion");
    io:println("Contains: " + contains);

    // Retrieves the starting index of the first occurrence of the substring "on" within the `statement` string.
    int index = statement.indexOf("on");
    io:println("IndexOf: " + index);

    // Retrieves the starting index of the last occurrence of the substring "on" within the `statement` string.
    int lastIndex = statement.lastIndexOf("on");
    io:println("LastIndexOf: " + lastIndex);

    // Replaces the first instance of the string "Lion" in the `statement` string with the string "Tiger".
    string s5 = statement.replaceFirst("Lion", "Tiger");
    io:println("ReplaceFirst: " + s5);

    // Replaces the string "Lion" in the `statement` string with the string "Tiger".
    string s6 = statement.replace("Lion", "Tiger");
    io:println("Replace: " + s6);

    // Replaces each substring of the `statement` string, which matches the given
    // regular expression with the replacement string "0".
    string s7 = statement.replaceAll("[o]+", "0");
    io:println("ReplaceAll: " + s7);

    // Retrieves the length of the `string`.
    int length = statement.length();
    io:println("Length: " + length);

    // Removes any leading and trailing white spaces.
    string s8 = statement.trim();
    io:println("Trim: " + s8);

    // Checks whether the given `string` ends with the suffix "Lion".
    boolean hasSuffix = statement.hasSuffix("Lion");
    io:println("HasSuffix: " + hasSuffix);

    // Checks whether the given `string` starts with the prefix "Lion".
    boolean hasPrefix = statement.hasPrefix("Lion");
    io:println("HasPrefix: " + hasPrefix);

    // Retrieves an unescaped string by omitting the escape characters of the original string.
    string s9 = statement.unescape();
    io:println("Unescape: " + s9);

    // Splits the `string` based on the given regular expression to produce a String array.
    string[] array = statement.split(" ");
    io:println("Split: " + array[0]);
    io:println("Split: " + array[1]);
    io:println("Split: " + array[2]);

    // Converts the `string` to a `byte` array.
    byte[] bytes = statement.toByteArray("UTF-8");

    // Converts a `byte` array to a string.
    string s10 = encoding:byteArrayToString(bytes);
    io:println("Bytes: " + s10);

    // Formats a `string` according to the given format arguments.
    float value = 5.8;
    string s11 = io:sprintf("%s %f", array[0], value);
    io:println("Sprintf: " + s11);
}
