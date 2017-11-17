package ballerina.builtin;

@Description { value:"Compares two strings, ignoring the case of the strings"}
@Param { value:"mainString: The original string argument" }
@Param { value:"anotherString: The string to be compared" }
@Return { value:"True if the strings are equal; false otherwise" }
public native function <string mainString> equalsIgnoreCase (string anotherString) (boolean);

@Description { value:"Returns a string with all the characters converted to uppercase"}
@Param { value:"mainString: The original string argument" }
@Return { value:"The string converted to uppercase" }
public native function <string mainString> toUpperCase () (string);

@Description { value:"Returns a new string that is the substring of the specified string"}
@Param { value:"mainString: The original string argument" }
@Param { value:"from: The starting index" }
@Param { value:"to: The ending index" }
@Return { value:"The derived sub string" }
public native function <string mainString> subString (int from, int to) (string);

@Description { value:"Returns the first index of the last occurence of the substring within the specified string"}
@Param { value:"mainString: The original string argument" }
@Param { value:"subString: The substring to search for" }
@Return { value:"The index of the last occurence of the substring" }
public native function <string mainString> lastIndexOf (string subString) (int);

@Description { value:"Replaces the first instance of the replacePattern with the replaceWith string and returns the result"}
@Param { value:"mainString: The original string argument" }
@Param { value:"replacePattern: The pattern to search for " }
@Param { value:"replaceWith: The replacement string" }
@Return { value:"The derived string" }
public native function <string mainString> replaceFirst (string replacePattern, string replaceWith) (string);

@Description { value:"Returns the length of the specified string "}
@Param { value:"mainString: The original string argument" }
@Return { value:"The length of the specified string" }
public native function <string mainString> length () (int);

@Description { value:"Returns a Boolean value indicating whether a string contains the specified substring"}
@Param { value:"mainString: The original string argument" }
@Param { value:"subString: The substring to be compared" }
@Return { value:"True if the string contains the substring; false otherwise" }
public native function <string mainString> contains (string subString) (boolean);

@Description { value:"Returns the first index of the first occurence of the substring within the specified string"}
@Param { value:"mainString: The original string argument" }
@Param { value:"subString: The substring to search for" }
@Return { value:"The index of the first occurence of the substring" }
public native function <string mainString> indexOf (string subString) (int);

@Description { value:"Returns a trimmed string by omitting the leading and trailing whitespaces of the original string"}
@Param { value:"mainString: The original string argument" }
@Return { value:"The derived string" }
public native function <string mainString> trim () (string);

@Description { value:"Returns a Boolean value indicating whether the string ends with specified suffix"}
@Param { value:"mainString: The original string argument" }
@Param { value:"suffix: The suffix to be compared" }
@Return { value:"True if the string ends with the suffix; false otherwise" }
public native function <string mainString> hasSuffix (string suffix) (boolean);

@Description { value:"Returns an unescaped string by omitting the escape characters of the original string"}
@Param { value:"mainString: The original string argument" }
@Return { value:"The derived string" }
public native function <string mainString> unescape () (string);

@Description { value:"Returns a string with all the characters converted to lowercase"}
@Param { value:"mainString: The original string argument" }
@Return { value:"The string converted to lowercase" }
public native function <string mainString> toLowerCase () (string);

@Description { value:"Returns a Boolean value indicating whether a string starts with the specified prefix"}
@Param { value:"mainString: The original string argument" }
@Param { value:"prefix: The prefix to be compared" }
@Return { value:"True if the string starts with the prefix; false otherwise" }
public native function <string mainString> hasPrefix (string prefix) (boolean);

@Description { value:"Replaces each substring of the mainString that matches the given regular expression with the given replacement"}
@Param { value:"mainString: The original string argument" }
@Param { value:"replacePattern: The regular expression to search for" }
@Param { value:"replaceWith: The replacement string" }
@Return { value:"The derived string" }
public native function <string mainString> replaceAll (string replacePattern, string replaceWith) (string);

@Description { value:"Replaces all instances of the replacePattern string with the replaceWith string and returns the result"}
@Param { value:"mainString: The original string argument" }
@Param { value:"replacePattern: The pattern to search for" }
@Param { value:"replaceWith: The replacement string" }
@Return { value:"The derived string" }
public native function <string mainString> replace (string replacePattern, string replaceWith) (string);

@Description { value:"Splits the string with the given regular expression to produce a string array."}
@Param { value:"mainString: The original string argument" }
@Param { value:"regex: The regex to split the string" }
@Return { value:"The split string array" }
public native function <string mainString> split (string regex) (string[]);

@Description { value:"Converts string to a blob"}
@Param { value:"mainString: string value to be converted" }
@Param { value:"encoding: Encoding to used in the conversion" }
@Return { value:"The blob representation of the given String" }
public native function <string mainString> toBlob (string encoding) (blob);
