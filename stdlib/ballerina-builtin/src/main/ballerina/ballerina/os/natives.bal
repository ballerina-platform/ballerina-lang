package ballerina.os;

@Description {value:"Returns the environment variable value associated with the provided name."}
@Param {value:"name: Name of the environment variable"}
@Return { value:"Environment variable value if it exists, otherwise an empty string"}
documentation {
Returns the environment variable value associated with the provided name..
- #name Name of the environment variable
- #envVar Environment variable value if it exists, otherwise an empty string
}
public native function getEnv (string name) (string envVar);

@Description {value:"Splits the value of an environment variable using path separator and returns the separated values as an array."}
@Param {value:"name: Name of the environment variable"}
@Return { value:"Environment variable values as an array if the provided environment variable exists, otherwise an empty array"}
documentation {
Splits the value of an environment variable using path separator and returns the separated values as an array.
- #name Name of the environment variable
- #envVarArray Environment variable values as an array if the provided environment variable exists, otherwise an empty array
}
public native function getMultivaluedEnv (string name) (string[] envVarArray);

@Description {value:"Returns the name of the operating system."}
@Return { value:"OS name if the OS can be identified, an empty string otherwise"}
documentation {
Returns the name of the operating system.
- #name OS name if the OS can be identified, an empty string otherwise
}
public native function getName () (string name);

@Description {value:"Returns the version of the operating system."}
@Return { value:"OS version if the OS can be identified, an empty string otherwise"}
documentation {
Returns the version of the operating system.
- #ver OS version if the OS can be identified, an empty string otherwise
}
public native function getVersion () (string ver);

@Description {value:"Returns the architecture of the operating system."}
@Return { value:"OS architecture if the OS can be identified, an empty string otherwise"}
documentation {
Returns the architecture of the operating system.
- #architecture OS architecture if the OS can be identified, an empty string otherwise
}
public native function getArchitecture () (string architecture);
