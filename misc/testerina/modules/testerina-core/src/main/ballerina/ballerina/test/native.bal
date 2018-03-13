package ballerina.test;

@Description { value:"Starts the service specified in the 'serviceName' argument" }
@Param {value:"serviceName: Name of the service to be started"}
public native function startService (string serviceName) (string);

@Description { value:"Stops the service specified in the 'serviceName' argument" }
@Param {value:"serviceName: Name of the service to be stopped"}
public native function stopService (string serviceName);