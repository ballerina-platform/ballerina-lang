package ballerina.os;

import ballerina.doc;

@doc:Description {value:"Returns environment variable which is associated with the provided name."}
@doc:Param {value:"name: name of the environment variable"}
@doc:Return {value:"string: environment variable value if exists, otherwise an empty string"}
public native function getEnv (string name) (string);

@doc:Description {value:"Splits the value of a environment variable using path separator and returns the separated values as an array."}
@doc:Param {value:"name: name of the environment variable"}
@doc:Return {value:"string[]: environment variable values as an array if the provided environment variable exists, otherwise an empty array"}
public native function getMultivaluedEnv (string name) (string[]);

@doc:Description {value:"Returns the name of the Operating System."}
@doc:Return {value:"string: OS name if the OS can be identified, an empty string otherwise"}
public native function getName () (string);

@doc:Description {value:"Returns the version of the Operating System."}
@doc:Return {value:"string: OS version if the OS can be identified, an empty string otherwise"}
public native function getVersion () (string);

@doc:Description {value:"Returns the architecture of the Operating System."}
@doc:Return {value:"string: OS architecture if the OS can be identified, an empty string otherwise"}
public native function getArchitecture () (string);
