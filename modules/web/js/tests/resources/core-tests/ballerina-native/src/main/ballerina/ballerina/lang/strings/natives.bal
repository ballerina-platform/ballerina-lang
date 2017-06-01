package ballerina.lang.strings;

import ballerina.doc;

@doc:Description { value:"Returns a string with all the characters converted to uppercase"}
@doc:Param { value:"s: The original string argument" }
@doc:Return { value:"string: The string converted to uppercase" }
native function toUpperCase (string s) (string);

@doc:Description { value:"Returns a string representation of an XML argument"}
@doc:Param { value:"e: The XML object argument" }
@doc:Return { value:"string: String representation of the specified XML object" }
native function valueOf (xml e) (string);

@doc:Description { value:"Returns a new string that is the substring of the specified string"}
@doc:Param { value:"mainString: The original string argument" }
@doc:Param { value:"from: The starting index" }
@doc:Param { value:"to: The ending index" }
@doc:Return { value:"string: The derived sub string" }
native function subString (string mainString, int from, int to) (string);

@doc:Description { value:"Compares two strings, ignoring the case of the strings"}
@doc:Param { value:"mainString: The original string argument" }
@doc:Param { value:"anotherString: The string to be compared" }
@doc:Return { value:"boolean: True if the strings are equal; false otherwise" }
native function equalsIgnoreCase (string mainString, string anotherString) (boolean);

@doc:Description { value:"Returns the first index of the last occurence of the substring within the specified string"}
@doc:Param { value:"mainString: The original string argument" }
@doc:Param { value:"subString: The substring to search for" }
@doc:Return { value:"int: The index of the last occurence of the substring" }
native function lastIndexOf (string mainString, string subString) (int);

@doc:Description { value:"Returns a string representation of an integer argument"}
@doc:Param { value:"i: An integer argument" }
@doc:Return { value:"string: String representation of the specified integer argument" }
native function valueOf (int i) (string);

@doc:Description { value:"Returns a string representation of a JSON argument"}
@doc:Param { value:"value: A JSON argument" }
@doc:Return { value:"string: String representation of the specified JSON argument" }
native function valueOf (json value) (string);

@doc:Description { value:"Replaces the first instance of the replacePattern with the replaceWith string and returns the result"}
@doc:Param { value:"mainString: The original string argument" }
@doc:Param { value:"replacePattern: The pattern to search for " }
@doc:Param { value:"replaceWith: The replacement string" }
@doc:Return { value:"string: The derived string" }
native function replaceFirst (string mainString, string replacePattern, string replaceWith) (string);

@doc:Description { value:"Returns the length of the specified string "}
@doc:Param { value:"s: The original string argument" }
@doc:Return { value:"int: The length of the specified string" }
native function length (string s) (int);

@doc:Description { value:"Returns a Boolean value indicating whether a string contains the specified substring"}
@doc:Param { value:"mainString: The original string argument" }
@doc:Param { value:"subString: The substring to be compared" }
@doc:Return { value:"boolean: True if the string contains the substring; false otherwise" }
native function contains (string mainString, string subString) (boolean);

@doc:Description { value:"Returns the first index of the first occurence of the substring within the specified string"}
@doc:Param { value:"mainString: The original string argument" }
@doc:Param { value:"subString: The substring to search for" }
@doc:Return { value:"int: The index of the first occurence of the substring" }
native function indexOf (string mainString, string subString) (int);

@doc:Description { value:"Returns a string representation of a float argument"}
@doc:Param { value:"f: A float argument" }
@doc:Return { value:"string: String representation of the specified float argument" }
native function valueOf (float f) (string);

@doc:Description { value:"Returns a string representation of a Boolean argument"}
@doc:Param { value:"b: A Boolean argument" }
@doc:Return { value:"string: String representation of the specified Boolean argument" }
native function valueOf (boolean b) (string);

@doc:Description { value:"Returns a trimmed string by omitting the leading and trailing whitespaces of the original string"}
@doc:Param { value:"s: The original string argument" }
@doc:Return { value:"string: The derived string" }
native function trim (string s) (string);

@doc:Description { value:"Returns a Boolean value indicating whether the string ends with specified suffix"}
@doc:Param { value:"mainString: The original string argument" }
@doc:Param { value:"suffix: The suffix to be compared" }
@doc:Return { value:"boolean: True if the string ends with the suffix; false otherwise" }
native function hasSuffix (string mainString, string suffix) (boolean);

@doc:Description { value:"Returns an unescaped string by omitting the escape characters of the original string"}
@doc:Param { value:"s: The original string argument" }
@doc:Return { value:"string: The derived string" }
native function unescape (string s) (string);

@doc:Description { value:"Returns a string with all the characters converted to lowercase"}
@doc:Param { value:"s: The original string argument" }
@doc:Return { value:"string: The string converted to lowercase" }
native function toLowerCase (string s) (string);

@doc:Description { value:"Returns a Boolean value indicating whether a string starts with the specified prefix"}
@doc:Param { value:"mainString: The original string argument" }
@doc:Param { value:"prefix: The prefix to be compared" }
@doc:Return { value:"boolean: True if the string starts with the prefix; false otherwise" }
native function hasPrefix (string mainString, string prefix) (boolean);

@doc:Description { value:"Replaces all instances of the replacePattern with the replaceWith string and returns the result"}
@doc:Param { value:"mainString: The original string argument" }
@doc:Param { value:"replacePattern: The pattern to search for " }
@doc:Param { value:"replaceWith: The replacement string" }
@doc:Return { value:"string: The derived string" }
native function replaceAll (string mainString, string replacePattern, string replaceWith) (string);

@doc:Description { value:"Replaces the replacePattern string with the replaceWith string and returns the result"}
@doc:Param { value:"mainString: The original string argument" }
@doc:Param { value:"replacePattern: The pattern to search for " }
@doc:Param { value:"replaceWith: The replacement string" }
@doc:Return { value:"string: The derived string" }
native function replace (string mainString, string replacePattern, string replaceWith) (string);

@doc:Description { value:"Returns the string representation of a string argument"}
@doc:Param { value:"s: The specified string argument" }
@doc:Return { value:"string: String representation of the specified string argument" }
native function valueOf (string s) (string);

@doc:Description { value:"Splits the string with the given regular expression to produce a string array."}
@doc:Param { value:"mainString: The original string argument" }
@doc:Param { value:"regex: The regex to split the string" }
@doc:Return { value:"string[]: The split string array" }
native function split (string mainString, string regex) (string[]);