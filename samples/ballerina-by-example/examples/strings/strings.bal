function main (string[] args) {

    string statement = "Lion in Town. Catch the Lion ";

    string s1 = statement.toUpperCase();
    println("ToUpper: " + s1);

    string s2 = statement.toLowerCase();
    println("ToLower: " + s2);

    //Compares two strings, ignoring the case. Returns True if the strings are equal; false otherwise.
    boolean isEqual = statement.equalsIgnoreCase("lion in town. catch the lion ");
    println("EqualsIgnoreCase: " + isEqual);

    //Returns a new string that is the substring of the specified string. The original string, starting index and
    //ending index should be specified.
    string s3 = statement.subString(0, 4);
    println("SubString: " + s3);

    boolean contains = statement.contains("Lion");
    println("Contains: " + contains);

    //Returns the first index of the first occurrence of the substring within the specified string.
    int index = statement.indexOf("on");
    println("IndexOf: " + index);

    //Returns the first index of the last occurrence of the substring within the specified string.
    int lastIndex = statement.lastIndexOf("on");
    println("LastIndexOf: " + lastIndex);

    //Returns the string representation of the specified 'any' type value.
    float value = 5.8;
    string s4 = <string>value;
    println("ValueOf: " + s4);

    //Replaces the first instance of the replacePattern with the replaceWith string.
    string s5 = statement.replaceFirst("Lion", "Tiger");
    println("ReplaceFirst: " + s5);

    //This replaces the replacePattern string with the replacement string.
    string s6 = statement.replace("Lion", "Tiger");
    println("Replace: " + s6);

    //Replaces each substring of the replacePattern that matches the given regular expression with the replacement string.
    string s7 = statement.replaceAll("[o]+", "0");
    println("ReplaceAll: " + s7);

    int length = statement.length();
    println("Length: " + length);

    string s8 = statement.trim();
    println("Trim: " + s8);

    boolean hasSuffix = statement.hasSuffix("Lion ");
    println("HasSuffix: " + hasSuffix);

    boolean hasPrefix = statement.hasPrefix("Lion");
    println("HasPreffix: " + hasPrefix);

    //Returns an unescaped string by omitting the escape characters of the original string.
    string s9 = statement.unescape();
    println("Unescape: " + s9);

    //Splits the string with the given regular expression to produce a string array.
    string[] array = statement.split(" ");
    println("Split: " + array[0]);
    println("Split: " + array[1]);
    println("Split: " + array[2]);

    //Converts string to a BLOB.
    blob blobValue = statement.toBlob("UTF-8");

    //Converts the BLOB to the string.
    string s10 = blobValue.toString("UTF-8");
    println("Blob: " + s10);
}
