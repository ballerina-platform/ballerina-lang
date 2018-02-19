package ballerina.builtin;

@Description { value:"Compares two strings, ignoring the case of the strings"}
@Param { value:"mainString: The original string argument" }
@Param { value:"anotherString: The string to be compared" }
@Return { value:"True if the strings are equal; false otherwise" }
documentation {
Compares two strings, ignoring the case of the strings.
- #mainString The original string argument
- #anotherString The string to be compared
- #isEqual True if the strings are equal; false otherwise
}
public native function <string mainString> equalsIgnoreCase (string anotherString) (boolean isEqual);

@Description { value:"Returns a string with all the characters converted to uppercase"}
@Param { value:"mainString: The original string argument" }
@Return { value:"The string converted to uppercase" }
documentation {
Returns a string with all the characters converted to uppercase.
- #mainString The original string argument
- #s The string converted to uppercase
}
public native function <string mainString> toUpperCase () (string s);

@Description { value:"Returns a new string that is the substring of the specified string"}
@Param { value:"mainString: The original string argument" }
@Param { value:"from: The starting index" }
@Param { value:"to: The ending index" }
@Return { value:"The derived sub string" }
documentation {
Returns a new string that is the substring of the specified string.
- #mainString The original string argument
- #from The starting index
- #to The ending index
- #s The derived sub string
}
public native function <string mainString> subString (int from, int to) (string s);

@Description { value:"Returns the first index of the last occurence of the substring within the specified string"}
@Param { value:"mainString: The original string argument" }
@Param { value:"subString: The substring to search for" }
@Return { value:"The index of the last occurence of the substring" }
documentation {
Returns the first index of the last occurence of the substring within the specified string.
- #mainString The original string argument
- #subString  The substring to search for
- #index The index of the last occurence of the substring
}
public native function <string mainString> lastIndexOf (string subString) (int index);

@Description { value:"Replaces the first instance of the replacePattern with the replaceWith string and returns the result"}
@Param { value:"mainString: The original string argument" }
@Param { value:"replacePattern: The pattern to search for " }
@Param { value:"replaceWith: The replacement string" }
@Return { value:"The derived string" }
documentation {
Replaces the first instance of the replacePattern with the replaceWith string and returns the result.
- #mainString The original string argument
- #replacePattern The pattern to search for
- #replaceWith The replacement string
- #s The derived string
}
public native function <string mainString> replaceFirst (string replacePattern, string replaceWith) (string s);

@Description { value:"Returns the length of the specified string "}
@Param { value:"mainString: The original string argument" }
@Return { value:"The length of the specified string" }
documentation {
Returns the length of the specified string.
- #mainString The original string argument
- #len The length of the specified string
}
public native function <string mainString> length () (int len);

@Description { value:"Returns a Boolean value indicating whether a string contains the specified substring"}
@Param { value:"mainString: The original string argument" }
@Param { value:"subString: The substring to be compared" }
@Return { value:"True if the string contains the substring; false otherwise" }
documentation {
Returns a Boolean value indicating whether a string contains the specified substring.
- #mainString The original string argument
- #subString The substring to be compared
- #isContained True if the string contains the substring; false otherwise
}
public native function <string mainString> contains (string subString) (boolean isContained);

@Description { value:"Returns the first index of the first occurence of the substring within the specified string"}
@Param { value:"mainString: The original string argument" }
@Param { value:"subString: The substring to search for" }
@Return { value:"The index of the first occurence of the substring" }
documentation {
Returns a string with all the characters converted to uppercase.
- #mainString The original string argument
- #subString The substring to search for
- #index The index of the first occurence of the substring
}
public native function <string mainString> indexOf (string subString) (int index);

@Description { value:"Returns a trimmed string by omitting the leading and trailing whitespaces of the original string"}
@Param { value:"mainString: The original string argument" }
@Return { value:"The derived string" }
documentation {
Returns a trimmed string by omitting the leading and trailing whitespaces of the original string.
- #mainString The original string argument
- #s The derived string
}
public native function <string mainString> trim () (string s);

@Description { value:"Returns a Boolean value indicating whether the string ends with specified suffix"}
@Param { value:"mainString: The original string argument" }
@Param { value:"suffix: The suffix to be compared" }
@Return { value:"True if the string ends with the suffix; false otherwise" }
documentation {
Returns a Boolean value indicating whether the string ends with specified suffix.
- #mainString The original string argument
- #suffix The suffix to be compared
- #exist True if the string ends with the suffix; false otherwise
}
public native function <string mainString> hasSuffix (string suffix) (boolean exist);

@Description { value:"Returns an unescaped string by omitting the escape characters of the original string"}
@Param { value:"mainString: The original string argument" }
@Return { value:"The derived string" }
documentation {
Returns an unescaped string by omitting the escape characters of the original string.
- #mainString The original string argument
- #s The derived string
}
public native function <string mainString> unescape () (string s);

@Description { value:"Returns a string with all the characters converted to lowercase"}
@Param { value:"mainString: The original string argument" }
@Return { value:"The string converted to lowercase" }
documentation {
Returns a string with all the characters converted to lowercase.
- #mainString The original string argument
- #s The string converted to lowercase
}
public native function <string mainString> toLowerCase () (string s);

