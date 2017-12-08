package ballerina.os;

@Description {value:"Returns environment variable which is associated with the provided name."}
@Param {value:"name: name of the environment variable"}
@Return {value:"string: environment variable value if exists, otherwise an empty string"}
public native function getEnv (string name) (string);

@Description {value:"Splits the value of a environment variable using path separator and returns the separated values as an array."}
@Param {value:"name: name of the environment variable"}
@Return {value:"string[]: environment variable values as an array if the provided environment variable exists, otherwise an empty array"}
public native function getMultivaluedEnv (string name) (string[]);

@Description {value:"Returns the name of the Operating System."}
@Return {value:"string: OS name if the OS can be identified, an empty string otherwise"}
public native function getName () (string);

@Description {value:"Returns the version of the Operating System."}
@Return {value:"string: OS version if the OS can be identified, an empty string otherwise"}
public native function getVersion () (string);

@Description {value:"Returns the architecture of the Operating System."}
@Return {value:"string: OS architecture if the OS can be identified, an empty string otherwise"}
public native function getArchitecture () (string);
