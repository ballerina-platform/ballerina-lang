package ballerina.test;

@Description { value:"Starts all the services defined in the package specified in the 'packageName' argument" }
@Param {value:"packageName: Name of the package"}
public native function startServices (string packageName) returns (boolean);

@Description { value:"Stops all the services defined in the package specified in the 'packageName' argument" }
@Param {value:"packageName: Name of the package"}
public native function stopServices (string packageName);