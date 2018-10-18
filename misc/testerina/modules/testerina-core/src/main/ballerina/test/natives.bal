
@Description { value:"Starts all the services defined in the module specified in the 'moduleName' argument" }
@Param {value:"moduleName: Name of the module"}
public extern function startServices (string moduleName) returns (boolean);

@Description { value:"Stops all the services defined in the module specified in the 'moduleName' argument" }
@Param {value:"moduleName: Name of the module"}
public extern function stopServices (string moduleName);

@Description { value:"Start a service skeleton from a given Swagger definition in the given ballerina module." }
@Param {value:"moduleName: Name of the module"}
@Param {value:"swaggerFilePath: Path to the Swagger definition"}
public extern function startServiceSkeleton (string moduleName, string swaggerFilePath) returns (boolean);

@Description { value:"Stop a service skeleton and cleanup created directories of a given ballerina module." }
@Param {value:"moduleName: Name of the module"}
public extern function stopServiceSkeleton (string moduleName);