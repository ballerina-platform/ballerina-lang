## Module overview

This module provides utility functions to manipulate built-in `string` data type. 

### Samples

#### Compare two strings ignoring the case
```ballerina
import ballerina/io;
import ballerina/stringutils;

public function main() {
    string str1 = "Comparing String";
    string str2 = "cOmpaRinG sTrinG";
    io:print("String '" + str1 + "' and '" + str2 + "' are");
    if (stringutils:equalsIgnoreCase(str1, str2)) {
        io:println(" equal");
    } else {
        io:println(" not equal");
    }
}
```

#### Replace a substring in a string, using a regex
```ballerina
import ballerina/io;
import ballerina/stringutils;
 
public function main() {
    string str = "OriginalPPPString";
    string regex = "P+";
    string newString = stringutils:replaceAll(str, regex, " ");
    // newString will be 'Original String'
    io:println(newString);
}
```

#### Split a string
```ballerina
import ballerina/io;
import ballerina/stringutils;

public function main() {
    string stringsArray = "Sam,Ru,Supun";
    string[] result = stringutils:split(stringsArray, ",");
    // result will be {"Sam", "Ru", "Supun"}
    foreach var str in result {
        io:println(str);
    }
}
```
