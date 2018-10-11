# Starts all the services defined in the package specified in the 'packageName' argument.
#
# + packageName - Name of the package
#
# + return - `true` if the services were started successfully, `false` otherwise
public extern function startServices(string packageName) returns boolean;

# Stops all the services defined in the package specified in the 'packageName' argument.
#
# + packageName - Name of the package
public extern function stopServices(string packageName);

# Start a service skeleton from a given Swagger definition in the given ballerina package.
#
# + packageName - Name of the package
# + swaggerFilePath - Path to the Swagger definition
#
# + return - `true` if the service skeleton was started successfully, `false` otherwise
public extern function startServiceSkeleton(string packageName, string swaggerFilePath) returns boolean;

# Stop a service skeleton and cleanup created directories of a given ballerina package.
#
# + packageName - Name of the package
public extern function stopServiceSkeleton(string packageName);
