
@Description {value:"Halts the current thread for a predefined amount of time."}
@Param {value:"millis: Amount of time to sleep in milliseconds"}
public native function sleepCurrentThread (int millis);

@Description {value:"Adds the given name, value pair to the system properties."}
@Param {value:"name: Name of the property"}
@Param {value:"value: Value of the property"}
public native function setProperty (string name, string value);

@Description {value:"Returns the value associated with the specified property name."}
@Param {value:"name: Name of the property"}
@Return { value:"Value of the property if the property exists, an empty string otherwise"}
public native function getProperty (string name) (string);

@Description {value:"Returns all system properties."}
@Return { value:"All system properties"}
public native function getProperties () (map);

@Description {value:"Returns the current working directory."}
@Return { value:"Current working directory or an empty string if the current working directory cannot be determined"}
public native function getCurrentDirectory () (string);

@Description {value:"Returns the charset encoding used in the runtime."}
@Return { value:"Encoding if it is available, an empty string otherwise"}
public native function getFileEncoding () (string);
