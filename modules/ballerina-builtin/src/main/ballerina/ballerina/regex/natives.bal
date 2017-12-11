package ballerina.regex;

@Description { value: "Represents a Regular expression in ballerina and can perform various Regular expression methods."}
@Field { value : "regex: Regular expression as a String"}
public struct Regex {
   string regex;
}

@Description { value:"Parse Regular Expression string"}
@Param { value:"reg: The Regular Expression" }
public native function <Regex reg> compile ();

@Description { value:"Finds all the strings matching the regular expression"}
@Param { value:"reg: The Regular Expression" }
@Param { value:"mainString: The original string argument" }
@Return { value: "The matching string array"}
public native function <Regex reg> findAll (string mainString) (string[]);

@Description { value:"Returns a Boolean value indicating whether the string matches the regular expression"}
@Param { value:"reg: The Regular Expression" }
@Param { value:"mainString: The original string argument" }
@Return { value: "True if the string matches the regex; false otherwise"}
public native function <Regex reg> matches (string mainString) (boolean);

@Description { value:"Replaces the mainString with the replacement on occurrences that matches the given regular expression" }
@Param { value:"reg: The Regular Expression" }
@Param { value:"mainString: The original string argument" }
@Param { value:"replaceWith: The replacement string" }
@Return { value:"The derived string" }
public native function <Regex reg> replaceAll (string mainString, string replaceWith) (string);

@Description { value:"Replaces the first instance of the regex with the replaceWith string and returns the result"}
@Param { value:"reg: The Regular Expression" }
@Param { value:"mainString: The original string argument" }
@Param { value:"replaceWith: The replacement string" }
@Return { value:"The derived string" }
public native function <Regex reg> replaceFirst (string mainString, string replaceWith) (string);
