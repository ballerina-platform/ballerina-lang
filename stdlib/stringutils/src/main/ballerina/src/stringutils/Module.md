## Module overview

This module provides utility functions to manipulate built-in `string` data type. 

### Examples

#### Compare two strings ignoring the case
```ballerina
import ballerina/io;
import ballerina/stringutils;

function checkStrings(string str1, string str2) {
        io:print("String " + str1 + " and " + str2 + " are ");
    if (stringutils:equalsIgnoreCase(str1, str2)) {
        io:println(" equal");
    } else {
        io:println(" not equal");
    }
}
```

#### Replace a substring in a string, using a regex
```ballerina
import ballerina/stringutils;

function replaceByRegex() {
    string str = "OriginalPPPString";
    string regex = "PPP";
    string newString = stringutils:replaceAll(str, regex, " ");
    // newString will be 'Original String'
}
```

#### Split a string
```ballerina
import ballerina/stringutils;

function splitString() {
    string str = "Sam,Ru,Supun";
    string[] result = stringutils:split(str, ",");
    // result will be {"Sam", "Ru", "Supun"}
}
```
