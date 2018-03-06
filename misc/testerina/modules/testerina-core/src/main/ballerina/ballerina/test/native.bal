package ballerina.test;

@Description { value:"Starts the service specified in the 'serviceName' argument" }
@Param {value:"serviceName: Name of the service to start"}
public native function startService (string serviceName) (string);