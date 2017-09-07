package ballerina.runtime;

import ballerina.doc;

@doc:Description {value:"Sleeps the current thread for a predefined amount of time."}
@doc:Param {value:"millis: amount of time to sleep in milliseconds"}
native function sleepCurrentThread (int millis);

@doc:Description {value:"Adds the given name, value pair to the system properties."}
@doc:Param {value:"name: name of the property"}
@doc:Param {value:"value: value of the property"}
native function setProperty (string name, string value);

@doc:Description {value:"Returns the value associated with the specified property name."}
@doc:Param {value:"name: name of the property"}
@doc:Return {value:"string: value of the property if the property exists, an empty string otherwise"}
native function getProperty (string name) (string);

@doc:Description {value:"Returns all system properties."}
@doc:Return {value:"map: all system properties"}
native function getProperties () (map);

@doc:Description {value:"Returns the current working directory."}
@doc:Return {value:"string: current working directory or an empty string if the current working directory cannot be determined"}
native function getCurrentDirectory () (string);

@doc:Description {value:"Returns the charset encoding used in the runtime."}
@doc:Return {value:"string: Encoding if it is available, an empty string otherwise"}
native function getFileEncoding () (string);
