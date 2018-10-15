
@Description { value:"Starts all the services defined in the package specified in the 'packageName' argument" }
@Param {value:"packageName: Name of the package"}
public extern function startServices (string packageName) returns (boolean);

@Description { value:"Stops all the services defined in the package specified in the 'packageName' argument" }
@Param {value:"packageName: Name of the package"}
public extern function stopServices (string packageName);

@Description { value:"Start a service skeleton from a given Swagger definition in the given ballerina package." }
@Param {value:"packageName: Name of the package"}
@Param {value:"swaggerFilePath: Path to the Swagger definition"}
public extern function startServiceSkeleton (string packageName, string swaggerFilePath) returns (boolean);

@Description { value:"Stop a service skeleton and cleanup created directories of a given ballerina package." }
@Param {value:"packageName: Name of the package"}
public extern function stopServiceSkeleton (string packageName);