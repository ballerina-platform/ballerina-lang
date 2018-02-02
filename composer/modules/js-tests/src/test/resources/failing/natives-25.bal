package ballerina.runtime;

@Description {value:"Sleeps the current thread for a predefined amount of time."}
@Param {value:"millis: amount of time to sleep in milliseconds"}
public native function sleepCurrentThread (int millis);

@Description {value:"Adds the given name, value pair to the system properties."}
@Param {value:"name: name of the property"}
@Param {value:"value: value of the property"}
public native function setProperty (string name, string value);

@Description {value:"Returns the value associated with the specified property name."}
@Param {value:"name: name of the property"}
@Return {value:"string: value of the property if the property exists, an empty string otherwise"}
public native function getProperty (string name) (string);

@Description {value:"Returns all system properties."}
@Return {value:"map: all system properties"}
public native function getProperties () (map);

@Description {value:"Returns the current working directory."}
@Return {value:"string: current working directory or an empty string if the current working directory cannot be determined"}
public native function getCurrentDirectory () (string);

@Description {value:"Returns the charset encoding used in the runtime."}
@Return {value:"string: Encoding if it is available, an empty string otherwise"}
public native function getFileEncoding () (string);
