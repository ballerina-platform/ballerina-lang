package ballerina.test;

import ballerina.doc;

@doc:Description { value:"Starts the service specified in the 'serviceName' argument" }
@doc:Param {value:"serviceName: Name of the service to start"}
native function startService (string serviceName) (string);