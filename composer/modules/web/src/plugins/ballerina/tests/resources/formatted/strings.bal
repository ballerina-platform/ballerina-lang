import ballerina/lang.system;
import ballerina/lang.strings;
import ballerina/lang.blobs;

function main (string... args) {

    string statement = "Lion in Town. Catch the Lion ";

    string s1 = strings:toUpperCase(statement);
    system:println("ToUpper: " + s1);

    string s2 = strings:toLowerCase(statement);
    system:println("ToLower: " + s2);

    //Compares two strings, ignoring the case. Returns True if the strings are equal; false otherwise.
    boolean isEqual = strings:equalsIgnoreCase(statement,
                                               "lion in town. catch the lion ");
    system:println("EqualsIgnoreCase: " + isEqual);

    //Returns a new string that is the substring of the specified string. The original string, starting index and
    //ending index should be specified.
    string s3 = strings:subString(statement, 0, 4);
    system:println("SubString: " + s3);

    boolean contains = strings:contains(statement, "Lion");
    system:println("Contains: " + contains);

    //Returns the first index of the first occurrence of the substring within the specified string.
    int index = strings:indexOf(statement, "on");
    system:println("IndexOf: " + index);

    //Returns the first index of the last occurrence of the substring within the specified string.
    int lastIndex = strings:lastIndexOf(statement, "on");
    system:println("LastIndexOf: " + lastIndex);

    //Returns the string representation of the specified 'any' type value.
    string s4 = strings:valueOf(5.8);
    system:println("ValueOf: " + s4);

    //Replaces the first instance of the replacePattern with the replaceWith string.
    string s5 = strings:replaceFirst(statement, "Lion", "Tiger");
    system:println("ReplaceFirst: " + s5);

    //This replaces the replacePattern string with the replacement string.
    string s6 = strings:replace(statement, "Lion", "Tiger");
    system:println("Replace: " + s6);

    //Replaces each substring of the replacePattern that matches the given regular expression with the replacement string.
    string s7 = strings:replaceAll(statement, "[o]+", "0");
    system:println("ReplaceAll: " + s7);

    int length = strings:length(statement);
    system:println("Length: " + length);

    string s8 = strings:trim(statement);
    system:println("Trim: " + s8);

    boolean hasSuffix = strings:hasSuffix(statement, "Lion ");
    system:println("HasSuffix: " + hasSuffix);

    boolean hasPrefix = strings:hasPrefix(statement, "Lion");
    system:println("HasPreffix: " + hasPrefix);

    //Returns an unescaped string by omitting the escape characters of the original string.
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