@Description { value:"Returns a Boolean value indicating whether a string starts with the specified prefix"}
@Param { value:"mainString: The original string argument" }
@Param { value:"prefix: The prefix to be compared" }
@Return { value:"True if the string starts with the prefix; false otherwise" }
documentation {
Returns a Boolean value indicating whether a string starts with the specified prefix.
- #mainString The original string argument
- #prefix The prefix to be compared
- #exist True if the string starts with the prefix; false otherwise
}
public native function <string mainString> hasPrefix (string prefix) (boolean exist);

@Description { value:"Replaces each substring of the mainString that matches the given regular expression with the given replacement"}
@Param { value:"mainString: The original string argument" }
@Param { value:"replacePattern: The regular expression to search for" }
@Param { value:"replaceWith: The replacement string" }
@Return { value:"The derived string" }
documentation {
Replaces each substring of the mainString that matches the given regular expression with the given replacement.
- #mainString The original string argument
- #replacePattern The regular expression to search for
- #replaceWith The replacement string
- #s The derived string
}
public native function <string mainString> replaceAll (string replacePattern, string replaceWith) (string s);

@Description { value:"Replaces all instances of the replacePattern string with the replaceWith string and returns the result"}
@Param { value:"mainString: The original string argument" }
@Param { value:"replacePattern: The pattern to search for" }
@Param { value:"replaceWith: The replacement string" }
@Return { value:"The derived string" }
documentation {
Replaces all instances of the replacePattern string with the replaceWith string and returns the result.
- #mainString The original string argument
- #replacePattern The pattern to search for
- #replaceWith The replacement string
- #s The derived string
}
public native function <string mainString> replace (string replacePattern, string replaceWith) (string s);

@Description { value:"Splits the string with the given regular expression to produce a string array."}
@Param { value:"mainString: The original string argument" }
@Param { value:"regex: The regex to split the string" }
@Return { value:"The split string array" }
documentation {
Splits the string with the given regular expression to produce a string array.
- #mainString The original string argument
- #regex The regex to split the string
- #sArray The split string array
}
public native function <string mainString> split (string regex) (string[] sArray);

@Description { value:"Converts string to a blob"}
@Param { value:"mainString: string value to be converted" }
@Param { value:"encoding: Encoding to used in the conversion" }
@Return { value:"The blob representation of the given String" }
documentation {
Converts string to a blob.
- #mainString string value to be converted
- #encoding Encoding to used in the conversion
- #sBlob The blob representation of the given String
}
public native function <string mainString> toBlob (string encoding) (blob sBlob);

@Description { value: "Represents a Regular expression in ballerina and can perform various Regular expression methods."}
@Field { value : "regex:Pattern as a String"}
public struct Regex {
   string pattern;
}

@Description { value:"Finds all the strings matching the regular expression"}
@Param { value:"mainString: The original string argument" }
@Param { value:"reg: Regular expression" }
@Return { value: "The matching string array"}
@Return { value:"error: Error will be returned if there exist a syntax error in pattern" }
documentation {
Finds all the strings matching the regular expression.
- #mainString The original string argument
- #reg Regular expression
- #sArray The matching string array
- #err Error will be returned if there exist a syntax error in pattern
}
public native function <string mainString> findAllWithRegex (Regex reg) (string[] sArray, error err);

@Description { value:"Returns a Boolean value indicating whether the string matches the regular expression"}
@Param { value:"mainString: The original string argument" }
@Param { value:"reg: Regular expression" }
@Return { value: "True if the string matches the regex; false otherwise"}
@Return { value:"error: Error will be returned if there exist a syntax error in pattern" }
documentation {
Returns a Boolean value indicating whether the string matches the regular expression.
- #mainString The original string argument
- #reg Regular expression
- #matches True if the string matches the regex; false otherwise
- #err Error will be returned if there exist a syntax error in pattern
}
public native function <string mainString>  matchesWithRegex (Regex reg) (boolean matches, error err);

@Description { value:"Replaces the mainString with the replacement of occurrences that matches the given regular expression" }
@Param { value:"mainString: The original string argument" }
@Param { value:"reg: Regular expression" }
@Param { value:"replaceWith: The replacement string" }
@Return { value:"The derived string" }
@Return { value:"error: Error will be returned if there exist a syntax error in pattern" }
documentation {
Replaces the mainString with the replacement of occurrences that matches the given regular expression.
- #mainString The original string argument
- #reg Regular expression
- #replaceWith The replacement string
- #s The derived string
- #err Error will be returned if there exist a syntax error in pattern
}
public native function <string mainString> replaceAllWithRegex (Regex reg, string replaceWith) (string s, error err);

@Description { value:"Replaces the first instance of the regular expression matching area with the replaceWith string and returns the result"}
@Param { value:"mainString: The original string argument" }
@Param { value:"reg: Regular expression" }
@Param { value:"replaceWith: The replacement string" }
@Return { value:"The derived string" }
@Return { value:"error: Error will be returned if there exist a syntax error in pattern" }
documentation {
Replaces the first instance of the regular expression matching area with the replaceWith string and returns the result.
- #mainString The original string argument
- #reg Regular expression
- #replaceWith The replacement string
- #s The derived string
- #err Error will be returned if there exist a syntax error in pattern
}
public native function <string mainString> replaceFirstWithRegex (Regex reg, string replaceWith) (string s, error err);
