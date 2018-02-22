import ballerina.io;

function main (string[] args) {

    string statement = "Lion in Town. Catch the Lion ";

    string s1 = statement.toUpperCase();
    io:println("ToUpper: " + s1);

    string s2 = statement.toLowerCase();
    io:println("ToLower: " + s2);

    //Compares two strings, ignoring the case. Returns True if the strings are equal; false otherwise.
    boolean isEqual = statement.equalsIgnoreCase("lion in town. catch the lion ");
    io:println("EqualsIgnoreCase: " + isEqual);

    //Returns a new string that is the substring of the specified string. The original string, starting index and
    //ending index should be specified.
    string s3 = statement.subString(0, 4);
    io:println("SubString: " + s3);

    boolean contains = statement.contains("Lion");
    io:println("Contains: " + contains);

    //Returns the first index of the first occurrence of the substring within the specified string.
    int index = statement.indexOf("on");
    io:println("IndexOf: " + index);

    //Returns the first index of the last occurrence of the substring within the specified string.
    int lastIndex = statement.lastIndexOf("on");
    io:println("LastIndexOf: " + lastIndex);

    //Converts a float type into a string.
    float value = 5.8;
    string s4 = <string>value;
    io:println("ValueOf: " + s4);

    //Replaces the first instance of the replacePattern with the replaceWith string.
    string s5 = statement.replaceFirst("Lion", "Tiger");
    io:println("ReplaceFirst: " + s5);

    //This replaces the replacePattern string with the replacement string.
    string s6 = statement.replace("Lion", "Tiger");
    io:println("Replace: " + s6);

    //Replaces each substring of the replacePattern that matches the given regular expression with the replacement string.
    string s7 = statement.replaceAll("[o]+", "0");
    io:println("ReplaceAll: " + s7);

    //Returns the length of the string.
    int length = statement.length();
    io:println("Length: " + length);

    //Remove any leading and trailing white spaces
    string s8 = statement.trim();
    io:println("Trim: " + s8);

    //Tests if this string ends with the specified suffix.
    boolean hasSuffix = statement.hasSuffix("Lion ");
    io:println("HasSuffix: " + hasSuffix);

    //Tests if this string starts with the specified prefix.
    boolean hasPrefix = statement.hasPrefix("Lion");
    io:println("HasPreffix: " + hasPrefix);

    //Returns an unescaped string by omitting the escape characters of the original string.
    string s9 = statement.unescape();
    io:println("Unescape: " + s9);

    //Splits the string with the given regular expression to produce a string array.
    string[] array = statement.split(" ");
    io:println("Split: " + array[0]);
    io:println("Split: " + array[1]);
    io:println("Split: " + array[2]);

    //Converts string to a BLOB.
    blob blobValue = statement.toBlob("UTF-8");

    //Converts the BLOB to the string.
    string s10 = blobValue.toString("UTF-8");
    io:println("Blob: " + s10);
}
