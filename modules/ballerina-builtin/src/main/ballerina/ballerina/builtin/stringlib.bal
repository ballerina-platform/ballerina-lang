package ballerina.builtin;

@Description { value:"Compares two strings, ignoring the case of the strings"}
@Param { value:"mainString: The original string argument" }
@Param { value:"anotherString: The string to be compared" }
@Return { value:"boolean: True if the strings are equal; false otherwise" }
public native function <string mainString> equalsIgnoreCase (string anotherString) (boolean);
