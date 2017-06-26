import ballerina.lang.system;
import ballerina.lang.strings;
import ballerina.lang.blobs;

function main (string[] args) {

    string statement = "Lion in Town. Catch the Lion ";

    //This returns a string with all the characters converted to uppercase
    string s1 = strings:toUpperCase(statement);
    system:println("ToUpper: " + s1);

    //This returns a string with all the characters converted to lowercase
    string s2 = strings:toLowerCase(statement);
    system:println("ToLower: " + s2);

    //Compares two strings, ignoring the case of the strings. True if the strings are equal; false otherwise
    boolean isEqual = strings:equalsIgnoreCase(statement,
                                              "lion in town. catch the lion ");
    system:println("EqualsIgnoreCase: " + isEqual);

    //This returns a new string that is the substring of the specified string. The main string, starting index and
    //ending index should be specified.
    string s3 = strings:subString(statement, 0, 4);
    system:println("SubString: " + s3);

    //This returns a Boolean value indicating whether a string contains the specified substring
    boolean contains = strings:contains(statement, "Lion");
    system:println("Contains: " + contains);

    //Returns the first index of the first occurence of the substring within the specified string
    int index = strings:indexOf(statement, "on");
    system:println("IndexOf: " + index);

    //Returns the first index of the last occurence of the substring within the specified string
    int lastIndex = strings:lastIndexOf(statement, "on");
    system:println("LastIndexOf: " + lastIndex);

    //This is a String representation of the specified 'any' type argument
    string s4 = strings:valueOf(5.8);
    system:println("ValueOf: " + s4);

    //Replaces the first instance of the replacePattern with the replaceWith string and returns the result
    string s5 = strings:replaceFirst(statement, "Lion", "Tiger");
    system:println("ReplaceFirst: " + s5);

    //This replaces the replacePattern string with the replacement string and returns the result
    string s6 = strings:replace(statement, "Lion", "Tiger");
    system:println("Replace: " + s6);

    //Replaces each substring of the replacePattern that matches the given regular expression with the replacement string
    string s7 = strings:replaceAll(statement, "[o]+", "0");
    system:println("ReplaceAll: " + s7);

    //Returns the length of the specified string
    int length = strings:length(statement);
    system:println("Length: " + length);

    //Returns a trimmed string by omitting the leading and trailing whitespaces of the original string
    string s8 = strings:trim(statement);
    system:println("Trim: " + s8);

    //This returns a Boolean value indicating whether the string ends with specified suffix
    boolean hasSuffix = strings:hasSuffix(statement, "Lion ");
    system:println("HasSuffix: " + hasSuffix);

    //Returns a Boolean value indicating whether a string starts with the specified prefix
    boolean hasPrefix = strings:hasPrefix(statement, "Lion");
    system:println("HasPreffix: " + hasPrefix);

    //Returns an unescaped string by omitting the escape characters of the original string
    string s9 = strings:unescape(statement);
    system:println("Unescape: " + s9);

    //Splits the string with the given regular expression to produce a string array
    string[] array = strings:split(statement, " ");
    system:println("Split: " + array[0]);
    system:println("Split: " + array[1]);
    system:println("Split: " + array[2]);

    //Converts string to a BLOB
    blob blobValue = strings:toBlob(statement, "UTF-8");
    //Converts the BLOB to the string
    string s10 = blobs:toString(blobValue, "UTF-8");
    system:println("Blob: " + s10);



}
