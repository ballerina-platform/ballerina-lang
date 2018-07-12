import ballerina/internal;
import ballerina/io;

function main(string... args) {

    string statement = "Lion in Town. Catch the Lion ";

    string s1 = statement.toUpper();
    io:println("ToUpper: " + s1);

    string s2 = statement.toLower();
    io:println("ToLower: " + s2);

    //This compares two strings ignoring the case. It returns 'true' if the strings are equal, or 'false' if they are not equal.
    boolean isEqual =
            statement.equalsIgnoreCase("lion in town. catch the lion ");
    io:println("EqualsIgnoreCase: " + isEqual);

    //This returns a new string that is a substring of the specified string. You must provide the original string,
    //and the starting and ending indexes of the substring.
    string s3 = statement.substring(0, 4);
    io:println("SubString: " + s3);

    boolean contains = statement.contains("Lion");
    io:println("Contains: " + contains);

    //This returns the first index of the first occurrence of the substring within the specified string.
    int index = statement.indexOf("on");
    io:println("IndexOf: " + index);

    //This returns the first index of the last occurrence of the substring within the specified string.
    int lastIndex = statement.lastIndexOf("on");
    io:println("LastIndexOf: " + lastIndex);

    //This converts a value of the float type to a string.
    float value = 5.8;
    string s4 = <string>value;
    io:println("ValueOf: " + s4);

    //This replaces the first instance of the 'replacePattern' string with the 'replaceWith' string.
    string s5 = statement.replaceFirst("Lion", "Tiger");
    io:println("ReplaceFirst: " + s5);

    //This replaces the 'replacePattern' string with the replacement string.
    string s6 = statement.replace("Lion", "Tiger");
    io:println("Replace: " + s6);

    //This replaces each substring of the 'replacePattern' string that matches the given regular expression with the replacement string.
    string s7 = statement.replaceAll("[o]+", "0");
    io:println("ReplaceAll: " + s7);

    //This returns the length of the string.
    int length = statement.length();
    io:println("Length: " + length);

    //This removes any leading and trailing white spaces.
    string s8 = statement.trim();
    io:println("Trim: " + s8);

    //This checks whether the given string ends with the specified suffix.
    boolean hasSuffix = statement.hasSuffix("Lion ");
    io:println("HasSuffix: " + hasSuffix);

    //This checks whether the given string starts with the specified prefix.
    boolean hasPrefix = statement.hasPrefix("Lion");
    io:println("HasPrefix: " + hasPrefix);

    //This returns an unescaped string by omitting the escape characters of the original string.
    string s9 = statement.unescape();
    io:println("Unescape: " + s9);

    //This splits the string with the given regular expression to produce a string array.
    string[] array = statement.split(" ");
    io:println("Split: " + array[0]);
    io:println("Split: " + array[1]);
    io:println("Split: " + array[2]);

    //This converts a string to a byte array.
    byte[] bytes = statement.toByteArray("UTF-8");

    //This converts a value of the byte array to a string.
    string s10 = internal:byteArrayToString(bytes, "UTF-8");
    io:println("Bytes: " + s10);

    //This formats a string according to the given format arguments.
    string s11 = io:sprintf("%s %f", array[0], value);
    io:println("Sprintf: " + s11);
}
